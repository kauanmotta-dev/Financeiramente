package com.financeiramente.core.domain.entity;

public class PlanejamentoCategoria {
    private final String id;
    private String planejamentoId;
    private String categoriaId;
    private double limite;

    public PlanejamentoCategoria(String id, String planejamentoId, String categoriaId, double limite) {
        this.id = id;
        this.planejamentoId = planejamentoId;
        this.categoriaId = categoriaId;
        this.limite = limite;
    }

    public String getId() { return id; }
    public String getPlanejamentoId() { return planejamentoId; }
    public void setPlanejamentoId(String planejamentoId) { this.planejamentoId = planejamentoId; }
    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }
    public double getLimite() { return limite; }
    public void setLimite(double limite) { this.limite = limite; }
}
