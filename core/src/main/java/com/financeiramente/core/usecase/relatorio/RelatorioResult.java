package com.financeiramente.core.usecase.relatorio;

import com.financeiramente.core.domain.entity.Lancamento;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * DTO de saída do GerarRelatorioUseCase.
 */
public class RelatorioResult {
    private final List<Lancamento> lancamentos;
    private final Map<String, Double> totalPorCategoria;
    private final double totalReceitas;
    private final double totalDespesas;
    private final double saldo;

    public RelatorioResult(List<Lancamento> lancamentos,
                           double totalReceitas,
                           double totalDespesas,
                           Map<String, Double> totalPorCategoria) {
        this.lancamentos = lancamentos != null
                ? Collections.unmodifiableList(lancamentos)
                : Collections.emptyList();
        this.totalPorCategoria = totalPorCategoria != null
                ? Collections.unmodifiableMap(totalPorCategoria)
                : Collections.emptyMap();
        this.totalReceitas = totalReceitas;
        this.totalDespesas = totalDespesas;
        this.saldo = totalReceitas - totalDespesas;
    }

    public List<Lancamento> getLancamentos() { return lancamentos; }
    public Map<String, Double> getTotalPorCategoria() { return totalPorCategoria; }
    public double getTotalReceitas() { return totalReceitas; }
    public double getTotalDespesas() { return totalDespesas; }
    public double getSaldo() { return saldo; }
}
