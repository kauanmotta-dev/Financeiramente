package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.vo.StatusSaldo;

/**
 * DTO de saída representando o saldo de uma categoria em um determinado mês.
 * Calculado por: limite − SUM(lançamentos da categoria no mês).
 */
public class SaldoCategoria {
    private final String categoriaId;
    private final String categoriaNome;
    private final double limite;
    private final double gastoRealizado;
    private final double saldo;
    private final StatusSaldo status;

    public SaldoCategoria(String categoriaId, String categoriaNome,
                          double limite, double gastoRealizado,
                          double saldo, StatusSaldo status) {
        this.categoriaId = categoriaId;
        this.categoriaNome = categoriaNome;
        this.limite = limite;
        this.gastoRealizado = gastoRealizado;
        this.saldo = saldo;
        this.status = status;
    }

    public String getCategoriaId() { return categoriaId; }
    public String getCategoriaNome() { return categoriaNome; }
    public double getLimite() { return limite; }
    public double getGastoRealizado() { return gastoRealizado; }
    public double getSaldo() { return saldo; }
    public StatusSaldo getStatus() { return status; }
}
