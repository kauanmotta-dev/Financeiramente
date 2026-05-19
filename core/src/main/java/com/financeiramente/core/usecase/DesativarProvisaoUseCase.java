package com.financeiramente.core.usecase;

import com.financeiramente.core.repository.ProvisaoRepository;

public class DesativarProvisaoUseCase {

    private final ProvisaoRepository repository;

    public DesativarProvisaoUseCase(ProvisaoRepository repository) {
        this.repository = repository;
    }

    public void executar(String id) {
        repository.desativar(id);
    }
}
