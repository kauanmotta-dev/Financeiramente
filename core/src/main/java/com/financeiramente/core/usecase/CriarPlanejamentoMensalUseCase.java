package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.PlanejamentoCategoria;
import com.financeiramente.core.domain.entity.PlanejamentoMensal;
import com.financeiramente.core.repository.PlanejamentoRepository;
import com.financeiramente.core.util.DomainException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CriarPlanejamentoMensalUseCase {

    private final PlanejamentoRepository planejamentoRepository;

    public CriarPlanejamentoMensalUseCase(PlanejamentoRepository planejamentoRepository) {
        this.planejamentoRepository = planejamentoRepository;
    }

    public PlanejamentoMensal executar(int ano, int mes) {
        if (mes < 1 || mes > 12) {
            throw new DomainException("Mês inválido: " + mes);
        }

        Optional<PlanejamentoMensal> existente = planejamentoRepository.buscarPorMes(ano, mes);
        if (existente.isPresent()) {
            return existente.get();
        }

        Optional<PlanejamentoMensal> padrao = planejamentoRepository.buscarPadrao();

        PlanejamentoMensal novoPlano;
        if (padrao.isPresent()) {
            PlanejamentoMensal base = padrao.get();
            novoPlano = new PlanejamentoMensal(
                    UUID.randomUUID().toString(),
                    ano,
                    mes,
                    base.getReceitaEsperada(),
                    base.getReservaImprevisto(),
                    false,
                    false,
                    System.currentTimeMillis()
            );
        } else {
            novoPlano = new PlanejamentoMensal(
                    UUID.randomUUID().toString(),
                    ano,
                    mes,
                    0.0,
                    0.0,
                    false,
                    false,
                    System.currentTimeMillis()
            );
        }

        planejamentoRepository.salvar(novoPlano);

        if (padrao.isPresent()) {
            List<PlanejamentoCategoria> itensPadrao =
                    planejamentoRepository.listarItensPorPlano(padrao.get().getId());
            for (PlanejamentoCategoria item : itensPadrao) {
                PlanejamentoCategoria novoItem = new PlanejamentoCategoria(
                        UUID.randomUUID().toString(),
                        novoPlano.getId(),
                        item.getCategoriaId(),
                        item.getLimite()
                );
                planejamentoRepository.salvarItemCategoria(novoItem);
            }
        }

        return novoPlano;
    }
}
