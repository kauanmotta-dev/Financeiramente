package com.financeiramente.core.domain.vo;

/**
 * Status visual do saldo de uma categoria no mês.
 * VERDE:    gasto < 80% do limite
 * AMARELO:  80% <= gasto <= 100% do limite
 * VERMELHO: gasto > limite
 */
public enum StatusSaldo {
    VERDE,
    AMARELO,
    VERMELHO
}
