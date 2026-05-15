package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.Meta;
import java.util.List;
import java.util.Optional;

public interface MetaRepository {
    void salvar(Meta meta);
    void atualizar(Meta meta);
    void desativar(String id);
    Optional<Meta> buscarPorId(String id);
    List<Meta> listarAtivas();
}
