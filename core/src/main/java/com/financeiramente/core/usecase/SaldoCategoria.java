package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.vo.TipoCategoria;

public class SaldoCategoria {
    private final String categoriaId;
    private final String categoriaNome;
    private final TipoCategoria tipoCategoria;
    private final double limite;
    private final double gastoRealizado;
    private final double saldo;

    public SaldoCategoria(String categoriaId, String categoriaNome, TipoCategoria tipoCategoria,
                          double limite, double gastoRealizado, double saldo) {
        this.categoriaId     = categoriaId;
        this.categoriaNome   = categoriaNome;
        this.tipoCategoria   = tipoCategoria;
        this.limite          = limite;
        this.gastoRealizado  = gastoRealizado;
        this.saldo           = saldo;
    }

    public String getCategoriaId()     { return categoriaId; }
    public String getCategoriaNome()   { return categoriaNome; }
    public TipoCategoria getTipoCategoria() { return tipoCategoria; }
    public double getLimite()          { return limite; }
    public double getGastoRealizado()  { return gastoRealizado; }
    public double getSaldo()           { return saldo; }
}
