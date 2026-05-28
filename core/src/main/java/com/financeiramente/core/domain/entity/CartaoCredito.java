package com.financeiramente.core.domain.entity;

import com.financeiramente.core.domain.vo.BandeiraCartao;
import com.financeiramente.core.util.DomainException;
import java.math.BigDecimal;

public class CartaoCredito {
    private final String id;
    private String nome;
    private int diaVencimento;
    private int diasParaFechamento;
    private BigDecimal limite;
    private BandeiraCartao bandeira;
    private String icone;
    private String cor;
    private boolean ativo;
    private final long criadoEm;
    private long atualizadoEm;

    public CartaoCredito(String id, String nome, int diaVencimento, int diasParaFechamento,
                         BigDecimal limite, BandeiraCartao bandeira, String icone, String cor,
                         boolean ativo, long criadoEm, long atualizadoEm) {
        this.id = id;
        this.nome = nome;
        this.diaVencimento = diaVencimento;
        this.diasParaFechamento = diasParaFechamento;
        this.limite = limite;
        this.bandeira = bandeira;
        this.icone = icone;
        this.cor = cor;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getDiaVencimento() { return diaVencimento; }
    public void setDiaVencimento(int diaVencimento) { this.diaVencimento = diaVencimento; }
    public int getDiasParaFechamento() { return diasParaFechamento; }
    public void setDiasParaFechamento(int diasParaFechamento) { this.diasParaFechamento = diasParaFechamento; }
    public BigDecimal getLimite() { return limite; }
    public void setLimite(BigDecimal limite) { this.limite = limite; }
    public BandeiraCartao getBandeira() { return bandeira; }
    public void setBandeira(BandeiraCartao bandeira) { this.bandeira = bandeira; }
    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public long getCriadoEm() { return criadoEm; }
    public long getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(long atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public static Builder builder(String id) { return new Builder(id); }

    public static final class Builder {
        private final String id;
        private String nome;
        private int diaVencimento;
        private int diasParaFechamento = 10;
        private BigDecimal limite;
        private BandeiraCartao bandeira;
        private String icone = "\uD83D\uDCB3"; // 💳
        private String cor = "#6366F1";
        private boolean ativo = true;
        private long criadoEm = System.currentTimeMillis();
        private long atualizadoEm = System.currentTimeMillis();

        private Builder(String id) { this.id = id; }

        public Builder nome(String nome) { this.nome = nome; return this; }
        public Builder diaVencimento(int diaVencimento) { this.diaVencimento = diaVencimento; return this; }
        public Builder diasParaFechamento(int dias) { this.diasParaFechamento = dias; return this; }
        public Builder limite(BigDecimal limite) { this.limite = limite; return this; }
        public Builder bandeira(BandeiraCartao bandeira) { this.bandeira = bandeira; return this; }
        public Builder icone(String icone) { this.icone = icone; return this; }
        public Builder cor(String cor) { this.cor = cor; return this; }
        public Builder ativo(boolean ativo) { this.ativo = ativo; return this; }
        public Builder criadoEm(long criadoEm) { this.criadoEm = criadoEm; return this; }
        public Builder atualizadoEm(long atualizadoEm) { this.atualizadoEm = atualizadoEm; return this; }

        public CartaoCredito build() {
            if (nome == null || nome.trim().isEmpty()) {
                throw new DomainException("Nome do cartão não pode ser vazio.");
            }
            if (diaVencimento < 1 || diaVencimento > 28) {
                throw new DomainException("Dia de vencimento deve estar entre 1 e 28.");
            }
            if (diasParaFechamento < 1 || diasParaFechamento > 28) {
                throw new DomainException("Dias para fechamento deve estar entre 1 e 28.");
            }
            if (limite != null && limite.compareTo(BigDecimal.ZERO) <= 0) {
                throw new DomainException("Limite deve ser maior que zero.");
            }
            return new CartaoCredito(id, nome.trim(), diaVencimento, diasParaFechamento,
                    limite, bandeira, icone, cor, ativo, criadoEm, atualizadoEm);
        }
    }
}
