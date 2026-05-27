package com.financeiramente.core.domain.entity;

public class Meta {
    private final String id;
    private String nome;
    private double valorObjetivo;
    private double valorAtual;
    private double valorInicial;
    private String dataAlvo;
    private String descricao;
    private long criadoEm;

    public Meta(String id, String nome, double valorObjetivo, double valorAtual,
                double valorInicial, String dataAlvo, String descricao, long criadoEm) {
        this.id = id;
        this.nome = nome;
        this.valorObjetivo = valorObjetivo;
        this.valorAtual = valorAtual;
        this.valorInicial = valorInicial;
        this.dataAlvo = dataAlvo;
        this.descricao = descricao;
        this.criadoEm = criadoEm;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getValorObjetivo() { return valorObjetivo; }
    public void setValorObjetivo(double valorObjetivo) { this.valorObjetivo = valorObjetivo; }
    public double getValorAtual() { return valorAtual; }
    public void setValorAtual(double valorAtual) { this.valorAtual = valorAtual; }
    public double getValorInicial() { return valorInicial; }
    public void setValorInicial(double valorInicial) { this.valorInicial = valorInicial; }
    public String getDataAlvo() { return dataAlvo; }
    public void setDataAlvo(String dataAlvo) { this.dataAlvo = dataAlvo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public long getCriadoEm() { return criadoEm; }

    /** Valor efetivo restante considerando o valor inicial já deduzido. */
    public double getValorEfetivo() {
        return Math.max(0, valorObjetivo - valorInicial);
    }
}
