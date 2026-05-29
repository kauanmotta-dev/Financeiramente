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

    /**
     * Exclui o lançamento.
     * Se o lançamento for o pagamento de uma fatura, lança {@link LancamentoPagamentoFaturaException}
     * para que a camada de apresentação possa exibir o aviso adequado.
     */
    public void executar(String lancamentoId) {
        Lancamento lancamento = lancamentoRepository.buscarPorId(lancamentoId)
                .orElseThrow(() -> new DomainException("Lançamento não encontrado."));

        Optional<String> faturaIdOpt = lancamentoRepository.buscarFaturaIdPorLancamentoPagamento(lancamentoId);
        if (faturaIdOpt.isPresent()) {
            throw new LancamentoPagamentoFaturaException(faturaIdOpt.get());
        }

        lancamentoRepository.deletar(lancamento.getId());
    }

    /**
     * Verifica se o lançamento é o pagamento de uma fatura, retornando o faturaId se for.
     */
    public Optional<String> buscarFaturaIdPorLancamentoPagamento(String lancamentoId) {
        return lancamentoRepository.buscarFaturaIdPorLancamentoPagamento(lancamentoId);
    }

    /** Exceção lançada quando se tenta excluir um lançamento que é pagamento de fatura. */
    public static final class LancamentoPagamentoFaturaException extends DomainException {
        private final String faturaId;

        public LancamentoPagamentoFaturaException(String faturaId) {
            super("Este lançamento é o pagamento de uma fatura.");
            this.faturaId = faturaId;
        }

        public String getFaturaId() { return faturaId; }
    }
}

