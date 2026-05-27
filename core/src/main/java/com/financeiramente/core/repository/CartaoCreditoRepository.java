package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.CartaoCredito;
import java.util.List;
import java.util.Optional;

public interface CartaoCreditoRepository {
    void salvar(CartaoCredito cartao);
    void atualizar(CartaoCredito cartao);
    Optional<CartaoCredito> buscarPorId(String id);
    List<CartaoCredito> listarAtivos();
    void desativar(String id, long atualizadoEm);
}
