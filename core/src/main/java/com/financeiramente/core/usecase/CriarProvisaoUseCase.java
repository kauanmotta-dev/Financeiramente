package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Provisao;
import com.financeiramente.core.repository.ProvisaoRepository;
import com.financeiramente.core.util.DomainException;

import java.util.UUID;

public class CriarProvisaoUseCase {

    private final ProvisaoRepository repository;

    public CriarProvisaoUseCase(ProvisaoRepository repository) {
        this.repository = repository;
    }

    public Provisao executar(String nome, double totalAnual, double valorMensal, String categoriaId) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DomainException("Nome da provisão é obrigatório.");
        }
        if (totalAnual <= 0) {
            throw new DomainException("Total anual deve ser maior que zero.");
        }
        if (valorMensal <= 0) {
            throw new DomainException("Valor mensal deve ser maior que zero.");
        }

        Provisao provisao = new Provisao(
                UUID.randomUUID().toString(),
                nome.trim(),
                totalAnual,
                valorMensal,
                0.0,
                categoriaId,
                true,
                System.currentTimeMillis()
        );
        repository.salvar(provisao);
        return provisao;
    }
}
