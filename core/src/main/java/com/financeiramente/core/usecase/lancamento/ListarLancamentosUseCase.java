package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.db.Pagina;
import com.financeiramente.core.db.PaginaResultado;
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

    public PaginaResultado<Lancamento> porMes(int ano, int mes, Pagina pagina) {
        List<Lancamento> itens = lancamentoRepository.listarPorMes(ano, mes, pagina);
        int total = lancamentoRepository.contarPorMes(ano, mes);
        return new PaginaResultado<>(itens, total, pagina != null && pagina.getOffset() + itens.size() < total);
    }

    public List<Lancamento> porCategoria(String categoriaId, int ano, int mes) {
        return lancamentoRepository.listarPorCategoria(categoriaId, ano, mes);
    }

    public PaginaResultado<Lancamento> porCategoria(String categoriaId, int ano, int mes, Pagina pagina) {
        List<Lancamento> itens = lancamentoRepository.listarPorCategoria(categoriaId, ano, mes, pagina);
        int total = lancamentoRepository.contarPorCategoria(categoriaId, ano, mes);
        return new PaginaResultado<>(itens, total, pagina != null && pagina.getOffset() + itens.size() < total);
    }

    public List<Lancamento> porPeriodo(String dataInicio, String dataFim) {
        return lancamentoRepository.listarPorPeriodo(dataInicio, dataFim);
    }

    public PaginaResultado<Lancamento> porPeriodo(String dataInicio, String dataFim, Pagina pagina) {
        List<Lancamento> itens = lancamentoRepository.listarPorPeriodo(dataInicio, dataFim, pagina);
        int total = lancamentoRepository.contarPorPeriodo(dataInicio, dataFim);
        return new PaginaResultado<>(itens, total, pagina != null && pagina.getOffset() + itens.size() < total);
    }
}
