package com.financeiramente.core.domain.entity;

import com.financeiramente.core.domain.vo.TipoCategoria;

public class Categoria {
    private final String id;
    private String nome;
    private String paiId;
    private TipoCategoria tipo;
    private Double limiteMensal;
    private int ordem;
    private long criadoEm;
    private String icone;
    private String cor;

    public Categoria(String id, String nome, String paiId, TipoCategoria tipo,
                     Double limiteMensal, int ordem, long criadoEm, String icone, String cor) {
        this.id = id;
        this.nome = nome;
        this.paiId = paiId;
        this.tipo = tipo;
        this.limiteMensal = limiteMensal;
        this.ordem = ordem;
        this.criadoEm = criadoEm;
        this.icone = icone != null ? icone : "\uD83D\uDCE6";
        this.cor = cor != null ? cor : "#6366F1";
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getPaiId() { return paiId; }
    public void setPaiId(String paiId) { this.paiId = paiId; }
    public TipoCategoria getTipo() { return tipo; }
    public void setTipo(TipoCategoria tipo) { this.tipo = tipo; }
    public Double getLimiteMensal() { return limiteMensal; }
    public void setLimiteMensal(Double limiteMensal) { this.limiteMensal = limiteMensal; }
    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }
    public long getCriadoEm() { return criadoEm; }
    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public static Builder builder(String id) { return new Builder(id); }

    public static final class Builder {
        private final String id;
        private String nome;
        private String paiId;
        private TipoCategoria tipo;
        private Double limiteMensal;
        private int ordem = 0;
        private long criadoEm = System.currentTimeMillis();
        private String icone = "\uD83D\uDCE6";
        private String cor = "#6366F1";

        private Builder(String id) { this.id = id; }

        public Builder nome(String nome) { this.nome = nome; return this; }
        public Builder paiId(String paiId) { this.paiId = paiId; return this; }
        public Builder tipo(TipoCategoria tipo) { this.tipo = tipo; return this; }
        public Builder limiteMensal(Double limiteMensal) { this.limiteMensal = limiteMensal; return this; }
        public Builder ordem(int ordem) { this.ordem = ordem; return this; }
        public Builder criadoEm(long criadoEm) { this.criadoEm = criadoEm; return this; }
        public Builder icone(String icone) { this.icone = icone; return this; }
        public Builder cor(String cor) { this.cor = cor; return this; }

        public Categoria build() {
            return new Categoria(id, nome, paiId, tipo, limiteMensal, ordem, criadoEm, icone, cor);
        }
    }
}
