package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.db.DatabaseDriver;
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
    private final DatabaseDriver databaseDriver;

    public GerarCobrancasRecorrentesCartaoUseCase(CompraCartaoRepository compraCartaoRepository,
                                                  ResolverFaturaParaLancamentoUseCase resolverFatura,
                                                  RegistrarLancamentoUseCase registrarLancamento,
                                                  DatabaseDriver databaseDriver) {
        this.compraCartaoRepository = compraCartaoRepository;
        this.resolverFatura = resolverFatura;
        this.registrarLancamento = registrarLancamento;
        this.databaseDriver = databaseDriver;
    }

    public int executar() {
        List<CompraCartao> recorrentes = compraCartaoRepository.listarRecorrentesAtivos();
        if (recorrentes.isEmpty()) {
            return 0;
        }

        int totalGerado = 0;
        LocalDate hoje = LocalDate.now();

        databaseDriver.beginTransaction();
        try {
            for (CompraCartao compra : recorrentes) {
                // 1. Verificação rápida por data (sem criar fatura): já existe lançamento
                //    com data dentro do mês calendário atual para esta recorrência?
                String mesAtual = YearMonth.from(hoje).toString(); // YYYY-MM
                if (compraCartaoRepository.existeLancamentoRecorrenteNoMes(compra.getId(), mesAtual)) {
                    continue;
                }

                // 2. Determina a data-alvo: este mês (se ainda não passou o dia) ou próximo
                LocalDate dataMesAtual = YearMonth.from(hoje).atDay(compra.getDiaRecorrencia());
                LocalDate dataCobranca = hoje.isAfter(dataMesAtual)
                    ? YearMonth.from(hoje).plusMonths(1).atDay(compra.getDiaRecorrencia())
                    : dataMesAtual;

                // 3. Resolve (e cria se necessário) a fatura para a data-alvo
                Fatura fatura = resolverFatura.executar(compra.getCartaoId(), dataCobranca.toString());

                // 4. Guarda dupla: cobre o caso em que a primeira cobrança foi pré-gerada
                //    pelo registrarRecorrente numa fatura futura (ex.: dia já passou no mês de cadastro)
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
                totalGerado++;
            }

            databaseDriver.commitTransaction();
            return totalGerado;
        } catch (Exception e) {
            databaseDriver.rollbackTransaction();
            throw e;
        }
    }
}
