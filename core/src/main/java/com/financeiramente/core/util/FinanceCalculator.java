package com.financeiramente.core.util;

import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.domain.vo.StatusSaldo;
import com.financeiramente.core.usecase.fatura.PagamentoFaturaResult;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class FinanceCalculator {

    private FinanceCalculator() {}

    public static double saldoCategoria(double limite, double gastoRealizado) {
        return limite - gastoRealizado;
    }

    public static StatusSaldo statusCategoria(double limite, double gasto) {
        if (limite <= 0) {
            return gasto > 0 ? StatusSaldo.VERMELHO : StatusSaldo.VERDE;
        }
        double percentual = gasto / limite;
        if (percentual < 0.75) return StatusSaldo.VERDE;
        if (percentual <= 1.00) return StatusSaldo.AMARELO;
        return StatusSaldo.VERMELHO;
    }

    public static double calcularPercentual(double parte, double total) {
        if (total <= 0.0) {
            return 0.0;
        }
        return (parte / total) * 100.0;
    }

    public static int calcularPercentualLimitado(double parte, double total) {
        return (int) Math.min(100.0, Math.max(0.0, calcularPercentual(parte, total)));
    }

    public static LocalDate projecaoConclusaoMeta(
            double valorObjetivo,
            double valorAtual,
            LocalDate dataInicio) {

        if (valorAtual <= 0 || dataInicio == null) return null;
        if (valorAtual >= valorObjetivo) return LocalDate.now();

        long mesesDesdeInicio = ChronoUnit.MONTHS.between(dataInicio, LocalDate.now());
        if (mesesDesdeInicio < 0) return null;
        else if (mesesDesdeInicio == 0) mesesDesdeInicio = 1;

        double taxaMensal = valorAtual / mesesDesdeInicio;
        if (taxaMensal <= 0) return null;

        double valorRestante = valorObjetivo - valorAtual;
        long mesesRestantes = (long) Math.ceil(valorRestante / taxaMensal);
        return LocalDate.now().plusMonths(mesesRestantes);
    }

    /**
     * Calcula o resultado de um pagamento de fatura.
     *
     * @param valorFatura total declarado pelo usuário no extrato do cartão
     * @param valorPago   valor que o usuário está pagando neste momento
     * @return PagamentoFaturaResult com status resultante e saldo devedor
     */
    public static PagamentoFaturaResult calcularPagamentoFatura(double valorFatura, double valorPago) {
        StatusFatura status = valorPago >= valorFatura ? StatusFatura.PAGO : StatusFatura.PAGO_PARCIAL;
        double saldoDevedor = Math.max(0.0, valorFatura - valorPago);
        return new PagamentoFaturaResult(status, saldoDevedor);
    }

    /**
     * Distribui um valor total em N parcelas com soma exata.
     * A diferença de centavos fica na última parcela.
     */
    public static double[] distribuirParcelas(double valorTotal, int numeroParcelas) {
        if (valorTotal <= 0) {
            throw new IllegalArgumentException("Valor total deve ser maior que zero.");
        }
        if (numeroParcelas < 2) {
            throw new IllegalArgumentException("Número de parcelas deve ser maior ou igual a 2.");
        }

        long totalCentavos = Math.round(valorTotal * 100);
        long parcelaBaseCentavos = totalCentavos / numeroParcelas;
        long restoCentavos = totalCentavos % numeroParcelas;

        double[] parcelas = new double[numeroParcelas];
        for (int i = 0; i < numeroParcelas - 1; i++) {
            parcelas[i] = parcelaBaseCentavos / 100.0;
        }
        parcelas[numeroParcelas - 1] = (parcelaBaseCentavos + restoCentavos) / 100.0;

        return parcelas;
    }
}
