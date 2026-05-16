package com.financeiramente.core.usecase;

import java.util.Collections;
import java.util.List;

/**
 * DTO de saída do CalcularSaldoMensalUseCase.
 *
 * Fórmula do saldo disponível atual (PRD / MER-DER seção 5):
 *   receitaRealizada − totalGasto − totalProvisoesMensais − reservaImprevisto = saldoDisponivel
 */
public class SaldoMensalResult {
    private final double receitaRealizada;
    private final double totalGasto;
    private final double totalProvisoesMensais;
    private final double reservaImprevisto;
    private final double saldoDisponivel;
    private final List<SaldoCategoria> saldosPorCategoria;

    public SaldoMensalResult(double receitaRealizada, double totalGasto,
                             double totalProvisoesMensais, double reservaImprevisto,
                             double saldoDisponivel, List<SaldoCategoria> saldosPorCategoria) {
        this.receitaRealizada = receitaRealizada;
        this.totalGasto = totalGasto;
        this.totalProvisoesMensais = totalProvisoesMensais;
        this.reservaImprevisto = reservaImprevisto;
        this.saldoDisponivel = saldoDisponivel;
        this.saldosPorCategoria = saldosPorCategoria != null
                ? Collections.unmodifiableList(saldosPorCategoria)
                : Collections.emptyList();
    }

    public double getReceitaRealizada() { return receitaRealizada; }
    public double getTotalGasto() { return totalGasto; }
    public double getTotalProvisoesMensais() { return totalProvisoesMensais; }
    public double getReservaImprevisto() { return reservaImprevisto; }
    public double getSaldoDisponivel() { return saldoDisponivel; }
    public List<SaldoCategoria> getSaldosPorCategoria() { return saldosPorCategoria; }
}
