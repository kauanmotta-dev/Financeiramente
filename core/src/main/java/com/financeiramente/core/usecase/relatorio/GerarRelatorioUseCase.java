package com.financeiramente.core.usecase.relatorio;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * Gera um relatório financeiro aplicando os filtros de FiltroRelatorio.
 * Filtros: período, categoria, tag, tipo.
 * Agrupa despesas por categoria no resultado.
 */
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
        List<Lancamento> lancamentos = lancamentoRepository.listarPorPeriodoComFiltros(
            filtro.getDataInicio(),
            filtro.getDataFim(),
            filtro.getCategoriaId(),
            filtro.getTipo(),
            filtro.getTagId()
        );

        BigDecimal totalReceitas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;
        Map<String, BigDecimal> totaisPorCategoriaId = lancamentoRepository.somarDespesasPorCategoriaEPeriodo(
            filtro.getDataInicio(),
            filtro.getDataFim(),
            filtro.getCategoriaId(),
            filtro.getTipo(),
            filtro.getTagId()
        );
        Map<String, String> categoriaIdParaNome = carregarCategoriasPorId(totaisPorCategoriaId);
        Map<String, BigDecimal> totalPorCategoria = new LinkedHashMap<>();

        for (Map.Entry<String, BigDecimal> entry : totaisPorCategoriaId.entrySet()) {
            String categoriaId = entry.getKey();
            String nomeCategoria = categoriaIdParaNome.getOrDefault(categoriaId, categoriaId);
            totalPorCategoria.put(nomeCategoria, entry.getValue());
        }

        for (Lancamento l : lancamentos) {
            if (l.getTipo() == TipoLancamento.RECEITA) {
                totalReceitas = totalReceitas.add(l.getValor());
            } else {
                totalDespesas = totalDespesas.add(l.getValor());
            }
        }

        return new RelatorioResult(lancamentos, totalReceitas, totalDespesas, totalPorCategoria);
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
