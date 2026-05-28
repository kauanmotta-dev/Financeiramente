package com.financeiramente.core.usecase.categoria;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.util.DomainException;

import java.math.BigDecimal;

public class EditarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;

    public EditarCategoriaUseCase(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public void executar(String id, String nome, TipoCategoria tipo, BigDecimal limiteMensal,
                         String icone, String cor) {
        Categoria categoria = categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Categoria não encontrada."));

        if (nome == null || nome.trim().isEmpty()) {
            throw new DomainException("Nome da categoria é obrigatório.");
        }
        if (limiteMensal != null && limiteMensal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Limite mensal deve ser positivo.");
        }

        if (categoria.getPaiId() != null) {
            Categoria pai = categoriaRepository.buscarPorId(categoria.getPaiId())
                    .orElseThrow(() -> new DomainException("Categoria pai não encontrada."));
            tipo = pai.getTipo();
            if (limiteMensal != null && pai.getLimiteMensal() != null) {
                BigDecimal limiteUtilizado = categoriaRepository.listarFilhas(pai.getId()).stream()
                        .filter(filha -> !filha.getId().equals(categoria.getId()))
                        .map(filha -> filha.getLimiteMensal() != null ? filha.getLimiteMensal() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (limiteMensal.add(limiteUtilizado).compareTo(pai.getLimiteMensal()) > 0) {
                    throw new DomainException(
                            "Limite das subcategorias não podem ultrapassar o limite da categoria pai (R$ "
                            + String.format("%.2f", pai.getLimiteMensal()) + ").");
                }
            }
        } else {
            if (limiteMensal != null) {
                BigDecimal limiteUtilizado = categoriaRepository.listarFilhas(categoria.getId()).stream()
                        .map(filha -> filha.getLimiteMensal() != null ? filha.getLimiteMensal() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (limiteMensal.compareTo(limiteUtilizado) < 0) {
                    throw new DomainException(
                            "Limite da categoria não pode ser menor que a soma dos limites das subcategorias (R$ "
                            + String.format("%.2f", limiteUtilizado) + ").");
                }
            }
        }

        categoria.atualizarDadosEdicao(nome.trim(), tipo, limiteMensal, icone, cor);

        categoriaRepository.atualizar(categoria);
    }
}
