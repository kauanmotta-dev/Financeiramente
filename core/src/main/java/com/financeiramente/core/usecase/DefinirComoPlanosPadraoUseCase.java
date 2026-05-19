package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.PlanejamentoMensal;
import com.financeiramente.core.repository.PlanejamentoRepository;
import com.financeiramente.core.util.DomainException;

import java.util.Optional;

public class DefinirComoPlanosPadraoUseCase {

    private final PlanejamentoRepository planejamentoRepository;

    public DefinirComoPlanosPadraoUseCase(PlanejamentoRepository planejamentoRepository) {
        this.planejamentoRepository = planejamentoRepository;
    }

    public void executar(PlanejamentoMensal plano) {
        if (plano == null) {
            throw new DomainException("Planejamento não informado.");
        }

        Optional<PlanejamentoMensal> anteriorPadrao = planejamentoRepository.buscarPadrao();
        if (anteriorPadrao.isPresent() && !anteriorPadrao.get().getId().equals(plano.getId())) {
            PlanejamentoMensal anterior = anteriorPadrao.get();
            anterior.setPadrao(false);
            planejamentoRepository.atualizar(anterior);
        }

        plano.setPadrao(true);
        planejamentoRepository.atualizar(plano);
    }
}
