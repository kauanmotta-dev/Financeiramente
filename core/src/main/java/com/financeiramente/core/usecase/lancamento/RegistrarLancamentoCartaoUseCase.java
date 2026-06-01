package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.db.TransactionManager;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.usecase.fatura.ResolverFaturaParaLancamentoUseCase;

public class RegistrarLancamentoCartaoUseCase {

    private final ResolverFaturaParaLancamentoUseCase resolverFatura;
    private final RegistrarLancamentoUseCase registrarLancamento;
    private final TransactionManager transactionManager;

    public RegistrarLancamentoCartaoUseCase(ResolverFaturaParaLancamentoUseCase resolverFatura,
                                            RegistrarLancamentoUseCase registrarLancamento,
                                            TransactionManager transactionManager) {
        this.resolverFatura = resolverFatura;
        this.registrarLancamento = registrarLancamento;
        this.transactionManager = transactionManager;
    }

    public Lancamento executar(RegistrarLancamentoInput input, String cartaoId) {
        final Lancamento[] lancamentoCriado = new Lancamento[1];

        transactionManager.executeInTransaction(() -> {
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

            lancamentoCriado[0] = registrarLancamento.executar(inputComFatura);
        });

        return lancamentoCriado[0];
    }
}
