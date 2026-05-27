package com.financeiramente.core.usecase.relatorio;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        List<Lancamento> lancamentos = lancamentoRepository.listarPorPeriodo(
                filtro.getDataInicio(), filtro.getDataFim());

        if (filtro.getCategoriaId() != null) {
            List<Lancamento> filtrados = new ArrayList<>();
            for (Lancamento l : lancamentos) {
                if (filtro.getCategoriaId().equals(l.getCategoriaId())) {
                    filtrados.add(l);
                }
            }
            lancamentos = filtrados;
        }

        if (filtro.getTipo() != null) {
            List<Lancamento> filtrados = new ArrayList<>();
            for (Lancamento l : lancamentos) {
                if (filtro.getTipo().equals(l.getTipo())) {
                    filtrados.add(l);
                }
            }
            lancamentos = filtrados;
        }

        if (filtro.getTagId() != null) {
            List<Lancamento> filtrados = new ArrayList<>();
            for (Lancamento l : lancamentos) {
                List<Tag> tags = tagRepository.listarPorLancamento(l.getId());
                for (Tag t : tags) {
                    if (filtro.getTagId().equals(t.getId())) {
                        filtrados.add(l);
                        break;
                    }
                }
            }
            lancamentos = filtrados;
        }

        double totalReceitas = 0.0;
        double totalDespesas = 0.0;
        Map<String, Double> totalPorCategoria = new LinkedHashMap<>();

        for (Lancamento l : lancamentos) {
            if (l.getTipo() == TipoLancamento.RECEITA) {
                totalReceitas += l.getValor();
            } else {
                totalDespesas += l.getValor();
                String catNome = categoriaRepository.buscarPorId(l.getCategoriaId())
                        .map(Categoria::getNome)
                        .orElse(l.getCategoriaId());
                totalPorCategoria.merge(catNome, l.getValor(), Double::sum);
            }
        }

        return new RelatorioResult(lancamentos, totalReceitas, totalDespesas, totalPorCategoria);
    }
}
