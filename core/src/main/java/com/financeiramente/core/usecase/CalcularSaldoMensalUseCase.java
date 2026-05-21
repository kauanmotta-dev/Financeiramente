package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.util.FinanceCalculator;

import java.util.ArrayList;
import java.util.List;

public class CalcularSaldoMensalUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final CategoriaRepository categoriaRepository;

    public CalcularSaldoMensalUseCase(LancamentoRepository lancamentoRepository,
                                      CategoriaRepository categoriaRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.categoriaRepository  = categoriaRepository;
    }

    public SaldoMensalResult executar(int ano, int mes) {
        double receitaRealizada = lancamentoRepository.somarPorTipoEMes(TipoLancamento.RECEITA, ano, mes);
        double totalGasto       = lancamentoRepository.somarPorTipoEMes(TipoLancamento.DESPESA, ano, mes);
        double saldoDisponivel  = receitaRealizada - totalGasto;

        List<SaldoCategoria> saldosPorCategoria = new ArrayList<>();
        List<Categoria> categorias = categoriaRepository.listarTodas();
        for (Categoria cat : categorias) {
            if (cat.getLimiteMensal() == null) continue;
            double limite = cat.getLimiteMensal();
            double gasto  = lancamentoRepository.somarPorCategoria(cat.getId(), ano, mes);
            double saldo  = FinanceCalculator.saldoCategoria(limite, gasto);
            saldosPorCategoria.add(new SaldoCategoria(
                    cat.getId(),
                    cat.getNome(),
                    cat.getTipo(),
                    limite,
                    gasto,
                    saldo));
        }

        return new SaldoMensalResult(receitaRealizada, totalGasto, saldoDisponivel, saldosPorCategoria);
    }
}
