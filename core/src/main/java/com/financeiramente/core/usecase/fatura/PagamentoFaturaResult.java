package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.domain.vo.StatusFatura;
import java.math.BigDecimal;

public class PagamentoFaturaResult {

    private final StatusFatura statusResultante;
    private final BigDecimal saldoDevedor;

    public PagamentoFaturaResult(StatusFatura statusResultante, BigDecimal saldoDevedor) {
        this.statusResultante = statusResultante;
        this.saldoDevedor = saldoDevedor;
    }

    public StatusFatura getStatusResultante() { return statusResultante; }
    public BigDecimal getSaldoDevedor() { return saldoDevedor; }
}
