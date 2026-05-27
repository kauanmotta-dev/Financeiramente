package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;

import java.util.List;

public class FaturaDetalheResult {
    private final Fatura fatura;
    private final List<Lancamento> lancamentos;
    private final double totalLancamentos;

    public FaturaDetalheResult(Fatura fatura, List<Lancamento> lancamentos, double totalLancamentos) {
        this.fatura = fatura;
        this.lancamentos = lancamentos;
        this.totalLancamentos = totalLancamentos;
    }

    public Fatura getFatura() { return fatura; }
    public List<Lancamento> getLancamentos() { return lancamentos; }
    public double getTotalLancamentos() { return totalLancamentos; }
}
