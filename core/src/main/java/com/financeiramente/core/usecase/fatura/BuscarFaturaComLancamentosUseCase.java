package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.util.DomainException;

import java.util.List;

public class BuscarFaturaComLancamentosUseCase {

    private final FaturaRepository faturaRepository;
    private final LancamentoRepository lancamentoRepository;

    public BuscarFaturaComLancamentosUseCase(FaturaRepository faturaRepository,
                                             LancamentoRepository lancamentoRepository) {
        this.faturaRepository = faturaRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    public FaturaDetalheResult executar(String faturaId) {
        Fatura fatura = faturaRepository.buscarPorId(faturaId)
                .orElseThrow(() -> new DomainException("Fatura não encontrada."));

        List<Lancamento> lancamentos = lancamentoRepository.listarPorFatura(faturaId);
        double total = lancamentoRepository.somarPorFatura(faturaId).doubleValue();

        return new FaturaDetalheResult(fatura, lancamentos, total);
    }
}
