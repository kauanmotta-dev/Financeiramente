package com.financeiramente.core.domain.entity;

public class Meta {
    private final String id;
    private String nome;
    private double valorObjetivo;
    private double valorAtual;
    private String dataAlvo;
    private String descricao;
    private boolean ativo;
    private long criadoEm;

    public Meta(String id, String nome, double valorObjetivo, double valorAtual,
                String dataAlvo, String descricao, boolean ativo, long criadoEm) {
        this.id = id;
        this.nome = nome;
        this.valorObjetivo = valorObjetivo;
        this.valorAtual = valorAtual;
        this.dataAlvo = dataAlvo;
        this.descricao = descricao;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getValorObjetivo() { return valorObjetivo; }
    public void setValorObjetivo(double valorObjetivo) { this.valorObjetivo = valorObjetivo; }
    public double getValorAtual() { return valorAtual; }
    public void setValorAtual(double valorAtual) { this.valorAtual = valorAtual; }
    public String getDataAlvo() { return dataAlvo; }
    public void setDataAlvo(String dataAlvo) { this.dataAlvo = dataAlvo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public long getCriadoEm() { return criadoEm; }
}
