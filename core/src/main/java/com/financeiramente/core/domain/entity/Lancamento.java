package com.financeiramente.core.domain.entity;

import com.financeiramente.core.domain.vo.TipoLancamento;

public class Lancamento {
    private final String id;
    private double valor;
    private TipoLancamento tipo;
    private String data;
    private String descricao;
    private String categoriaId;
    private String recorrenteId;
    private long criadoEm;
    private long atualizadoEm;

    public Lancamento(String id, double valor, TipoLancamento tipo, String data,
                      String descricao, String categoriaId, String recorrenteId,
                      long criadoEm, long atualizadoEm) {
        this.id = id;
        this.valor = valor;
        this.tipo = tipo;
        this.data = data;
        this.descricao = descricao;
        this.categoriaId = categoriaId;
        this.recorrenteId = recorrenteId;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public String getId() { return id; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public TipoLancamento getTipo() { return tipo; }
    public void setTipo(TipoLancamento tipo) { this.tipo = tipo; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }
    public String getRecorrenteId() { return recorrenteId; }
    public void setRecorrenteId(String recorrenteId) { this.recorrenteId = recorrenteId; }
    public long getCriadoEm() { return criadoEm; }
    public long getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(long atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public static Builder builder(String id) { return new Builder(id); }

    public static final class Builder {
        private final String id;
        private double valor;
        private TipoLancamento tipo = TipoLancamento.DESPESA;
        private String data;
        private String descricao;
        private String categoriaId;
        private String recorrenteId;
        private long criadoEm = System.currentTimeMillis();
        private long atualizadoEm = System.currentTimeMillis();

        private Builder(String id) { this.id = id; }

        public Builder valor(double valor) { this.valor = valor; return this; }
        public Builder tipo(TipoLancamento tipo) { this.tipo = tipo; return this; }
        public Builder data(String data) { this.data = data; return this; }
        public Builder descricao(String descricao) { this.descricao = descricao; return this; }
        public Builder categoriaId(String categoriaId) { this.categoriaId = categoriaId; return this; }
        public Builder recorrenteId(String recorrenteId) { this.recorrenteId = recorrenteId; return this; }
        public Builder criadoEm(long criadoEm) { this.criadoEm = criadoEm; return this; }
        public Builder atualizadoEm(long atualizadoEm) { this.atualizadoEm = atualizadoEm; return this; }

        public Lancamento build() {
            return new Lancamento(id, valor, tipo, data, descricao, categoriaId,
                    recorrenteId, criadoEm, atualizadoEm);
        }
    }
}
