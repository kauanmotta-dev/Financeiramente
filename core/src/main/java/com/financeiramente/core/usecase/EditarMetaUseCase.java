package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.util.DomainException;

public class EditarMetaUseCase {

    private final MetaRepository repository;

    public EditarMetaUseCase(MetaRepository repository) {
        this.repository = repository;
    }

    public Meta executar(String id, String nome, double valorObjetivo, String dataAlvo, String descricao) {
        Meta meta = repository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Meta não encontrada."));

        if (nome == null || nome.trim().isEmpty()) {
            throw new DomainException("Nome da meta é obrigatório.");
        }
        if (valorObjetivo <= 0) {
            throw new DomainException("Valor objetivo deve ser maior que zero.");
        }

        meta.setNome(nome.trim());
        meta.setValorObjetivo(valorObjetivo);
        meta.setDataAlvo(dataAlvo);
        meta.setDescricao(descricao);
        repository.atualizar(meta);
        return meta;
    }
}
