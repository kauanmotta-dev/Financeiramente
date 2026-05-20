package com.financeiramente.core.util;

import com.financeiramente.core.domain.vo.StatusSaldo;
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
}
