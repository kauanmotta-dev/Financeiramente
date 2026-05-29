package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.db.TransactionManager;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.util.DomainException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;










public class AlterarStatusFaturaUseCase {

    private final FaturaRepository faturaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final PagarFaturaUseCase pagarFatura;
    private final TransactionManager transactionManager;

    public AlterarStatusFaturaUseCase(FaturaRepository faturaRepository,
                                      LancamentoRepository lancamentoRepository,
                                      PagarFaturaUseCase pagarFatura,
                                      TransactionManager transactionManager) {
        this.faturaRepository = faturaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.pagarFatura = pagarFatura;
        this.transactionManager = transactionManager;
    }

    







    public PagamentoFaturaResult executar(String faturaId, StatusFatura novoStatus,
                                          double valorFatura, double valorPago) {
        Fatura fatura = faturaRepository.buscarPorId(faturaId)
                .orElseThrow(() -> new DomainException("Fatura não encontrada."));

        StatusFatura statusAtual = fatura.getStatus();

        if (statusAtual == novoStatus) {
            return null; 
        }

        if (novoStatus == StatusFatura.PAGO || novoStatus == StatusFatura.PAGO_PARCIAL) {
            
            if (statusAtual == StatusFatura.PAGO || statusAtual == StatusFatura.PAGO_PARCIAL) {
                throw new DomainException(
                        "Fatura já possui pagamento registrado. Reverta o pagamento antes de registrar um novo.");
            }
            
            return pagarFatura.executar(faturaId, valorFatura, valorPago);

        } else if (novoStatus == StatusFatura.FECHADO || novoStatus == StatusFatura.ABERTO) {
            
            if (statusAtual == StatusFatura.PAGO || statusAtual == StatusFatura.PAGO_PARCIAL) {
                transactionManager.executeInTransaction(() -> {
                    String lancamentoPagamentoId = removerLancamentosDerivadosDoPagamento(fatura, statusAtual);

                    if (lancamentoRepository.listarPorFatura(fatura.getId()).isEmpty()) {
                        faturaRepository.deletar(fatura.getId());
                        deletarLancamentoPagamentoSeExistir(lancamentoPagamentoId);
                        return;
                    }

                    long now = System.currentTimeMillis();
                    fatura.setStatus(novoStatus);
                    fatura.setValorPago(BigDecimal.ZERO);
                    fatura.setLancamentoPagamentoId(null);
                    fatura.setAtualizadoEm(now);
                    faturaRepository.atualizar(fatura);

                    deletarLancamentoPagamentoSeExistir(lancamentoPagamentoId);
                });
            } else {
                
                long now = System.currentTimeMillis();
                fatura.setStatus(novoStatus);
                fatura.setAtualizadoEm(now);
                faturaRepository.atualizar(fatura);
            }
            return null;
        }

        throw new DomainException("Transição de status inválida: " + statusAtual + " → " + novoStatus);
    }

    



    public void reverterPagamento(String faturaId) {
        executar(faturaId, StatusFatura.FECHADO, 0, 0);
    }

    private String removerLancamentosDerivadosDoPagamento(Fatura fatura, StatusFatura statusAtual) {
        String lancPagId = fatura.getLancamentoPagamentoId();

        List<Lancamento> lancamentosFatura = lancamentoRepository.listarPorFatura(fatura.getId());
        lancamentosFatura.stream()
                .filter(this::ehAjusteDeFatura)
                .map(Lancamento::getId)
                .forEach(lancamentoRepository::deletar);

        if (statusAtual == StatusFatura.PAGO_PARCIAL) {
            removerSaldoAnteriorDaProximaFatura(fatura);
        }

        return lancPagId;
    }

    private void deletarLancamentoPagamentoSeExistir(String lancamentoPagamentoId) {
        if (lancamentoPagamentoId != null) {
            lancamentoRepository.deletar(lancamentoPagamentoId);
        }
    }

    private void removerSaldoAnteriorDaProximaFatura(Fatura fatura) {
        String proximoMes = YearMonth.parse(fatura.getMes()).plusMonths(1).toString();
        faturaRepository.buscarPorCartaoEMes(fatura.getCartaoId(), proximoMes)
                .ifPresent(proximaFatura -> lancamentoRepository.listarPorFatura(proximaFatura.getId()).stream()
                        .filter(lancamento -> ehSaldoAnterior(lancamento, fatura.getMes()))
                        .map(Lancamento::getId)
                        .forEach(lancamentoRepository::deletar));
    }

    private boolean ehAjusteDeFatura(Lancamento lancamento) {
        return PagarFaturaUseCase.DESCRICAO_AJUSTE_FATURA.equals(lancamento.getDescricao());
    }

    private boolean ehSaldoAnterior(Lancamento lancamento, String mesFaturaOrigem) {
        return PagarFaturaUseCase.descricaoSaldoAnterior(mesFaturaOrigem).equals(lancamento.getDescricao());
    }
}
