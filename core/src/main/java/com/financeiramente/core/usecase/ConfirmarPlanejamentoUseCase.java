package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.PlanejamentoMensal;
import com.financeiramente.core.repository.PlanejamentoRepository;
import com.financeiramente.core.util.DomainException;

public class ConfirmarPlanejamentoUseCase {

    private final PlanejamentoRepository planejamentoRepository;

    public ConfirmarPlanejamentoUseCase(PlanejamentoRepository planejamentoRepository) {
        this.planejamentoRepository = planejamentoRepository;
    }

    public void executar(PlanejamentoMensal plano) {
        if (plano == null) {
            throw new DomainException("Planejamento não encontrado.");
        }
        plano.setConfirmado(true);
        planejamentoRepository.atualizar(plano);
    }
}
