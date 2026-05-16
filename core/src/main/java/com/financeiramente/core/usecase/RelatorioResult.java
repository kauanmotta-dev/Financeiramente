package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Lancamento;
import java.util.Collections;
import java.util.List;

/**
 * DTO de saída do GerarRelatorioUseCase.
 */
public class RelatorioResult {
    private final List<Lancamento> lancamentos;
    private final double totalReceitas;
    private final double totalDespesas;
    private final double saldo;

    public RelatorioResult(List<Lancamento> lancamentos,
                           double totalReceitas,
                           double totalDespesas) {
        this.lancamentos = lancamentos != null
                ? Collections.unmodifiableList(lancamentos)
                : Collections.emptyList();
        this.totalReceitas = totalReceitas;
        this.totalDespesas = totalDespesas;
        this.saldo = totalReceitas - totalDespesas;
    }

    public List<Lancamento> getLancamentos() { return lancamentos; }
    public double getTotalReceitas() { return totalReceitas; }
    public double getTotalDespesas() { return totalDespesas; }
    public double getSaldo() { return saldo; }
}
