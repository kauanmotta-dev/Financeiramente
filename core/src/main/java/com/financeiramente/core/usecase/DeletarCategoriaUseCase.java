package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.util.DomainException;

import java.util.List;

public class DeletarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;

    public DeletarCategoriaUseCase(CategoriaRepository categoriaRepository,
                                   LancamentoRepository lancamentoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    public void executar(String id) {
        categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Categoria não encontrada."));

        List<Categoria> filhas = categoriaRepository.listarFilhas(id);
        if (!filhas.isEmpty()) {
            throw new DomainException(
                    "Não é possível excluir uma categoria que possui subcategorias.");
        }

        if (lancamentoRepository.existePorCategoria(id)) {
            throw new DomainException(
                    "Não é possível excluir uma categoria com lançamentos vinculados.");
        }

        categoriaRepository.deletar(id);
    }
}
