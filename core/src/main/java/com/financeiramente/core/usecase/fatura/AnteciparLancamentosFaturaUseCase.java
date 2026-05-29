package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.util.DomainException;

import java.time.YearMonth;









public class AnteciparLancamentosFaturaUseCase {

    private final FaturaRepository faturaRepository;
    private final LancamentoRepository lancamentoRepository;

    public AnteciparLancamentosFaturaUseCase(FaturaRepository faturaRepository,
                                              LancamentoRepository lancamentoRepository) {
        this.faturaRepository = faturaRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    





    public Fatura executar(String faturaFuturaId) {
        Fatura faturaFutura = faturaRepository.buscarPorId(faturaFuturaId)
                .orElseThrow(() -> new DomainException("Fatura não encontrada."));

        if (faturaFutura.getStatus() == StatusFatura.PAGO
                || faturaFutura.getStatus() == StatusFatura.PAGO_PARCIAL) {
            throw new DomainException("Não é possível antecipar lançamentos de uma fatura já paga.");
        }

        Fatura faturaAtual = faturaRepository.buscarFaturaAbertaPorCartao(faturaFutura.getCartaoId())
                .orElseThrow(() -> new DomainException(
                        "Nenhuma fatura aberta encontrada para o cartão. Abra a fatura do mês atual primeiro."));

        YearMonth mesFutura = YearMonth.parse(faturaFutura.getMes());
        YearMonth mesAtual  = YearMonth.parse(faturaAtual.getMes());

        if (!mesFutura.isAfter(mesAtual)) {
            throw new DomainException(
                    "A fatura selecionada não é futura em relação à fatura aberta atual (" + faturaAtual.getMes() + ").");
        }

        lancamentoRepository.transferirLancamentosDeFatura(faturaFuturaId, faturaAtual.getId());

        if (lancamentoRepository.listarPorFatura(faturaFuturaId).isEmpty()) {
            faturaRepository.deletar(faturaFuturaId);
        }

        return faturaAtual;
    }
}
