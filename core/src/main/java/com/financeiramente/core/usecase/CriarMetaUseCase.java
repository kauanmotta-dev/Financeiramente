package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;

import java.util.UUID;

public class CriarMetaUseCase {

    private final MetaRepository repository;

    public CriarMetaUseCase(MetaRepository repository) {
        this.repository = repository;
    }

    public Meta executar(String nome, double valorObjetivo, String dataAlvo, String descricao) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DomainException("Nome da meta é obrigatório.");
        }
        if (valorObjetivo <= 0) {
            throw new DomainException("Valor objetivo deve ser maior que zero.");
        }

        Meta meta = new Meta(
                UUID.randomUUID().toString(),
                nome.trim(),
                valorObjetivo,
                0.0,
                dataAlvo,
                descricao,
                true,
                System.currentTimeMillis()
        );
        repository.salvar(meta);
        return meta;
    }
}
