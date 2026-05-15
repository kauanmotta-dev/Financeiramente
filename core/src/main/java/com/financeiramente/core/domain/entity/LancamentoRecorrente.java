package com.financeiramente.core.domain.entity;

import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.domain.vo.TipoRecorrencia;

public class LancamentoRecorrente {
    private final String id;
    private String descricao;
    private double valor;
    private TipoLancamento tipo;
    private String categoriaId;
    private TipoRecorrencia recorrencia;
    private Integer diaRecorrencia;
    private boolean ativo;
    private long criadoEm;

    public LancamentoRecorrente(String id, String descricao, double valor, TipoLancamento tipo,
                                 String categoriaId, TipoRecorrencia recorrencia,
                                 Integer diaRecorrencia, boolean ativo, long criadoEm) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.tipo = tipo;
        this.categoriaId = categoriaId;
        this.recorrencia = recorrencia;
        this.diaRecorrencia = diaRecorrencia;
        this.ativo = ativo;
        this.criadoEm = criadoEm;
    }

    public String getId() { return id; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public TipoLancamento getTipo() { return tipo; }
    public void setTipo(TipoLancamento tipo) { this.tipo = tipo; }
    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }
    public TipoRecorrencia getRecorrencia() { return recorrencia; }
    public void setRecorrencia(TipoRecorrencia recorrencia) { this.recorrencia = recorrencia; }
    public Integer getDiaRecorrencia() { return diaRecorrencia; }
    public void setDiaRecorrencia(Integer diaRecorrencia) { this.diaRecorrencia = diaRecorrencia; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public long getCriadoEm() { return criadoEm; }
}
