package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.Provisao;
import java.util.List;
import java.util.Optional;

public interface ProvisaoRepository {
    void salvar(Provisao provisao);
    void atualizar(Provisao provisao);
    void desativar(String id);
    Optional<Provisao> buscarPorId(String id);
    List<Provisao> listarAtivas();
    void atualizarSaldo(String id, double novoSaldo);
}
