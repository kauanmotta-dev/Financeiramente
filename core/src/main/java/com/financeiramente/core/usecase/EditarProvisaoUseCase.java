package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Provisao;
import com.financeiramente.core.repository.ProvisaoRepository;
import com.financeiramente.core.util.DomainException;

public class EditarProvisaoUseCase {

    private final ProvisaoRepository repository;

    public EditarProvisaoUseCase(ProvisaoRepository repository) {
        this.repository = repository;
    }

    public Provisao executar(String id, String nome, double totalAnual, double valorMensal, String categoriaId) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new DomainException("Nome da provisão é obrigatório.");
        }
        if (totalAnual <= 0) {
            throw new DomainException("Total anual deve ser maior que zero.");
        }
        if (valorMensal <= 0) {
            throw new DomainException("Valor mensal deve ser maior que zero.");
        }

        Provisao provisao = repository.buscarPorId(id)
                .orElseThrow(() -> new DomainException("Provisão não encontrada."));

        provisao.setNome(nome.trim());
        provisao.setTotalAnual(totalAnual);
        provisao.setValorMensal(valorMensal);
        provisao.setCategoriaId(categoriaId);

        repository.atualizar(provisao);
        return provisao;
    }
}
