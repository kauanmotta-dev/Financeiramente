package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.ProvisaoRepository;
import com.financeiramente.core.util.DomainException;

public class DeletarLancamentoUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final ProvisaoRepository provisaoRepository;

    public DeletarLancamentoUseCase(LancamentoRepository lancamentoRepository,
                                    ProvisaoRepository provisaoRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.provisaoRepository = provisaoRepository;
    }

    public void executar(String lancamentoId) {
        Lancamento lancamento = lancamentoRepository.buscarPorId(lancamentoId)
                .orElseThrow(() -> new DomainException("Lançamento não encontrado."));

        if (lancamento.getProvisaoId() != null) {
            provisaoRepository.buscarPorId(lancamento.getProvisaoId()).ifPresent(p ->
                    provisaoRepository.atualizarSaldo(p.getId(),
                            p.getSaldoAcumulado() + lancamento.getValor()));
        }

        lancamentoRepository.deletar(lancamentoId);
    }
}
