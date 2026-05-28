package com.financeiramente.core.usecase.relatorio;

import com.financeiramente.core.domain.entity.Lancamento;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DTO de saída do GerarRelatorioUseCase.
 */
public class RelatorioResult {
    private final List<Lancamento> lancamentos;
    private final Map<String, BigDecimal> totalPorCategoria;
    private final BigDecimal totalReceitas;
    private final BigDecimal totalDespesas;
    private final BigDecimal saldo;

    public RelatorioResult(List<Lancamento> lancamentos,
                           BigDecimal totalReceitas,
                           BigDecimal totalDespesas,
                           Map<String, BigDecimal> totalPorCategoria) {
        this.lancamentos = lancamentos != null
                ? Collections.unmodifiableList(lancamentos)
                : Collections.emptyList();
        this.totalPorCategoria = totalPorCategoria != null
                ? Collections.unmodifiableMap(totalPorCategoria)
                : Collections.emptyMap();
        this.totalReceitas = totalReceitas != null ? totalReceitas : BigDecimal.ZERO;
        this.totalDespesas = totalDespesas != null ? totalDespesas : BigDecimal.ZERO;
        this.saldo = this.totalReceitas.subtract(this.totalDespesas);
    }

    public List<Lancamento> getLancamentos() { return lancamentos; }
    public Map<String, BigDecimal> getTotalPorCategoria() { return totalPorCategoria; }
    public BigDecimal getTotalReceitas() { return totalReceitas; }
    public BigDecimal getTotalDespesas() { return totalDespesas; }
    public BigDecimal getSaldo() { return saldo; }
}
