package com.financeiramente.core.usecase.relatorio;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;






public class GerarRelatorioUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;

    public GerarRelatorioUseCase(LancamentoRepository lancamentoRepository,
                                 CategoriaRepository categoriaRepository,
                                 TagRepository tagRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.categoriaRepository  = categoriaRepository;
        this.tagRepository        = tagRepository;
    }

    public RelatorioResult executar(FiltroRelatorio filtro) {
        List<Lancamento> lancamentosOriginais = lancamentoRepository.listarPorPeriodoComFiltros(
            filtro.getDataInicio(),
            filtro.getDataFim(),
            filtro.getCategoriaId(),
            filtro.getTipo(),
            filtro.getTagId()
        );

        List<Lancamento> lancamentos = new ArrayList<>();
        BigDecimal totalReceitas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;
        Map<String, BigDecimal> totaisPorCategoriaId = new LinkedHashMap<>();

        for (Lancamento lancamento : lancamentosOriginais) {
            if (ehLancamentoDerivadoDeFatura(lancamento)) {
                continue;
            }

            lancamentos.add(lancamento);

            if (lancamento.getTipo() == TipoLancamento.RECEITA) {
                totalReceitas = totalReceitas.add(lancamento.getValor());
            } else {
                totalDespesas = totalDespesas.add(lancamento.getValor());
                if (lancamento.getCategoriaId() != null) {
                    totaisPorCategoriaId.merge(lancamento.getCategoriaId(), lancamento.getValor(), BigDecimal::add);
                }
            }
        }

        Map<String, String> categoriaIdParaNome = carregarCategoriasPorId(totaisPorCategoriaId);
        Map<String, BigDecimal> totalPorCategoria = new LinkedHashMap<>();

        for (Map.Entry<String, BigDecimal> entry : totaisPorCategoriaId.entrySet()) {
            String categoriaId = entry.getKey();
            String nomeCategoria = categoriaIdParaNome.getOrDefault(categoriaId, categoriaId);
            totalPorCategoria.put(nomeCategoria, entry.getValue());
        }

        return new RelatorioResult(lancamentos, totalReceitas, totalDespesas, totalPorCategoria);
    }

    private boolean ehLancamentoDerivadoDeFatura(Lancamento lancamento) {
        if (lancamentoRepository.buscarFaturaIdPorLancamentoPagamento(lancamento.getId()).isPresent()) {
            return true;
        }

        String descricao = lancamento.getDescricao();
        if (descricao == null) {
            return false;
        }

        String normalizada = descricao.trim().toLowerCase();
        return "ajuste de fatura".equals(normalizada)
            || normalizada.startsWith("saldo anterior (");
    }

    private Map<String, String> carregarCategoriasPorId(Map<String, BigDecimal> totaisPorCategoriaId) {
        if (totaisPorCategoriaId.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Categoria> categorias = categoriaRepository.listarPorIds(List.copyOf(totaisPorCategoriaId.keySet()));
        Map<String, String> categoriaIdParaNome = new HashMap<>();
        for (Categoria categoria : categorias) {
            categoriaIdParaNome.put(categoria.getId(), categoria.getNome());
        }
        return categoriaIdParaNome;
    }
}
