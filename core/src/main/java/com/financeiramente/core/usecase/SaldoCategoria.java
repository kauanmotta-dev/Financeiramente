package com.financeiramente.core.usecase;

public class SaldoCategoria {
    private final String categoriaId;
    private final String categoriaNome;
    private final double limite;
    private final double gastoRealizado;
    private final double saldo;

    public SaldoCategoria(String categoriaId, String categoriaNome,
                          double limite, double gastoRealizado, double saldo) {
        this.categoriaId     = categoriaId;
        this.categoriaNome   = categoriaNome;
        this.limite          = limite;
        this.gastoRealizado  = gastoRealizado;
        this.saldo           = saldo;
    }

    public String getCategoriaId()     { return categoriaId; }
    public String getCategoriaNome()   { return categoriaNome; }
    public double getLimite()          { return limite; }
    public double getGastoRealizado()  { return gastoRealizado; }
    public double getSaldo()           { return saldo; }
}
