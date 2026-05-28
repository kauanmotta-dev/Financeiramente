package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.db.TransactionManager;
import com.financeiramente.core.db.AppLogger;
import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.usecase.fatura.ResolverFaturaParaLancamentoUseCase;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

public class GerarCobrancasRecorrentesCartaoUseCase {

    private final CompraCartaoRepository compraCartaoRepository;
    private final ResolverFaturaParaLancamentoUseCase resolverFatura;
    private final RegistrarLancamentoUseCase registrarLancamento;
    private final TransactionManager transactionManager;
    private final AppLogger logger;

    public GerarCobrancasRecorrentesCartaoUseCase(CompraCartaoRepository compraCartaoRepository,
                                                  ResolverFaturaParaLancamentoUseCase resolverFatura,
                                                  RegistrarLancamentoUseCase registrarLancamento,
                                                  TransactionManager transactionManager,
                                                  AppLogger logger) {
        this.compraCartaoRepository = compraCartaoRepository;
        this.resolverFatura = resolverFatura;
        this.registrarLancamento = registrarLancamento;
        this.transactionManager = transactionManager;
        this.logger = logger;
    }

    public int executar() {
        List<CompraCartao> recorrentes = compraCartaoRepository.listarRecorrentesAtivos();
        if (recorrentes.isEmpty()) {
            return 0;
        }

        final int[] totalGerado = {0};
        LocalDate hoje = LocalDate.now();

        try {
            transactionManager.executeInTransaction(() -> {
                for (CompraCartao compra : recorrentes) {
                    String mesAtual = YearMonth.from(hoje).toString();
                    if (compraCartaoRepository.existeLancamentoRecorrenteNoMes(compra.getId(), mesAtual)) {
                        continue;
                    }

                    LocalDate dataMesAtual = YearMonth.from(hoje).atDay(compra.getDiaRecorrencia());
                    LocalDate dataCobranca = hoje.isAfter(dataMesAtual)
                        ? YearMonth.from(hoje).plusMonths(1).atDay(compra.getDiaRecorrencia())
                        : dataMesAtual;

                    Fatura fatura = resolverFatura.executar(compra.getCartaoId(), dataCobranca.toString());

                    if (compraCartaoRepository.existeLancamentoRecorrenteNaFatura(compra.getId(), fatura.getId())) {
                        continue;
                    }

                    RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                            compra.getValorTotal(),
                            TipoLancamento.DESPESA,
                            dataCobranca.toString(),
                            compra.getDescricao(),
                            compra.getCategoriaId(),
                            fatura.getId(),
                            compra.getId(),
                            Collections.emptyList()
                    );

                    registrarLancamento.executar(input);
                    totalGerado[0]++;
                }

            });
        } catch (RuntimeException exception) {
            logger.error("Rollback ao gerar cobrancas recorrentes do cartao.", exception);
            throw exception;
        }

        logger.info("Geradas " + totalGerado[0] + " cobrancas recorrentes no cartao.");

        return totalGerado[0];
    }
}
