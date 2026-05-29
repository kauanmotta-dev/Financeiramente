package com.financeiramente.core.util;

import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.domain.vo.StatusSaldo;
import com.financeiramente.core.usecase.fatura.PagamentoFaturaResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class FinanceCalculator {

    private FinanceCalculator() {}

    public static BigDecimal saldoCategoria(BigDecimal limite, BigDecimal gastoRealizado) {
        return limite.subtract(gastoRealizado);
    }

    public static StatusSaldo statusCategoria(BigDecimal limite, BigDecimal gasto) {
        if (limite.compareTo(BigDecimal.ZERO) <= 0) {
            return gasto.compareTo(BigDecimal.ZERO) > 0 ? StatusSaldo.VERMELHO : StatusSaldo.VERDE;
        }
        BigDecimal percentual = gasto.divide(limite, 8, RoundingMode.HALF_EVEN);
        if (percentual.compareTo(new BigDecimal("0.75")) < 0) return StatusSaldo.VERDE;
        if (percentual.compareTo(BigDecimal.ONE) <= 0) return StatusSaldo.AMARELO;
        return StatusSaldo.VERMELHO;
    }

    public static BigDecimal calcularPercentual(BigDecimal parte, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(new BigDecimal("100"))
                .divide(total, 2, RoundingMode.HALF_EVEN);
    }

    public static int calcularPercentualLimitado(BigDecimal parte, BigDecimal total) {
        BigDecimal percentual = calcularPercentual(parte, total)
                .max(BigDecimal.ZERO)
                .min(new BigDecimal("100"));
        return percentual.intValue();
    }

    public static LocalDate projecaoConclusaoMeta(
            BigDecimal valorObjetivo,
            BigDecimal valorAtual,
            LocalDate dataInicio) {

        if (valorAtual.compareTo(BigDecimal.ZERO) <= 0 || dataInicio == null) return null;
        if (valorAtual.compareTo(valorObjetivo) >= 0) return LocalDate.now();

        long mesesDesdeInicio = ChronoUnit.MONTHS.between(dataInicio, LocalDate.now());
        if (mesesDesdeInicio < 0) return null;
        else if (mesesDesdeInicio == 0) mesesDesdeInicio = 1;

        BigDecimal taxaMensal = valorAtual.divide(BigDecimal.valueOf(mesesDesdeInicio), 8, RoundingMode.HALF_EVEN);
        if (taxaMensal.compareTo(BigDecimal.ZERO) <= 0) return null;

        BigDecimal valorRestante = valorObjetivo.subtract(valorAtual);
        long mesesRestantes = valorRestante.divide(taxaMensal, 0, RoundingMode.CEILING).longValue();
        return LocalDate.now().plusMonths(mesesRestantes);
    }

    /**
     * Calcula o resultado de um pagamento de fatura.
     *
     * @param valorFatura total declarado pelo usuário no extrato do cartão
     * @param valorPago   valor que o usuário está pagando neste momento
     * @return PagamentoFaturaResult com status resultante e saldo devedor
     */
    public static PagamentoFaturaResult calcularPagamentoFatura(BigDecimal valorFatura, BigDecimal valorPago) {
        StatusFatura status = valorPago.compareTo(valorFatura) >= 0 ? StatusFatura.PAGO : StatusFatura.PAGO_PARCIAL;
        BigDecimal saldoDevedor = valorFatura.subtract(valorPago).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_EVEN);
        return new PagamentoFaturaResult(status, saldoDevedor, null);
    }

    /**
     * Distribui um valor total em N parcelas com soma exata.
     * A diferença de centavos fica na última parcela.
     */
    public static BigDecimal[] distribuirParcelas(BigDecimal valorTotal, int numeroParcelas) {
        if (valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor total deve ser maior que zero.");
        }
        if (numeroParcelas < 2) {
            throw new IllegalArgumentException("Número de parcelas deve ser maior ou igual a 2.");
        }

        BigDecimal valorNormalizado = valorTotal.setScale(2, RoundingMode.HALF_EVEN);
        long totalCentavos = valorNormalizado.movePointRight(2).longValueExact();
        long parcelaBaseCentavos = totalCentavos / numeroParcelas;
        long restoCentavos = totalCentavos % numeroParcelas;

        BigDecimal[] parcelas = new BigDecimal[numeroParcelas];
        for (int i = 0; i < numeroParcelas - 1; i++) {
            parcelas[i] = BigDecimal.valueOf(parcelaBaseCentavos, 2).setScale(2, RoundingMode.HALF_EVEN);
        }
        parcelas[numeroParcelas - 1] = BigDecimal.valueOf(parcelaBaseCentavos + restoCentavos, 2)
                .setScale(2, RoundingMode.HALF_EVEN);

        return parcelas;
    }
}
