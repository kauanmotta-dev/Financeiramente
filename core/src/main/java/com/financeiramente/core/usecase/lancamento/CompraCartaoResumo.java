package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.entity.CompraCartao;

public class CompraCartaoResumo {
    private final CompraCartao compra;
    private final int parcelasPagas;
    private final int totalParcelas;
    private final double valorJaDebitado;

    public CompraCartaoResumo(CompraCartao compra, int parcelasPagas, int totalParcelas, double valorJaDebitado) {
        this.compra = compra;
        this.parcelasPagas = parcelasPagas;
        this.totalParcelas = totalParcelas;
        this.valorJaDebitado = valorJaDebitado;
    }

    public CompraCartao getCompra() { return compra; }
    public int getParcelasPagas() { return parcelasPagas; }
    public int getTotalParcelas() { return totalParcelas; }
    public double getValorJaDebitado() { return valorJaDebitado; }
}
