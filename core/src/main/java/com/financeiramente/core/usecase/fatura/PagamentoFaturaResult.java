package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.domain.vo.StatusFatura;

public class PagamentoFaturaResult {

    private final StatusFatura statusResultante;
    private final double saldoDevedor;

    public PagamentoFaturaResult(StatusFatura statusResultante, double saldoDevedor) {
        this.statusResultante = statusResultante;
        this.saldoDevedor = saldoDevedor;
    }

    public StatusFatura getStatusResultante() { return statusResultante; }
    public double getSaldoDevedor() { return saldoDevedor; }
}
