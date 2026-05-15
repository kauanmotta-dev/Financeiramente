package com.financeiramente.core.domain.entity;

public class PlanejamentoMensal {
    private final String id;
    private int ano;
    private int mes;
    private double receitaEsperada;
    private double reservaImprevisto;
    private boolean padrao;
    private boolean confirmado;
    private long criadoEm;

    public PlanejamentoMensal(String id, int ano, int mes, double receitaEsperada,
                               double reservaImprevisto, boolean padrao,
                               boolean confirmado, long criadoEm) {
        this.id = id;
        this.ano = ano;
        this.mes = mes;
        this.receitaEsperada = receitaEsperada;
        this.reservaImprevisto = reservaImprevisto;
        this.padrao = padrao;
        this.confirmado = confirmado;
        this.criadoEm = criadoEm;
    }

    public String getId() { return id; }
    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }
    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }
    public double getReceitaEsperada() { return receitaEsperada; }
    public void setReceitaEsperada(double receitaEsperada) { this.receitaEsperada = receitaEsperada; }
    public double getReservaImprevisto() { return reservaImprevisto; }
    public void setReservaImprevisto(double reservaImprevisto) { this.reservaImprevisto = reservaImprevisto; }
    public boolean isPadrao() { return padrao; }
    public void setPadrao(boolean padrao) { this.padrao = padrao; }
    public boolean isConfirmado() { return confirmado; }
    public void setConfirmado(boolean confirmado) { this.confirmado = confirmado; }
    public long getCriadoEm() { return criadoEm; }
}
