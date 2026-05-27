package com.financeiramente.core.usecase.cartao;

import com.financeiramente.core.domain.vo.BandeiraCartao;

public class EditarCartaoCreditoInput {
    private final String id;
    private final String nome;
    private final int diaVencimento;
    private final int diasParaFechamento;
    private final Double limite;
    private final BandeiraCartao bandeira;
    private final String icone;
    private final String cor;

    public EditarCartaoCreditoInput(String id, String nome, int diaVencimento,
                                    int diasParaFechamento, Double limite,
                                    BandeiraCartao bandeira, String icone, String cor) {
        this.id = id;
        this.nome = nome;
        this.diaVencimento = diaVencimento;
        this.diasParaFechamento = diasParaFechamento;
        this.limite = limite;
        this.bandeira = bandeira;
        this.icone = icone;
        this.cor = cor;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public int getDiaVencimento() { return diaVencimento; }
    public int getDiasParaFechamento() { return diasParaFechamento; }
    public Double getLimite() { return limite; }
    public BandeiraCartao getBandeira() { return bandeira; }
    public String getIcone() { return icone; }
    public String getCor() { return cor; }
}
