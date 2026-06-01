package com.financeiramente.core.usecase.meta;

import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;

import java.math.BigDecimal;

public class EditarMetaUseCase {

    private final MetaRepository repository;

    public EditarMetaUseCase(MetaRepository repository) {
        this.repository = repository;
    }

    public Meta executar(String id, String nome, double valorObjetivo, double valorInicial,
                          String dataAlvo, String descricao) {
        Meta meta = repository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Meta não encontrada."));

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

        meta.atualizarDadosEdicao(
            nome.trim(),
            BigDecimal.valueOf(valorObjetivo),
            BigDecimal.valueOf(valorInicial),
            dataAlvo,
            descricao);
        repository.atualizar(meta);
        return meta;
    }
}
