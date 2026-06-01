package com.financeiramente.core.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class DataValidator {

    private DataValidator() {
    }

    public static String validarData(String data) {
        String valor = DomainException.requireNonBlank(data, "Data é obrigatória.");
        try {
            LocalDate.parse(valor);
            return valor;
        } catch (DateTimeParseException ex) {
            throw new DomainException("Data inválida. Formato esperado: YYYY-MM-DD.");
        }
    }
}