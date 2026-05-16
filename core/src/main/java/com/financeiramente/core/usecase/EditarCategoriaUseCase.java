package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.util.DomainException;

public class EditarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;

    public EditarCategoriaUseCase(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public void executar(String id, String nome, TipoCategoria tipo, Double limiteMensal) {
        Categoria categoria = categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Categoria não encontrada."));

        if (nome == null || nome.trim().isEmpty()) {
            throw new DomainException("Nome da categoria é obrigatório.");
        }
        if (tipo == null) {
            throw new DomainException("Tipo da categoria é obrigatório.");
        }
        if (limiteMensal != null && limiteMensal <= 0) {
            throw new DomainException("Limite mensal deve ser positivo.");
        }

        categoria.setNome(nome.trim());
        categoria.setTipo(tipo);
        categoria.setLimiteMensal(limiteMensal);

        categoriaRepository.atualizar(categoria);
    }
}
