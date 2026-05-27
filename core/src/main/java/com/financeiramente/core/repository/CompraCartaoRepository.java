package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.CompraCartao;

import java.util.List;
import java.util.Optional;

public interface CompraCartaoRepository {
    void salvar(CompraCartao compra);
    void atualizar(CompraCartao compra);
    void deletar(String id);
    Optional<CompraCartao> buscarPorId(String id);
    List<CompraCartao> listarPorCartao(String cartaoId);
    List<CompraCartao> listarRecorrentesAtivos();
    boolean existeLancamentoRecorrenteNaFatura(String compraCartaoId, String faturaId);
    boolean existeLancamentoRecorrenteNoMes(String compraCartaoId, String mes); // mes no formato YYYY-MM
}