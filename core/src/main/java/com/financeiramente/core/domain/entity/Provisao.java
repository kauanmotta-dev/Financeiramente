package com.financeiramente.core.domain.entity;

public class Provisao {
    private final String id;
    private String nome;
    private double totalAnual;
    private double valorMensal;
    private double saldoAcumulado;
    private String categoriaId;
    private boolean ativo;
    private long criadoEm;

    public Provisao(String id, String nome, double totalAnual, double valorMensal,
                    double saldoAcumulado, String categoriaId, boolean ativo, long criadoEm) {
        this.id = id;
        this.nome = nome;
        this.totalAnual = totalAnual;
        this.valorMensal = valorMensal;
        this.saldoAcumulado = saldoAcumulado;
        this.categoriaId = categoriaId;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getTotalAnual() { return totalAnual; }
    public void setTotalAnual(double totalAnual) { this.totalAnual = totalAnual; }
    public double getValorMensal() { return valorMensal; }
    public void setValorMensal(double valorMensal) { this.valorMensal = valorMensal; }
    public double getSaldoAcumulado() { return saldoAcumulado; }
    public void setSaldoAcumulado(double saldoAcumulado) { this.saldoAcumulado = saldoAcumulado; }
    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public long getCriadoEm() { return criadoEm; }
}
