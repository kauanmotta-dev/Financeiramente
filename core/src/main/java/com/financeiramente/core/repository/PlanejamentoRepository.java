package com.financeiramente.core.repository;

import com.financeiramente.core.domain.entity.PlanejamentoCategoria;
import com.financeiramente.core.domain.entity.PlanejamentoMensal;
import java.util.List;
import java.util.Optional;

public interface PlanejamentoRepository {
    void salvar(PlanejamentoMensal plano);
    void atualizar(PlanejamentoMensal plano);
    Optional<PlanejamentoMensal> buscarPorMes(int ano, int mes);
    Optional<PlanejamentoMensal> buscarPadrao();
    void salvarItemCategoria(PlanejamentoCategoria item);
    void atualizarItemCategoria(PlanejamentoCategoria item);
    List<PlanejamentoCategoria> listarItensPorPlano(String planejamentoId);
    Optional<PlanejamentoCategoria> buscarItemPorCategoria(String planejamentoId, String categoriaId);
}
