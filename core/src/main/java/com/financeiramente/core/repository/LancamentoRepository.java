package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.Lancamento;
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
    double somarPorTipoEMes(TipoLancamento tipo, int ano, int mes);
    double somarPorCategoria(String categoriaId, int ano, int mes);
    boolean existePorRecorrenteEMes(String recorrenteId, int ano, int mes);
    boolean existePorCategoria(String categoriaId);
}
