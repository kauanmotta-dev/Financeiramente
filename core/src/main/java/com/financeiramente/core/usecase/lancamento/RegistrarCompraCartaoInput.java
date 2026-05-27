package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.vo.TipoCompraCartao;

import java.util.Collections;
import java.util.List;

public class RegistrarCompraCartaoInput {
    private final String cartaoId;
    private final TipoCompraCartao tipo;
    private final double valorTotal;
    private final String data;
    private final String descricao;
    private final String categoriaId;
    private final List<String> tags;
    private final int numeroParcelas;
    private final Integer diaRecorrencia;

    public RegistrarCompraCartaoInput(String cartaoId,
                                      TipoCompraCartao tipo,
                                      double valorTotal,
                                      String data,
                                      String descricao,
                                      String categoriaId,
                                      List<String> tags,
                                      int numeroParcelas,
                                      Integer diaRecorrencia) {
        this.cartaoId = cartaoId;
        this.tipo = tipo;
        this.valorTotal = valorTotal;
        this.data = data;
        this.descricao = descricao;
        this.categoriaId = categoriaId;
        this.tags = tags != null ? Collections.unmodifiableList(tags) : Collections.emptyList();
        this.numeroParcelas = numeroParcelas;
        this.diaRecorrencia = diaRecorrencia;
    }

    public String getCartaoId() { return cartaoId; }
    public TipoCompraCartao getTipo() { return tipo; }
    public double getValorTotal() { return valorTotal; }
    public String getData() { return data; }
    public String getDescricao() { return descricao; }
    public String getCategoriaId() { return categoriaId; }
    public List<String> getTags() { return tags; }
    public int getNumeroParcelas() { return numeroParcelas; }
    public Integer getDiaRecorrencia() { return diaRecorrencia; }
}
