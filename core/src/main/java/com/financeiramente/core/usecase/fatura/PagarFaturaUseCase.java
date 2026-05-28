package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.db.TransactionManager;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoInput;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoUseCase;
import com.financeiramente.core.util.DomainException;
import com.financeiramente.core.util.FinanceCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;

public class PagarFaturaUseCase {

    private final FaturaRepository faturaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final RegistrarLancamentoUseCase registrarLancamento;
    private final ResolverFaturaParaLancamentoUseCase resolverFatura;
    private final TransactionManager transactionManager;

    public PagarFaturaUseCase(FaturaRepository faturaRepository,
                               LancamentoRepository lancamentoRepository,
                               RegistrarLancamentoUseCase registrarLancamento,
                               ResolverFaturaParaLancamentoUseCase resolverFatura,
                               TransactionManager transactionManager) {
        this.faturaRepository = faturaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.registrarLancamento = registrarLancamento;
        this.resolverFatura = resolverFatura;
        this.transactionManager = transactionManager;
    }

    public PagamentoFaturaResult executar(String faturaId, double valorFatura, double valorPago) {
        if (valorFatura <= 0) throw new DomainException("Valor da fatura deve ser maior que zero.");
        if (valorPago <= 0)   throw new DomainException("Valor pago deve ser maior que zero.");

        Fatura fatura = faturaRepository.buscarPorId(faturaId)
                .orElseThrow(() -> new DomainException("Fatura não encontrada."));

        if (fatura.getStatus() == StatusFatura.PAGO) {
            throw new DomainException("Fatura já foi paga integralmente.");
        }

        final PagamentoFaturaResult[] resultado = new PagamentoFaturaResult[1];

        transactionManager.executeInTransaction(() -> {
            String hoje = LocalDate.now().toString();
            BigDecimal bdFatura = BigDecimal.valueOf(valorFatura);
            BigDecimal bdPago   = BigDecimal.valueOf(valorPago);

            // 1. Delta: diferença entre o extrato declarado e os lançamentos já registrados
            BigDecimal somaExistente = lancamentoRepository.somarPorFatura(faturaId);
            BigDecimal delta = bdFatura.subtract(somaExistente);
            if (delta.compareTo(BigDecimal.ZERO) > 0) {
                RegistrarLancamentoInput encargosInput = new RegistrarLancamentoInput(
                        delta,
                        TipoLancamento.DESPESA,
                        hoje,
                        "Ajuste de fatura",
                        null,
                        faturaId,
                        Collections.emptyList());
                registrarLancamento.executar(encargosInput);
            }

            // 2. Calcular resultado do pagamento
            PagamentoFaturaResult result = FinanceCalculator.calcularPagamentoFatura(bdFatura, bdPago);

            // 3. Lançamento de pagamento (débito na conta corrente — não vinculado à fatura)
            BigDecimal valorEfetivamentePago = bdPago.min(bdFatura);
            RegistrarLancamentoInput pagamentoInput = new RegistrarLancamentoInput(
                    valorEfetivamentePago,
                    TipoLancamento.DESPESA,
                    hoje,
                    "Pagamento de fatura",
                    null,
                    null,   // sem faturaId: não incrementa total da fatura
                    Collections.emptyList());
            registrarLancamento.executar(pagamentoInput);

            // 4. Atualizar fatura
            long now = System.currentTimeMillis();
            fatura.setValorPago(valorEfetivamentePago);
            fatura.setStatus(result.getStatusResultante());
            fatura.setAtualizadoEm(now);
            faturaRepository.atualizar(fatura);

            // 5. Rollover na próxima fatura se pagamento parcial
            if (result.getStatusResultante() == StatusFatura.PAGO_PARCIAL) {
                String proximoMesDia1 = YearMonth.parse(fatura.getMes()).plusMonths(1).atDay(1).toString();
                Fatura proximaFatura = resolverFatura.executar(fatura.getCartaoId(), proximoMesDia1);

                RegistrarLancamentoInput rolloverInput = new RegistrarLancamentoInput(
                        result.getSaldoDevedor(),
                        TipoLancamento.DESPESA,
                        proximaFatura.getDataVencimento(),
                        "Saldo anterior (" + fatura.getMes() + ")",
                        null,
                        proximaFatura.getId(),
                        Collections.emptyList());
                registrarLancamento.executar(rolloverInput);
            }

            resultado[0] = result;
        });

        return resultado[0];
    }
}
