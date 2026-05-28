package com.financeiramente.core.usecase.meta;

import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;

import java.math.BigDecimal;
import java.util.UUID;

public class CriarMetaUseCase {

    private final MetaRepository repository;

    public CriarMetaUseCase(MetaRepository repository) {
        this.repository = repository;
    }

    public Meta executar(String nome, double valorObjetivo, double valorInicial,
                         String dataAlvo, String descricao) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DomainException("Nome da meta é obrigatório.");
        }
        if (valorObjetivo <= 0) {
            throw new DomainException("Valor objetivo deve ser maior que zero.");
        }
        if (valorInicial < 0) {
            throw new DomainException("Valor inicial não pode ser negativo.");
        }
        if (valorInicial >= valorObjetivo) {
            throw new DomainException("Valor inicial deve ser menor que o valor objetivo.");
        }

        Meta meta = new Meta(
                UUID.randomUUID().toString(),
                nome.trim(),
                BigDecimal.valueOf(valorObjetivo),
                BigDecimal.ZERO,
                BigDecimal.valueOf(valorInicial),
                dataAlvo,
                descricao,
                System.currentTimeMillis()
        );
        repository.salvar(meta);
        return meta;
    }
}
