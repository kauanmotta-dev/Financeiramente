package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.usecase.fatura.ResolverFaturaParaLancamentoUseCase;

@Deprecated
public class RegistrarLancamentoCartaoUseCase {

    private final ResolverFaturaParaLancamentoUseCase resolverFatura;
    private final RegistrarLancamentoUseCase registrarLancamento;
    private final DatabaseDriver databaseDriver;

    public RegistrarLancamentoCartaoUseCase(ResolverFaturaParaLancamentoUseCase resolverFatura,
                                            RegistrarLancamentoUseCase registrarLancamento,
                                            DatabaseDriver databaseDriver) {
        this.resolverFatura = resolverFatura;
        this.registrarLancamento = registrarLancamento;
        this.databaseDriver = databaseDriver;
    }

    public Lancamento executar(RegistrarLancamentoInput input, String cartaoId) {
        databaseDriver.beginTransaction();
        try {
            Fatura fatura = resolverFatura.executar(cartaoId, input.getData());

            RegistrarLancamentoInput inputComFatura = new RegistrarLancamentoInput(
                    input.getValor(),
                    input.getTipo() != null ? input.getTipo() : TipoLancamento.DESPESA,
                    input.getData(),
                    input.getDescricao(),
                    input.getCategoriaId(),
                    fatura.getId(),
                    input.getCompraCartaoId(),
                    input.getTags()
            );

            Lancamento lancamento = registrarLancamento.executar(inputComFatura);
            databaseDriver.commitTransaction();
            return lancamento;
        } catch (Exception e) {
            databaseDriver.rollbackTransaction();
            throw e;
        }
    }
}
