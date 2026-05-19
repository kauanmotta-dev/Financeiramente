package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.PlanejamentoCategoria;
import com.financeiramente.core.domain.entity.PlanejamentoMensal;
import com.financeiramente.core.repository.PlanejamentoRepository;
import com.financeiramente.core.util.DomainException;

import java.util.Optional;
import java.util.UUID;

public class DefinirLimiteCategoriaUseCase {

    private final PlanejamentoRepository planejamentoRepository;

    public DefinirLimiteCategoriaUseCase(PlanejamentoRepository planejamentoRepository) {
        this.planejamentoRepository = planejamentoRepository;
    }

    public PlanejamentoCategoria executar(PlanejamentoMensal plano, String categoriaId, double limite) {
        if (plano == null) {
            throw new DomainException("Planejamento não informado.");
        }
        if (categoriaId == null || categoriaId.trim().isEmpty()) {
            throw new DomainException("Categoria não informada.");
        }
        if (limite < 0) {
            throw new DomainException("Limite não pode ser negativo.");
        }

        Optional<PlanejamentoCategoria> existente =
                planejamentoRepository.buscarItemPorCategoria(plano.getId(), categoriaId);

        if (existente.isPresent()) {
            PlanejamentoCategoria item = existente.get();
            item.setLimite(limite);
            planejamentoRepository.atualizarItemCategoria(item);
            return item;
        } else {
            PlanejamentoCategoria novoItem = new PlanejamentoCategoria(
                    UUID.randomUUID().toString(),
                    plano.getId(),
                    categoriaId,
                    limite
            );
            planejamentoRepository.salvarItemCategoria(novoItem);
            return novoItem;
        }
    }
}
