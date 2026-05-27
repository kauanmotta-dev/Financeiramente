package com.financeiramente.core.usecase.categoria;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.util.DomainException;

import java.util.UUID;

public class CriarCategoriaUseCase {

    private final CategoriaRepository categoriaRepository;

    public CriarCategoriaUseCase(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Categoria executar(String nome, TipoCategoria tipo, String paiId, Double limiteMensal,
                              String icone, String cor) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DomainException("Nome da categoria é obrigatório.");
        }
        if (limiteMensal != null && limiteMensal <= 0) {
            throw new DomainException("Limite mensal deve ser positivo.");
        }

        if (paiId != null) {
            Categoria pai = categoriaRepository.buscarPorId(paiId)
                    .orElseThrow(() -> new DomainException("Categoria pai não encontrada."));
            if (pai.getPaiId() != null) {
                throw new DomainException(
                        "Não é permitido criar subcategoria de uma subcategoria (máximo 2 níveis).");
            }
            
           tipo = pai.getTipo();
            if (limiteMensal != null && pai.getLimiteMensal() != null) {
                double limiteUtilizado = categoriaRepository.listarFilhas(paiId).stream()
                        .mapToDouble(filha -> filha.getLimiteMensal() != null ? filha.getLimiteMensal() : 0.0)
                        .sum();
                if (limiteMensal + limiteUtilizado > pai.getLimiteMensal()) {
                    throw new DomainException(
                            "Limite das subcategorias não podem ultrapassar o limite da categoria pai (R$ "
                            + String.format("%.2f", pai.getLimiteMensal()) + ").");
                }
            }
        }

        int ordem = paiId == null
                ? categoriaRepository.listarRaizes().size()
                : categoriaRepository.listarFilhas(paiId).size();

        Categoria categoria = Categoria.builder(UUID.randomUUID().toString())
                .nome(nome.trim())
                .paiId(paiId)
                .tipo(tipo)
                .limiteMensal(limiteMensal)
                .ordem(ordem)
                .criadoEm(System.currentTimeMillis())
                .icone(icone != null ? icone : "\uD83D\uDCE6")
                .cor(cor != null ? cor : "#6366F1")
                .build();

        categoriaRepository.salvar(categoria);
        return categoria;
    }
}
