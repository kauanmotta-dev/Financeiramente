package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import java.util.List;
import java.util.Optional;

public interface LancamentoRepository {
    void salvar(Lancamento lancamento);
    void atualizar(Lancamento lancamento);
    void deletar(String id);
    Optional<Lancamento> buscarPorId(String id);
    List<Lancamento> listarPorMes(int ano, int mes);
    List<Lancamento> listarPorCategoria(String categoriaId, int ano, int mes);
    List<Lancamento> listarPorTag(String tagId, int ano, int mes);
    List<Lancamento> listarPorPeriodo(String dataInicio, String dataFim);
    List<Lancamento> listarPorFatura(String faturaId);
    List<Lancamento> listarPorCompraCartao(String compraCartaoId);
    double somarPorTipoEMes(TipoLancamento tipo, int ano, int mes);
    double somarPorCategoria(String categoriaId, int ano, int mes);
    double somarPorFatura(String faturaId);
    boolean existePorCategoria(String categoriaId);
    double somarDespesasSemFaturaPorMes(int ano, int mes);
    double somarDespesasPorTipoCategoriaEMes(TipoCategoria tipo, int ano, int mes);
}
