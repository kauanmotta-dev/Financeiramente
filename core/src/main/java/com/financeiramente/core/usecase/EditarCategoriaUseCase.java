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
        if (limiteMensal != null && limiteMensal <= 0) {
            throw new DomainException("Limite mensal deve ser positivo.");
        }

        if (categoria.getPaiId() != null) {
            Categoria pai = categoriaRepository.buscarPorId(categoria.getPaiId())
                    .orElseThrow(() -> new DomainException("Categoria pai não encontrada."));
            tipo = pai.getTipo();
            if (limiteMensal != null && pai.getLimiteMensal() != null) {
                double limiteUtilizado = categoriaRepository.listarFilhas(pai.getId()).stream()
                        .mapToDouble(filha -> filha.getLimiteMensal() != null ? filha.getLimiteMensal() : 0.0)
                        .sum();
                if (limiteMensal + limiteUtilizado > pai.getLimiteMensal()) {
                    throw new DomainException(
                            "Limite das subcategorias não podem ultrapassar o limite da categoria pai (R$ "
                            + String.format("%.2f", pai.getLimiteMensal()) + ").");
                }
            }
        } else {
            if(limiteMensal != null) {
                double limiteUtilizado = categoriaRepository.listarFilhas(categoria.getId()).stream()
                        .mapToDouble(filha -> filha.getLimiteMensal() != null ? filha.getLimiteMensal() : 0.0)
                        .sum();
                if (limiteMensal < limiteUtilizado) {
                throw new DomainException(
                        "Limite da categoria não pode ser menor que a soma dos limites das subcategorias (R$ "
                        + String.format("%.2f", limiteUtilizado) + ").");
                }
            }
        }

        categoria.setNome(nome.trim());
        categoria.setTipo(tipo);
        categoria.setLimiteMensal(limiteMensal);

        categoriaRepository.atualizar(categoria);
    }
}
