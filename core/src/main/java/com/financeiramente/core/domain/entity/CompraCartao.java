package com.financeiramente.core.domain.entity;

import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.util.DomainException;
import java.math.BigDecimal;

public class CompraCartao {
    private final String id;
    private String cartaoId;
    private String descricao;
    private BigDecimal valorTotal;
    private TipoCompraCartao tipo;
    private Integer totalParcelas;
    private String categoriaId;
    private String dataCompra;
    private Integer diaRecorrencia;
    private boolean ativo;
    private final long criadoEm;
    private long atualizadoEm;

    public CompraCartao(String id, String cartaoId, String descricao, BigDecimal valorTotal,
                        TipoCompraCartao tipo, Integer totalParcelas, String categoriaId,
                        String dataCompra, Integer diaRecorrencia, boolean ativo,
                        long criadoEm, long atualizadoEm) {
        this.id = id;
        this.cartaoId = cartaoId;
        this.descricao = descricao;
        this.valorTotal = valorTotal;
        this.tipo = tipo;
        this.totalParcelas = totalParcelas;
        this.categoriaId = categoriaId;
        this.dataCompra = dataCompra;
        this.diaRecorrencia = diaRecorrencia;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public String getId() { return id; }
    public String getCartaoId() { return cartaoId; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public TipoCompraCartao getTipo() { return tipo; }
    public Integer getTotalParcelas() { return totalParcelas; }
    public String getCategoriaId() { return categoriaId; }
    public String getDataCompra() { return dataCompra; }
    public Integer getDiaRecorrencia() { return diaRecorrencia; }
    public boolean isAtivo() { return ativo; }
    public long getCriadoEm() { return criadoEm; }
    public long getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(long atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public void cancelarRecorrencia(long atualizadoEm) {
        this.ativo = false;
        this.atualizadoEm = atualizadoEm;
    }

    public static Builder builder(String id) { return new Builder(id); }

    public static final class Builder {
        private final String id;
        private String cartaoId;
        private String descricao;
        private BigDecimal valorTotal = BigDecimal.ZERO;
        private TipoCompraCartao tipo;
        private Integer totalParcelas;
        private String categoriaId;
        private String dataCompra;
        private Integer diaRecorrencia;
        private boolean ativo = true;
        private long criadoEm = System.currentTimeMillis();
        private long atualizadoEm = System.currentTimeMillis();

        private Builder(String id) { this.id = id; }

        public Builder cartaoId(String cartaoId) { this.cartaoId = cartaoId; return this; }
        public Builder descricao(String descricao) { this.descricao = descricao; return this; }
        public Builder valorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; return this; }
        public Builder tipo(TipoCompraCartao tipo) { this.tipo = tipo; return this; }
        public Builder totalParcelas(Integer totalParcelas) { this.totalParcelas = totalParcelas; return this; }
        public Builder categoriaId(String categoriaId) { this.categoriaId = categoriaId; return this; }
        public Builder dataCompra(String dataCompra) { this.dataCompra = dataCompra; return this; }
        public Builder diaRecorrencia(Integer diaRecorrencia) { this.diaRecorrencia = diaRecorrencia; return this; }
        public Builder ativo(boolean ativo) { this.ativo = ativo; return this; }
        public Builder criadoEm(long criadoEm) { this.criadoEm = criadoEm; return this; }
        public Builder atualizadoEm(long atualizadoEm) { this.atualizadoEm = atualizadoEm; return this; }

        public CompraCartao build() {
            if (cartaoId == null || cartaoId.trim().isEmpty()) {
                throw new DomainException("Cartão da compra não pode ser vazio.");
            }
            if (descricao == null || descricao.trim().isEmpty()) {
                throw new DomainException("Descrição da compra não pode ser vazia.");
            }
            if (valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
                throw new DomainException("Valor total da compra deve ser maior que zero.");
            }
            if (tipo == null) {
                throw new DomainException("Tipo da compra no cartão é obrigatório.");
            }
            if (dataCompra == null || dataCompra.trim().isEmpty()) {
                throw new DomainException("Data da compra é obrigatória.");
            }

            if (tipo == TipoCompraCartao.PARCELADO) {
                if (totalParcelas == null || totalParcelas < 2 || totalParcelas > 480) {
                    throw new DomainException("Parcelamento deve ter entre 2 e 480 parcelas.");
                }
            } else {
                totalParcelas = null;
            }

            if (tipo == TipoCompraCartao.RECORRENTE) {
                if (diaRecorrencia == null || diaRecorrencia < 1 || diaRecorrencia > 28) {
                    throw new DomainException("Dia da recorrência deve estar entre 1 e 28.");
                }
            } else {
                diaRecorrencia = null;
            }

            return new CompraCartao(
                    id,
                    cartaoId,
                    descricao.trim(),
                    valorTotal,
                    tipo,
                    totalParcelas,
                    categoriaId,
                    dataCompra,
                    diaRecorrencia,
                    ativo,
                    criadoEm,
                    atualizadoEm
            );
        }
    }
}