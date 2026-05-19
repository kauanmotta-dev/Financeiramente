package com.financeiramente.core.usecase;

import com.financeiramente.core.repository.LancamentoRecorrenteRepository;

public class DesativarLancamentoRecorrenteUseCase {

    private final LancamentoRecorrenteRepository repository;

    public DesativarLancamentoRecorrenteUseCase(LancamentoRecorrenteRepository repository) {
        this.repository = repository;
    }

    public void executar(String id) {
        repository.desativar(id);
    }
}
