package com.financeiramente.core.usecase.fatura;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoInput;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoUseCase;
import com.financeiramente.core.util.DomainException;
import com.financeiramente.core.util.FinanceCalculator;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;

public class PagarFaturaUseCase {

    private final FaturaRepository faturaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final RegistrarLancamentoUseCase registrarLancamento;
    private final ResolverFaturaParaLancamentoUseCase resolverFatura;
    private final DatabaseDriver databaseDriver;

    public PagarFaturaUseCase(FaturaRepository faturaRepository,
                               LancamentoRepository lancamentoRepository,
                               RegistrarLancamentoUseCase registrarLancamento,
                               ResolverFaturaParaLancamentoUseCase resolverFatura,
                               DatabaseDriver databaseDriver) {
        this.faturaRepository = faturaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.registrarLancamento = registrarLancamento;
        this.resolverFatura = resolverFatura;
        this.databaseDriver = databaseDriver;
    }

    public PagamentoFaturaResult executar(String faturaId, double valorFatura, double valorPago) {
        if (valorFatura <= 0) throw new DomainException("Valor da fatura deve ser maior que zero.");
        if (valorPago <= 0)   throw new DomainException("Valor pago deve ser maior que zero.");

        Fatura fatura = faturaRepository.buscarPorId(faturaId)
                .orElseThrow(() -> new DomainException("Fatura não encontrada."));

        if (fatura.getStatus() == StatusFatura.PAGO) {
            throw new DomainException("Fatura já foi paga integralmente.");
        }

        databaseDriver.beginTransaction();
        try {
            String hoje = LocalDate.now().toString();

            // 1. Delta: diferença entre o extrato declarado e os lançamentos já registrados
            double somaExistente = lancamentoRepository.somarPorFatura(faturaId);
            double delta = valorFatura - somaExistente;
            if (delta > 0) {
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
            PagamentoFaturaResult result = FinanceCalculator.calcularPagamentoFatura(valorFatura, valorPago);

            // 3. Lançamento de pagamento (débito na conta corrente — não vinculado à fatura)
            double valorEfetivamentePago = Math.min(valorPago, valorFatura);
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

            databaseDriver.commitTransaction();
            return result;
        } catch (Exception e) {
            databaseDriver.rollbackTransaction();
            throw e;
        }
    }
}
