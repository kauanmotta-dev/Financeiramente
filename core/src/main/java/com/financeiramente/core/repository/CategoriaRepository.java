package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.Categoria;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {
    void salvar(Categoria categoria);
    void atualizar(Categoria categoria);
    void deletar(String id);
    Optional<Categoria> buscarPorId(String id);
    List<Categoria> listarRaizes();
    List<Categoria> listarFilhas(String paiId);
    List<Categoria> listarTodas();
}
