package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Provisao;
import com.financeiramente.core.repository.ProvisaoRepository;
import com.financeiramente.core.util.DomainException;

public class DebitarProvisaoUseCase {

    private final ProvisaoRepository repository;

    public DebitarProvisaoUseCase(ProvisaoRepository repository) {
        this.repository = repository;
    }

    public void executar(String provisaoId, double valor) {
        Provisao provisao = repository.buscarPorId(provisaoId)
                .orElseThrow(() -> new DomainException("Provisão não encontrada."));
        double novoSaldo = provisao.getSaldoAcumulado() - valor;
        if (novoSaldo < 0) {
            throw new DomainException("Saldo da provisão insuficiente.");
        }
        repository.atualizarSaldo(provisaoId, novoSaldo);
    }
}
