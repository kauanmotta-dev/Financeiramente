package com.financeiramente.core.usecase.lancamento;

import com.financeiramente.core.db.DatabaseDriver;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.usecase.fatura.ResolverFaturaParaLancamentoUseCase;import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Registra um lançamento parcelado no cartão de crédito.
 *
 * <p>Para cada parcela, cria um {@link Lancamento} individual na fatura do mês
 * correspondente. Exemplo: 10 parcelas de R$50 a partir de Jan/2024 gera 10
 * lançamentos de R$50 em Jan, Fev, Mar … Out/2024.
 *
 * <p>O {@code input.getValor()} representa o valor de cada parcela.
 * O número total de parcelas é informado em {@code input.getNumeroParcelas()}.
 */
@Deprecated
public class RegistrarLancamentoParceladoCartaoUseCase {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ResolverFaturaParaLancamentoUseCase resolverFatura;
    private final RegistrarLancamentoUseCase registrarLancamento;
    private final DatabaseDriver databaseDriver;

    public RegistrarLancamentoParceladoCartaoUseCase(
            ResolverFaturaParaLancamentoUseCase resolverFatura,
            RegistrarLancamentoUseCase registrarLancamento,
            DatabaseDriver databaseDriver) {
        this.resolverFatura = resolverFatura;
        this.registrarLancamento = registrarLancamento;
        this.databaseDriver = databaseDriver;
    }

    /**
     * Executa o registro parcelado.
     *
     * @param input     Dados base do lançamento. {@code input.getValor()} é o valor
     *                  de CADA parcela; {@code input.getNumeroParcelas()} é o total.
     * @param cartaoId  ID do cartão de crédito.
     * @return Lista com todos os lançamentos criados (um por parcela).
     */
    public List<Lancamento> executar(RegistrarLancamentoInput input, String cartaoId) {
        int parcelas = input.getNumeroParcelas();
        if (parcelas <= 0) parcelas = 1;
        if (parcelas > 480) {
            throw new IllegalArgumentException("Número de parcelas deve ser entre 1 e 480.");
        }

        LocalDate dataBase = LocalDate.parse(input.getData(), FORMATTER);
        List<Lancamento> criados = new ArrayList<>(parcelas);

        databaseDriver.beginTransaction();
        try {
            for (int i = 0; i < parcelas; i++) {
                LocalDate dataParcela = dataBase.plusMonths(i);
                String dataParcStr = dataParcela.format(FORMATTER);

                Fatura fatura = resolverFatura.executar(cartaoId, dataParcStr);

                String descricao = input.getDescricao();
                if (parcelas > 1) {
                    // Ex: "Compra X (1/10)", "(2/10)"…
                    String base = (descricao != null && !descricao.isEmpty()) ? descricao : "Parcela";
                    descricao = base + " (" + (i + 1) + "/" + parcelas + ")";
                }

                RegistrarLancamentoInput parcelaInput = new RegistrarLancamentoInput(
                        input.getValor(),
                        input.getTipo() != null ? input.getTipo() : TipoLancamento.DESPESA,
                        dataParcStr,
                        descricao,
                        input.getCategoriaId(),
                        fatura.getId(),
                    input.getCompraCartaoId(),
                        // Tags apenas na primeira parcela para evitar duplicação
                        i == 0 ? input.getTags() : java.util.Collections.emptyList()
                );

                Lancamento lancamento = registrarLancamento.executar(parcelaInput);
                criados.add(lancamento);
            }
            databaseDriver.commitTransaction();
        } catch (Exception e) {
            databaseDriver.rollbackTransaction();
            throw e;
        }
        return criados;
    }
}
