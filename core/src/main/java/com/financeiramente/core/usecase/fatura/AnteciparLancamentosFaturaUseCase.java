package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.util.DomainException;

import java.time.YearMonth;

/**
 * Antecipa os lançamentos de uma fatura futura para a fatura corrente (aberta) do mesmo cartão.
 *
 * Regras:
 * - A fatura alvo deve ser "futura" (mes > mês corrente da fatura atual aberta).
 * - Deve existir uma fatura aberta para o cartão.
 * - A fatura futura não pode estar PAGA ou PAGA_PARCIAL.
 */
public class AnteciparLancamentosFaturaUseCase {

    private final FaturaRepository faturaRepository;
    private final LancamentoRepository lancamentoRepository;

    public AnteciparLancamentosFaturaUseCase(FaturaRepository faturaRepository,
                                              LancamentoRepository lancamentoRepository) {
        this.faturaRepository = faturaRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    /**
     * Move todos os lançamentos da {@code faturaFuturaId} para a fatura aberta atual do mesmo cartão.
     *
     * @param faturaFuturaId ID da fatura futura cujos lançamentos serão antecipados.
     * @return A fatura de destino (a fatura aberta atual).
     */
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
