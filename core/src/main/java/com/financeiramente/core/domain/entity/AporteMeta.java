package com.financeiramente.core.domain.entity;

public class AporteMeta {
    private final String id;
    private String metaId;
    private double valor;
    private String data;
    private String descricao;
    private long criadoEm;

    public AporteMeta(String id, String metaId, double valor, String data,
                      String descricao, long criadoEm) {
        this.id = id;
        this.metaId = metaId;
        this.valor = valor;
        this.data = data;
        this.descricao = descricao;
        this.criadoEm = criadoEm;
    }

    public String getId() { return id; }
    public String getMetaId() { return metaId; }
    public void setMetaId(String metaId) { this.metaId = metaId; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public long getCriadoEm() { return criadoEm; }
}
