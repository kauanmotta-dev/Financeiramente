package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.domain.vo.StatusFatura;
import java.math.BigDecimal;

public class PagamentoFaturaResult {

    private final StatusFatura statusResultante;
    private final BigDecimal saldoDevedor;
    private final String lancamentoPagamentoId;

    public PagamentoFaturaResult(StatusFatura statusResultante, BigDecimal saldoDevedor, String lancamentoPagamentoId) {
        this.statusResultante = statusResultante;
        this.saldoDevedor = saldoDevedor;
        this.lancamentoPagamentoId = lancamentoPagamentoId;
    }

    public StatusFatura getStatusResultante() { return statusResultante; }
    public BigDecimal getSaldoDevedor() { return saldoDevedor; }
    public String getLancamentoPagamentoId() { return lancamentoPagamentoId; }
}
