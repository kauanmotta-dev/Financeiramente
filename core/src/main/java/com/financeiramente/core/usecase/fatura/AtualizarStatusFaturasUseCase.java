package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.repository.FaturaRepository;

import java.time.LocalDate;
import java.util.List;

public class AtualizarStatusFaturasUseCase {

    private final FaturaRepository faturaRepository;

    public AtualizarStatusFaturasUseCase(FaturaRepository faturaRepository) {
        this.faturaRepository = faturaRepository;
    }

    public void executar() {
        LocalDate hoje = LocalDate.now();
        List<Fatura> abertas = faturaRepository.listarAbertas();
        long now = System.currentTimeMillis();

        for (Fatura fatura : abertas) {
            LocalDate dataFechamento = LocalDate.parse(fatura.getDataFechamento());
            if (hoje.isAfter(dataFechamento)) {
                fatura.setStatus(StatusFatura.FECHADO);
                fatura.setAtualizadoEm(now);
                faturaRepository.atualizar(fatura);
            }
        }
    }
}
