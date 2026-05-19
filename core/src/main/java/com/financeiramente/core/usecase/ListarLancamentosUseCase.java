package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.repository.LancamentoRepository;

import java.util.List;

public class ListarLancamentosUseCase {

    private final LancamentoRepository lancamentoRepository;

    public ListarLancamentosUseCase(LancamentoRepository lancamentoRepository) {
        this.lancamentoRepository = lancamentoRepository;
    }

    public List<Lancamento> porMes(int ano, int mes) {
        return lancamentoRepository.listarPorMes(ano, mes);
    }

    public List<Lancamento> porCategoria(String categoriaId, int ano, int mes) {
        return lancamentoRepository.listarPorCategoria(categoriaId, ano, mes);
    }

    public List<Lancamento> porPeriodo(String dataInicio, String dataFim) {
        return lancamentoRepository.listarPorPeriodo(dataInicio, dataFim);
    }
}
