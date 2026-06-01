package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.vo.TipoLancamento;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class RegistrarLancamentoInput {
    private final BigDecimal valor;
    private final TipoLancamento tipo;
    private final String data;
    private final String descricao;
    private final String categoriaId;
    private final String faturaId;
    private final String compraCartaoId;
    private final List<String> tags;
    private final int numeroParcelas;

    public RegistrarLancamentoInput(BigDecimal valor, TipoLancamento tipo, String data,
                                    String descricao, String categoriaId,
                                    String faturaId,
                                    List<String> tags) {
        this(valor, tipo, data, descricao, categoriaId, faturaId, null, tags, 1);
    }

    public RegistrarLancamentoInput(BigDecimal valor, TipoLancamento tipo, String data,
                                    String descricao, String categoriaId,
                                    String faturaId,
                                    List<String> tags, int numeroParcelas) {
        this(valor, tipo, data, descricao, categoriaId, faturaId, null, tags, numeroParcelas);
    }

    public RegistrarLancamentoInput(BigDecimal valor, TipoLancamento tipo, String data,
                                    String descricao, String categoriaId,
                                    String faturaId,
                                    String compraCartaoId,
                                    List<String> tags) {
        this(valor, tipo, data, descricao, categoriaId, faturaId, compraCartaoId, tags, 1);
    }

    public RegistrarLancamentoInput(BigDecimal valor, TipoLancamento tipo, String data,
                                    String descricao, String categoriaId,
                                    String faturaId,
                                    String compraCartaoId,
                                    List<String> tags, int numeroParcelas) {
        this.valor = valor;
        this.tipo = tipo;
        this.data = data;
        this.descricao = descricao;
        this.categoriaId = categoriaId;
        this.faturaId = faturaId;
        this.compraCartaoId = compraCartaoId;
        this.tags = tags != null ? Collections.unmodifiableList(tags) : Collections.emptyList();
        this.numeroParcelas = numeroParcelas > 0 ? numeroParcelas : 1;
    }

    public RegistrarLancamentoInput(String valor, TipoLancamento tipo, String data,
                                    String descricao, String categoriaId,
                                    String faturaId,
                                    List<String> tags) {
        this(new BigDecimal(valor), tipo, data, descricao, categoriaId, faturaId, tags);
    }

    public BigDecimal getValor() { return valor; }
    public TipoLancamento getTipo() { return tipo; }
    public String getData() { return data; }
    public String getDescricao() { return descricao; }
    public String getCategoriaId() { return categoriaId; }
    public String getFaturaId() { return faturaId; }
    public String getCompraCartaoId() { return compraCartaoId; }
    public List<String> getTags() { return tags; }
    public int getNumeroParcelas() { return numeroParcelas; }
}
