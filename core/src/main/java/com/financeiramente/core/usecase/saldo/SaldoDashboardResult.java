package com.financeiramente.core.usecase.saldo;

public class SaldoDashboardResult {

    private final double saldoConta;
    private final double totalReceita;
    private final double totalGastoConta;

    private final double saldoEssenciais;
    private final double limiteEssenciais;
    private final double gastoEssenciais;

    private final double saldoNaoEssenciais;
    private final double limiteNaoEssenciais;
    private final double gastoNaoEssenciais;

    public SaldoDashboardResult(
            double saldoConta, double totalReceita, double totalGastoConta,
            double saldoEssenciais, double limiteEssenciais, double gastoEssenciais,
            double saldoNaoEssenciais, double limiteNaoEssenciais, double gastoNaoEssenciais) {
        this.saldoConta = saldoConta;
        this.totalReceita = totalReceita;
        this.totalGastoConta = totalGastoConta;
        this.saldoEssenciais = saldoEssenciais;
        this.limiteEssenciais = limiteEssenciais;
        this.gastoEssenciais = gastoEssenciais;
        this.saldoNaoEssenciais = saldoNaoEssenciais;
        this.limiteNaoEssenciais = limiteNaoEssenciais;
        this.gastoNaoEssenciais = gastoNaoEssenciais;
    }

    public double getSaldoConta()          { return saldoConta; }
    public double getTotalReceita()        { return totalReceita; }
    public double getTotalGastoConta()     { return totalGastoConta; }

    public double getSaldoEssenciais()     { return saldoEssenciais; }
    public double getLimiteEssenciais()    { return limiteEssenciais; }
    public double getGastoEssenciais()     { return gastoEssenciais; }

    public double getSaldoNaoEssenciais()  { return saldoNaoEssenciais; }
    public double getLimiteNaoEssenciais() { return limiteNaoEssenciais; }
    public double getGastoNaoEssenciais()  { return gastoNaoEssenciais; }
}
