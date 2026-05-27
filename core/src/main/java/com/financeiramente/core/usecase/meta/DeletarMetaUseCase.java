package com.financeiramente.core.usecase.meta;

import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;

public class DeletarMetaUseCase {

    private final MetaRepository repository;

    public DeletarMetaUseCase(MetaRepository repository) {
        this.repository = repository;
    }

    public void executar(String id) {
        repository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Meta não encontrada."));
        repository.deletar(id);
    }
}
