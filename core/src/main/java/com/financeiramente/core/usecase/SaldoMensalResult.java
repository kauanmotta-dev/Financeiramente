package com.financeiramente.core.usecase;

import java.util.Collections;
import java.util.List;

public class SaldoMensalResult {
    private final double receitaRealizada;
    private final double totalGasto;
    private final double saldoDisponivel;
    private final List<SaldoCategoria> saldosPorCategoria;

    public SaldoMensalResult(double receitaRealizada, double totalGasto,
                             double saldoDisponivel, List<SaldoCategoria> saldosPorCategoria) {
        this.receitaRealizada = receitaRealizada;
        this.totalGasto = totalGasto;
        this.saldoDisponivel = saldoDisponivel;
        this.saldosPorCategoria = saldosPorCategoria != null
                ? Collections.unmodifiableList(saldosPorCategoria)
                : Collections.emptyList();
    }

    public double getReceitaRealizada() { return receitaRealizada; }
    public double getTotalGasto() { return totalGasto; }
    public double getSaldoDisponivel() { return saldoDisponivel; }
    public List<SaldoCategoria> getSaldosPorCategoria() { return saldosPorCategoria; }
}
