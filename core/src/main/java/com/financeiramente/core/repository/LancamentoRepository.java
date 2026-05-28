package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.db.Pagina;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import java.util.Optional;

public interface LancamentoRepository {
    void salvar(Lancamento lancamento);
    void atualizar(Lancamento lancamento);
    void deletar(String id);
    Optional<Lancamento> buscarPorId(String id);

    List<Lancamento> listarPorMes(int ano, int mes);
    List<Lancamento> listarPorMes(int ano, int mes, Pagina pagina);

    List<Lancamento> listarPorCategoria(String categoriaId, int ano, int mes);
    List<Lancamento> listarPorCategoria(String categoriaId, int ano, int mes, Pagina pagina);

    List<Lancamento> listarPorTag(String tagId, int ano, int mes);
    List<Lancamento> listarPorTag(String tagId, int ano, int mes, Pagina pagina);

    List<Lancamento> listarPorPeriodo(String dataInicio, String dataFim);
    List<Lancamento> listarPorPeriodo(String dataInicio, String dataFim, Pagina pagina);

    List<Lancamento> listarPorPeriodoComFiltros(String dataInicio,
                                                String dataFim,
                                                String categoriaId,
                                                TipoLancamento tipo,
                                                String tagId);
    List<Lancamento> listarPorPeriodoComFiltros(String dataInicio,
                                                String dataFim,
                                                String categoriaId,
                                                TipoLancamento tipo,
                                                String tagId,
                                                Pagina pagina);

    List<Lancamento> listarPorFatura(String faturaId);
    List<Lancamento> listarPorFatura(String faturaId, Pagina pagina);

    List<Lancamento> listarPorCompraCartao(String compraCartaoId);
    List<Lancamento> listarPorCompraCartao(String compraCartaoId, Pagina pagina);
    BigDecimal somarPorTipoEMes(TipoLancamento tipo, int ano, int mes);
    BigDecimal somarPorCategoria(String categoriaId, int ano, int mes);
    BigDecimal somarPorFatura(String faturaId);
    boolean existePorCategoria(String categoriaId);
    BigDecimal somarDespesasSemFaturaPorMes(int ano, int mes);
    BigDecimal somarDespesasPorTipoCategoriaEMes(TipoCategoria tipo, int ano, int mes);
    Map<String, BigDecimal> somarDespesasPorCategoriaEPeriodo(String dataInicio, String dataFim);
    Map<String, BigDecimal> somarDespesasPorCategoriaEPeriodo(String dataInicio,
                                                              String dataFim,
                                                              String categoriaId,
                                                              TipoLancamento tipo,
                                                              String tagId);
    int contarPorMes(int ano, int mes);
    int contarPorPeriodo(String dataInicio, String dataFim);
    int contarPorCategoria(String categoriaId, int ano, int mes);
}
