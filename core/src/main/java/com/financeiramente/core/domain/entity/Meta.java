package com.financeiramente.core.domain.entity;

import java.math.BigDecimal;

public class Meta {
    private final String id;
    private String nome;
    private BigDecimal valorObjetivo;
    private BigDecimal valorAtual;
    private BigDecimal valorInicial;
    private String dataAlvo;
    private String descricao;
    private long criadoEm;

    public Meta(String id, String nome, BigDecimal valorObjetivo, BigDecimal valorAtual,
                BigDecimal valorInicial, String dataAlvo, String descricao, long criadoEm) {
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
    public BigDecimal getValorObjetivo() { return valorObjetivo; }
    public BigDecimal getValorAtual() { return valorAtual; }
    public BigDecimal getValorInicial() { return valorInicial; }
    public String getDataAlvo() { return dataAlvo; }
    public String getDescricao() { return descricao; }
    public long getCriadoEm() { return criadoEm; }

    public void atualizarDadosEdicao(String nome, BigDecimal valorObjetivo, BigDecimal valorInicial,
                                     String dataAlvo, String descricao) {
        this.nome = nome;
        this.valorObjetivo = valorObjetivo;
        this.valorInicial = valorInicial;
        this.dataAlvo = dataAlvo;
        this.descricao = descricao;
    }

    public void atualizarValorAtual(BigDecimal valorAtual) {
        this.valorAtual = valorAtual;
    }

    /** Valor efetivo restante considerando o valor inicial já deduzido. */
    public BigDecimal getValorEfetivo() {
        return valorObjetivo.subtract(valorInicial).max(BigDecimal.ZERO);
    }
}
