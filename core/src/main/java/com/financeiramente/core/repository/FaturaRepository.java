package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FaturaRepository {
    void salvar(Fatura fatura);
    void atualizar(Fatura fatura);
    void deletar(String id);
    Optional<Fatura> buscarPorId(String id);
    Optional<Fatura> buscarPorCartaoEMes(String cartaoId, String mes);
    List<Fatura> listarPorCartao(String cartaoId);
    List<Fatura> listarAbertas();
    Optional<Fatura> buscarFaturaAbertaPorCartao(String cartaoId);
    boolean existeFaturaComStatus(String cartaoId, StatusFatura status);
    BigDecimal somarTotalUtilizadoPorCartao(String cartaoId);
    BigDecimal somarValorPagoPorDataDePagamento(int ano, int mes);
}
