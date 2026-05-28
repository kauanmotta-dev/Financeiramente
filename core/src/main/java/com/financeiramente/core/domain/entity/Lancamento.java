package com.financeiramente.core.domain.entity;

import com.financeiramente.core.domain.vo.TipoLancamento;
import java.math.BigDecimal;

public class Lancamento {
    private final String id;
    private BigDecimal valor;
    private TipoLancamento tipo;
    private String data;
    private String descricao;
    private String categoriaId;
    private String faturaId;
    private String compraCartaoId;
    private long criadoEm;
    private long atualizadoEm;

    public Lancamento(String id, BigDecimal valor, TipoLancamento tipo, String data,
                      String descricao, String categoriaId, String faturaId,
                      String compraCartaoId,
                      long criadoEm, long atualizadoEm) {
        this.id = id;
        this.valor = valor;
        this.tipo = tipo;
        this.data = data;
        this.descricao = descricao;
        this.categoriaId = categoriaId;
        this.faturaId = faturaId;
        this.compraCartaoId = compraCartaoId;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public String getId() { return id; }
    public BigDecimal getValor() { return valor; }
    public TipoLancamento getTipo() { return tipo; }
    public String getData() { return data; }
    public String getDescricao() { return descricao; }
    public String getCategoriaId() { return categoriaId; }
    public String getFaturaId() { return faturaId; }
    public void setFaturaId(String faturaId) { this.faturaId = faturaId; }
    public String getCompraCartaoId() { return compraCartaoId; }
    public void setCompraCartaoId(String compraCartaoId) { this.compraCartaoId = compraCartaoId; }
    public long getCriadoEm() { return criadoEm; }
    public long getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(long atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public void atualizarDados(BigDecimal valor, TipoLancamento tipo, String data, String descricao, String categoriaId) {
        this.valor = valor;
        this.tipo = tipo;
        this.data = data;
        this.descricao = descricao;
        this.categoriaId = categoriaId;
    }

    public static Builder builder(String id) { return new Builder(id); }

    public static final class Builder {
        private final String id;
        private BigDecimal valor = BigDecimal.ZERO;
        private TipoLancamento tipo = TipoLancamento.DESPESA;
        private String data;
        private String descricao;
        private String categoriaId;
        private String faturaId;
        private String compraCartaoId;
        private long criadoEm = System.currentTimeMillis();
        private long atualizadoEm = System.currentTimeMillis();

        private Builder(String id) { this.id = id; }

        public Builder valor(BigDecimal valor) { this.valor = valor; return this; }
        public Builder valor(double valor) { this.valor = BigDecimal.valueOf(valor); return this; }
        public Builder tipo(TipoLancamento tipo) { this.tipo = tipo; return this; }
        public Builder data(String data) { this.data = data; return this; }
        public Builder descricao(String descricao) { this.descricao = descricao; return this; }
        public Builder categoriaId(String categoriaId) { this.categoriaId = categoriaId; return this; }
        public Builder faturaId(String faturaId) { this.faturaId = faturaId; return this; }
        public Builder compraCartaoId(String compraCartaoId) { this.compraCartaoId = compraCartaoId; return this; }
        public Builder criadoEm(long criadoEm) { this.criadoEm = criadoEm; return this; }
        public Builder atualizadoEm(long atualizadoEm) { this.atualizadoEm = atualizadoEm; return this; }

        public Lancamento build() {
            return new Lancamento(id, valor, tipo, data, descricao, categoriaId,
                    faturaId, compraCartaoId, criadoEm, atualizadoEm);
        }
    }
}
