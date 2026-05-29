package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.util.DomainException;

import java.util.Optional;

public class DeletarLancamentoUseCase {

    private final LancamentoRepository lancamentoRepository;

    public DeletarLancamentoUseCase(LancamentoRepository lancamentoRepository) {
        this.lancamentoRepository = lancamentoRepository;
    }

    




    public void executar(String lancamentoId) {
        Lancamento lancamento = lancamentoRepository.buscarPorId(lancamentoId)
                .orElseThrow(() -> new DomainException("Lançamento não encontrado."));

        Optional<String> faturaIdOpt = lancamentoRepository.buscarFaturaIdPorLancamentoPagamento(lancamentoId);
        if (faturaIdOpt.isPresent()) {
            throw new LancamentoPagamentoFaturaException(faturaIdOpt.get());
        }

        lancamentoRepository.deletar(lancamento.getId());
    }

    


    public Optional<String> buscarFaturaIdPorLancamentoPagamento(String lancamentoId) {
        return lancamentoRepository.buscarFaturaIdPorLancamentoPagamento(lancamentoId);
    }

    
    public static final class LancamentoPagamentoFaturaException extends DomainException {
        private final String faturaId;

        public LancamentoPagamentoFaturaException(String faturaId) {
            super("Este lançamento é o pagamento de uma fatura.");
            this.faturaId = faturaId;
        }

        public String getFaturaId() { return faturaId; }
    }
}

