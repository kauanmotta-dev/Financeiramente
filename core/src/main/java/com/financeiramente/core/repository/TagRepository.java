package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.Tag;
import java.util.List;
import java.util.Optional;

public interface TagRepository {
    void salvar(Tag tag);
    void atualizar(Tag tag);
    void deletar(String id);
    List<Tag> listarTodas();
    Optional<Tag> buscarPorNome(String nome);
    void vincularLancamento(String lancamentoId, String tagId);
    void desvincularLancamento(String lancamentoId);
    List<Tag> listarPorLancamento(String lancamentoId);
}
