package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.vo.TipoLancamento;
import java.util.Collections;
import java.util.List;

public class RegistrarLancamentoInput {
    private final double valor;
    private final TipoLancamento tipo;
    private final String data;
    private final String descricao;
    private final String categoriaId;
    private final String recorrenteId;
    private final List<String> tags;

    public RegistrarLancamentoInput(double valor, TipoLancamento tipo, String data,
                                    String descricao, String categoriaId,
                                    String recorrenteId,
                                    List<String> tags) {
        this.valor = valor;
        this.tipo = tipo;
        this.data = data;
        this.descricao = descricao;
        this.categoriaId = categoriaId;
        this.recorrenteId = recorrenteId;
        this.tags = tags != null ? Collections.unmodifiableList(tags) : Collections.emptyList();
    }

    public double getValor() { return valor; }
    public TipoLancamento getTipo() { return tipo; }
    public String getData() { return data; }
    public String getDescricao() { return descricao; }
    public String getCategoriaId() { return categoriaId; }
    public String getRecorrenteId() { return recorrenteId; }
    public List<String> getTags() { return tags; }
}
