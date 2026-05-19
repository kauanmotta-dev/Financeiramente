package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Provisao;
import com.financeiramente.core.repository.ProvisaoRepository;

import java.util.List;

public class CreditarProvisoesMensaisUseCase {

    private final ProvisaoRepository repository;

    public CreditarProvisoesMensaisUseCase(ProvisaoRepository repository) {
        this.repository = repository;
    }

    public void executar() {
        List<Provisao> ativas = repository.listarAtivas();
        for (Provisao provisao : ativas) {
            double novoSaldo = provisao.getSaldoAcumulado() + provisao.getValorMensal();
            repository.atualizarSaldo(provisao.getId(), novoSaldo);
        }
    }
}
