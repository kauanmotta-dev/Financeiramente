package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.LancamentoRecorrente;
import com.financeiramente.core.repository.LancamentoRecorrenteRepository;
import com.financeiramente.core.repository.LancamentoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GerarLancamentosRecorrentesUseCase {

    private final LancamentoRecorrenteRepository recorrenteRepository;
    private final LancamentoRepository lancamentoRepository;

    public GerarLancamentosRecorrentesUseCase(LancamentoRecorrenteRepository recorrenteRepository,
                                               LancamentoRepository lancamentoRepository) {
        this.recorrenteRepository = recorrenteRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    public List<Lancamento> executar(int ano, int mes) {
        List<LancamentoRecorrente> ativos = recorrenteRepository.listarAtivos();
        List<Lancamento> gerados = new ArrayList<>();

        for (LancamentoRecorrente rec : ativos) {
            if (lancamentoRepository.existePorRecorrenteEMes(rec.getId(), ano, mes)) {
                continue;
            }

            int dia = (rec.getDiaRecorrencia() != null && rec.getDiaRecorrencia() > 0)
                    ? rec.getDiaRecorrencia() : 1;
            int maxDia = diasNoMes(ano, mes);
            if (dia > maxDia) dia = maxDia;

            String data = String.format("%04d-%02d-%02d", ano, mes, dia);
            long now = System.currentTimeMillis();

            Lancamento lancamento = Lancamento.builder(UUID.randomUUID().toString())
                    .valor(rec.getValor())
                    .tipo(rec.getTipo())
                    .data(data)
                    .descricao(rec.getDescricao())
                    .categoriaId(rec.getCategoriaId())
                    .recorrenteId(rec.getId())
                    .criadoEm(now)
                    .atualizadoEm(now)
                    .build();

            lancamentoRepository.salvar(lancamento);
            gerados.add(lancamento);
        }

        return gerados;
    }

    private int diasNoMes(int ano, int mes) {
        int[] dias = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if (mes == 2 && isAnoBissexto(ano)) return 29;
        return dias[mes - 1];
    }

    private boolean isAnoBissexto(int ano) {
        return (ano % 4 == 0 && ano % 100 != 0) || (ano % 400 == 0);
    }
}
