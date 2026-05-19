package com.financeiramente.core.usecase;

import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;

public class DesativarMetaUseCase {

    private final MetaRepository repository;

    public DesativarMetaUseCase(MetaRepository repository) {
        this.repository = repository;
    }

    public void executar(String id) {
        repository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Meta não encontrada."));
        repository.desativar(id);
    }
}
