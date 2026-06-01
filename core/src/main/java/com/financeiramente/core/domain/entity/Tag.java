package com.financeiramente.core.domain.entity;

public class Tag {
    public static final String DEFAULT_EMOJI = "🏷️";
    public static final String DEFAULT_COLOR = "#6366F1";

    private final String id;
    private String nome;
    private String emoji;
    private String cor;
    private long criadoEm;

    public Tag(String id, String nome, long criadoEm) {
        this(id, nome, DEFAULT_EMOJI, DEFAULT_COLOR, criadoEm);
    }

    public Tag(String id, String nome, String emoji, String cor, long criadoEm) {
        this.id = id;
        this.nome = nome;
        this.emoji = emoji;
        this.cor = cor;
        this.criadoEm = criadoEm;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
    public long getCriadoEm() { return criadoEm; }
}
