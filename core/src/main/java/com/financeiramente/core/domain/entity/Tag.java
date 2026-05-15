package com.financeiramente.core.domain.entity;

public class Tag {
    private final String id;
    private String nome;
    private long criadoEm;

    public Tag(String id, String nome, long criadoEm) {
        this.id = id;
        this.nome = nome;
        this.criadoEm = criadoEm;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public long getCriadoEm() { return criadoEm; }
}
