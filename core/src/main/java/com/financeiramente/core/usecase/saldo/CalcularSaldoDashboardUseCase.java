package com.financeiramente.core.usecase.saldo;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalcularSaldoDashboardUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FaturaRepository faturaRepository;
    private final AporteMetaRepository aporteMetaRepository;

    public CalcularSaldoDashboardUseCase(LancamentoRepository lancamentoRepository,
                                          CategoriaRepository categoriaRepository,
                                          FaturaRepository faturaRepository,
                                          AporteMetaRepository aporteMetaRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.categoriaRepository  = categoriaRepository;
        this.faturaRepository     = faturaRepository;
        this.aporteMetaRepository = aporteMetaRepository;
    }

    public SaldoDashboardResult executar(int ano, int mes) {
        List<Categoria> todasCategorias = categoriaRepository.listarTodas();

        // ── Saldo em Conta ────────────────────────────────────────────────────
        double totalReceita       = lancamentoRepository.somarPorTipoEMes(TipoLancamento.RECEITA, ano, mes).doubleValue();
        double despesasSemFatura  = lancamentoRepository.somarDespesasSemFaturaPorMes(ano, mes).doubleValue();
        double faturasPagas       = faturaRepository.somarValorPagoPorDataDePagamento(ano, mes).doubleValue();
        double aportesMeta        = aporteMetaRepository.somarPorMesEAno(mes, ano).doubleValue();
        double saldoConta         = totalReceita - despesasSemFatura - faturasPagas - aportesMeta;
        double totalGastoConta    = despesasSemFatura + faturasPagas + aportesMeta;

        // ── Saldo Essenciais ──────────────────────────────────────────────────
        double limiteEssenciais  = calcularLimitePorTipo(TipoCategoria.ESSENCIAL, todasCategorias);
        double gastoEssenciais   = lancamentoRepository.somarDespesasPorTipoCategoriaEMes(TipoCategoria.ESSENCIAL, ano, mes).doubleValue();
        double saldoEssenciais   = limiteEssenciais - gastoEssenciais;

        // ── Saldo Não Essenciais ──────────────────────────────────────────────
        double limiteNaoEssenciais = calcularLimitePorTipo(TipoCategoria.NAO_ESSENCIAL, todasCategorias);
        double gastoNaoEssenciais  = lancamentoRepository.somarDespesasPorTipoCategoriaEMes(TipoCategoria.NAO_ESSENCIAL, ano, mes).doubleValue();
        double saldoNaoEssenciais  = limiteNaoEssenciais - gastoNaoEssenciais;

        return new SaldoDashboardResult(
                saldoConta, totalReceita, totalGastoConta,
                saldoEssenciais, limiteEssenciais, gastoEssenciais,
                saldoNaoEssenciais, limiteNaoEssenciais, gastoNaoEssenciais);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double calcularLimitePorTipo(TipoCategoria tipo, List<Categoria> todas) {
        Map<String, List<Categoria>> filhasPorPai = new HashMap<>();
        List<Categoria> raizes = new ArrayList<>();

        for (Categoria cat : todas) {
            if (cat.getPaiId() == null) {
                if (cat.getTipo() == tipo) raizes.add(cat);
            } else {
                filhasPorPai.computeIfAbsent(cat.getPaiId(), k -> new ArrayList<>()).add(cat);
            }
        }

        double total = 0.0;
        for (Categoria raiz : raizes) {
            total += calcularLimiteEfetivo(raiz, filhasPorPai);
        }
        return total;
    }

    private double calcularLimiteEfetivo(Categoria categoria,
                                          Map<String, List<Categoria>> filhasPorPai) {
        if (categoria.getLimiteMensal() != null && categoria.getLimiteMensal().compareTo(BigDecimal.ZERO) > 0) {
            return categoria.getLimiteMensal().doubleValue();
        }
        double somaFilhas = 0.0;
        for (Categoria filha : filhasPorPai.getOrDefault(categoria.getId(), Collections.emptyList())) {
            somaFilhas += calcularLimiteEfetivo(filha, filhasPorPai);
        }
        return somaFilhas;
    }
}
