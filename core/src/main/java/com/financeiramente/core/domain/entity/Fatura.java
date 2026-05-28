package com.financeiramente.core.domain.entity;

import com.financeiramente.core.domain.vo.StatusFatura;
import java.math.BigDecimal;

public class Fatura {
    private final String id;
    private final String cartaoId;
    private final String mes;
    private final String dataFechamento;
    private final String dataVencimento;
    private BigDecimal valorPago;
    private StatusFatura status;
    private String descricao;
    private final long criadoEm;
    private long atualizadoEm;

    public Fatura(String id, String cartaoId, String mes, String dataFechamento,
                  String dataVencimento, BigDecimal valorPago, StatusFatura status,
                  String descricao, long criadoEm, long atualizadoEm) {
        this.id = id;
        this.cartaoId = cartaoId;
        this.mes = mes;
        this.dataFechamento = dataFechamento;
        this.dataVencimento = dataVencimento;
        this.valorPago = valorPago;
        this.status = status;
        this.descricao = descricao;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public String getId() { return id; }
    public String getCartaoId() { return cartaoId; }
    public String getMes() { return mes; }
    public String getDataFechamento() { return dataFechamento; }
    public String getDataVencimento() { return dataVencimento; }
    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }
    public StatusFatura getStatus() { return status; }
    public void setStatus(StatusFatura status) { this.status = status; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public long getCriadoEm() { return criadoEm; }
    public long getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(long atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public static Builder builder(String id) { return new Builder(id); }

    public static final class Builder {
        private final String id;
        private String cartaoId;
        private String mes;
        private String dataFechamento;
        private String dataVencimento;
        private BigDecimal valorPago = BigDecimal.ZERO;
        private StatusFatura status = StatusFatura.ABERTO;
        private String descricao;
        private long criadoEm = System.currentTimeMillis();
        private long atualizadoEm = System.currentTimeMillis();

        private Builder(String id) { this.id = id; }

        public Builder cartaoId(String cartaoId) { this.cartaoId = cartaoId; return this; }
        public Builder mes(String mes) { this.mes = mes; return this; }
        public Builder dataFechamento(String dataFechamento) { this.dataFechamento = dataFechamento; return this; }
        public Builder dataVencimento(String dataVencimento) { this.dataVencimento = dataVencimento; return this; }
        public Builder valorPago(BigDecimal valorPago) { this.valorPago = valorPago; return this; }
        public Builder status(StatusFatura status) { this.status = status; return this; }
        public Builder descricao(String descricao) { this.descricao = descricao; return this; }
        public Builder criadoEm(long criadoEm) { this.criadoEm = criadoEm; return this; }
        public Builder atualizadoEm(long atualizadoEm) { this.atualizadoEm = atualizadoEm; return this; }

        public Fatura build() {
            return new Fatura(id, cartaoId, mes, dataFechamento, dataVencimento,
                    valorPago, status, descricao, criadoEm, atualizadoEm);
        }
    }
}
