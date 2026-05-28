package com.financeiramente.core.usecase.categoria;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.util.DomainException;

import java.util.List;

public class ReordenarCategoriasUseCase {

    private final CategoriaRepository categoriaRepository;

    public ReordenarCategoriasUseCase(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public void executar(List<String> idsOrdenados) {
        for (int i = 0; i < idsOrdenados.size(); i++) {
            final int posicao = i;
            Categoria categoria = categoriaRepository.buscarPorId(idsOrdenados.get(i))
                    .orElseThrow(() -> new DomainException("Categoria não encontrada na reordenação."));
            categoria.reordenar(posicao);
            categoriaRepository.atualizar(categoria);
        }
    }
}
