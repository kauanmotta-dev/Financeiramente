package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.util.DomainException;

public class DeletarLancamentoUseCase {

    private final LancamentoRepository lancamentoRepository;

    public DeletarLancamentoUseCase(LancamentoRepository lancamentoRepository) {
        this.lancamentoRepository = lancamentoRepository;
    }

    public void executar(String lancamentoId) {
        Lancamento lancamento = lancamentoRepository.buscarPorId(lancamentoId)
                .orElseThrow(() -> new DomainException("Lançamento não encontrado."));

        lancamentoRepository.deletar(lancamento.getId());
    }
}
