package com.financeiramente.core.usecase.cartao;

import com.financeiramente.core.domain.vo.BandeiraCartao;
import java.math.BigDecimal;
import java.util.List;

public class CriarCartaoCreditoInput {
    private final String nome;
    private final int diaVencimento;
    private final int diasParaFechamento;
    private final BigDecimal limite;
    private final BandeiraCartao bandeira;
    private final String icone;
    private final String cor;

    public CriarCartaoCreditoInput(String nome, int diaVencimento, int diasParaFechamento,
                                   BigDecimal limite, BandeiraCartao bandeira,
                                   String icone, String cor) {
        this.nome = nome;
        this.diaVencimento = diaVencimento;
        this.diasParaFechamento = diasParaFechamento;
        this.limite = limite;
        this.bandeira = bandeira;
        this.icone = icone;
        this.cor = cor;
    }

    public String getNome() { return nome; }
    public int getDiaVencimento() { return diaVencimento; }
    public int getDiasParaFechamento() { return diasParaFechamento; }
    public BigDecimal getLimite() { return limite; }
    public BandeiraCartao getBandeira() { return bandeira; }
    public String getIcone() { return icone; }
    public String getCor() { return cor; }
}
