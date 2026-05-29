package com.financeiramente.core.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;

public final class FaturaDateCalculator {

    private FaturaDateCalculator() {}

    public static String calcularDataVencimento(int diaVencimento, String mesReferencia) {
        YearMonth ym = YearMonth.parse(mesReferencia);
        LocalDate date = ym.atDay(Math.min(diaVencimento, ym.lengthOfMonth()));
        date = ajustarFimDeSemanaProximoDiaUtil(date);
        return date.toString();
    }

    public static String calcularDataFechamento(int diaVencimento, int diasParaFechamento, String mesReferencia) {
        YearMonth ym = YearMonth.parse(mesReferencia);
        int diaFechamento = diaVencimento - diasParaFechamento;

        LocalDate date;
        if (diaFechamento >= 1) {
            date = ym.atDay(Math.min(diaFechamento, ym.lengthOfMonth()));
        } else {
            
            YearMonth mesAnterior = ym.minusMonths(1);
            int diaNoMesAnterior = mesAnterior.lengthOfMonth() + diaFechamento;
            date = mesAnterior.atDay(Math.max(1, diaNoMesAnterior));
        }

        date = ajustarFimDeSemanaDiaUtilAnterior(date);
        return date.toString();
    }

    public static YearMonth resolverMesCompetencia(int diaVencimento, int diasParaFechamento, String dataLancamento) {
        LocalDate lancamento = LocalDate.parse(dataLancamento);
        YearMonth mesCorrente = YearMonth.of(lancamento.getYear(), lancamento.getMonth());

        String dataFechamento = calcularDataFechamento(diaVencimento, diasParaFechamento, mesCorrente.toString());
        LocalDate fechamento = LocalDate.parse(dataFechamento);

        if (lancamento.isAfter(fechamento)) {
            return mesCorrente.plusMonths(1);
        }
        return mesCorrente;
    }

    

    
    private static LocalDate ajustarFimDeSemanaProximoDiaUtil(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) return date.plusDays(2);
        if (dow == DayOfWeek.SUNDAY)   return date.plusDays(1);
        return date;
    }

    
    private static LocalDate ajustarFimDeSemanaDiaUtilAnterior(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) return date.minusDays(1);
        if (dow == DayOfWeek.SUNDAY)   return date.minusDays(2);
        return date;
    }
}
