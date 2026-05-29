package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.db.TransactionManager;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;
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
import java.time.format.TextStyle;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Locale;

public class PagarFaturaUseCase {

    static final String DESCRICAO_AJUSTE_FATURA = "Ajuste de fatura";
    static final String PREFIXO_SALDO_ANTERIOR = "Saldo anterior (";

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
            BigDecimal valorTotalDeclarado = BigDecimal.valueOf(valorFatura);
            BigDecimal valorPagoInformado = BigDecimal.valueOf(valorPago);

            // 1. Delta: diferença entre o extrato declarado e os lançamentos já registrados
            BigDecimal somaExistente = lancamentoRepository.somarPorFatura(faturaId);
            BigDecimal delta = valorTotalDeclarado.subtract(somaExistente);
            if (delta.compareTo(BigDecimal.ZERO) > 0) {
                RegistrarLancamentoInput encargosInput = new RegistrarLancamentoInput(
                        delta,
                        TipoLancamento.DESPESA,
                        hoje,
                        DESCRICAO_AJUSTE_FATURA,
                        null,
                        faturaId,
                        null,
                    Collections.emptyList(), 1);
                registrarLancamento.executar(encargosInput);
            }

            // 2. Calcular resultado do pagamento
        PagamentoFaturaResult result = FinanceCalculator.calcularPagamentoFatura(
            valorTotalDeclarado,
            valorPagoInformado);

                YearMonth competencia = YearMonth.parse(fatura.getMes());
                String mesAbreviado = competencia.getMonth()
                    .getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"))
                    .replace(".", "")
                    .toUpperCase(Locale.ROOT);
                if (mesAbreviado.length() > 3) {
                mesAbreviado = mesAbreviado.substring(0, 3);
                }
                String tipoPagamento = result.getStatusResultante() == StatusFatura.PAGO ? "Total" : "Parcial";
                String descricaoPagamento = String.format(
                    Locale.ROOT,
                    "Pagamento %s %s %02d",
                    tipoPagamento,
                    mesAbreviado,
                    competencia.getYear() % 100);

            // 3. Lançamento de pagamento (débito na conta corrente — não vinculado à fatura)
        BigDecimal valorEfetivamentePago = valorPagoInformado.min(valorTotalDeclarado);
            RegistrarLancamentoInput pagamentoInput = new RegistrarLancamentoInput(
                    valorEfetivamentePago,
                    TipoLancamento.DESPESA,
                    hoje,
                    descricaoPagamento,
                    null,
                    null,   // sem faturaId: não incrementa total da fatura
                    null,
                    Collections.emptyList(), 1);
            Lancamento lancamentoPagamento = registrarLancamento.executar(pagamentoInput);

            // 4. Atualizar fatura com lancamento_pagamento_id
            long now = System.currentTimeMillis();
            fatura.setValorPago(valorEfetivamentePago);
            fatura.setStatus(result.getStatusResultante());
            fatura.setLancamentoPagamentoId(lancamentoPagamento.getId());
            fatura.setAtualizadoEm(now);
            faturaRepository.atualizar(fatura);

            // 5. Rollover na próxima fatura se pagamento parcial
            if (result.getStatusResultante() == StatusFatura.PAGO_PARCIAL) {
                String proximoMes = YearMonth.parse(fatura.getMes()).plusMonths(1).toString();
                Fatura proximaFatura = resolverFatura.executarPorMes(fatura.getCartaoId(), proximoMes);

                RegistrarLancamentoInput rolloverInput = new RegistrarLancamentoInput(
                        result.getSaldoDevedor(),
                        TipoLancamento.DESPESA,
                        proximaFatura.getDataVencimento(),
                    descricaoSaldoAnterior(fatura.getMes()),
                        null,
                        proximaFatura.getId(),
                        null,
                    Collections.emptyList(), 1);
                registrarLancamento.executar(rolloverInput);
            }

            resultado[0] = new PagamentoFaturaResult(result.getStatusResultante(), result.getSaldoDevedor(), lancamentoPagamento.getId());
        });

        return resultado[0];
    }

    static String descricaoSaldoAnterior(String mesFaturaOrigem) {
        return PREFIXO_SALDO_ANTERIOR + mesFaturaOrigem + ")";
    }
}
