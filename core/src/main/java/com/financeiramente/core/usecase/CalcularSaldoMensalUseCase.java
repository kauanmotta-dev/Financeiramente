package com.financeiramente.core.usecase;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.PlanejamentoCategoria;
import com.financeiramente.core.domain.entity.PlanejamentoMensal;
import com.financeiramente.core.domain.entity.Provisao;
import com.financeiramente.core.domain.vo.StatusSaldo;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.PlanejamentoRepository;
import com.financeiramente.core.repository.ProvisaoRepository;
import com.financeiramente.core.util.FinanceCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CalcularSaldoMensalUseCase {

    private final LancamentoRepository lancamentoRepository;
    private final PlanejamentoRepository planejamentoRepository;
    private final ProvisaoRepository provisaoRepository;
    private final CategoriaRepository categoriaRepository;

    public CalcularSaldoMensalUseCase(LancamentoRepository lancamentoRepository,
                                      PlanejamentoRepository planejamentoRepository,
                                      ProvisaoRepository provisaoRepository,
                                      CategoriaRepository categoriaRepository) {
        this.lancamentoRepository   = lancamentoRepository;
        this.planejamentoRepository = planejamentoRepository;
        this.provisaoRepository     = provisaoRepository;
        this.categoriaRepository    = categoriaRepository;
    }

    public SaldoMensalResult executar(int ano, int mes) {
        double receitaRealizada = lancamentoRepository.somarPorTipoEMes(TipoLancamento.RECEITA, ano, mes);
        double totalGasto       = lancamentoRepository.somarPorTipoEMes(TipoLancamento.DESPESA, ano, mes);

        List<Provisao> provisoesAtivas = provisaoRepository.listarAtivas();
        double totalProvisoesMensais = provisoesAtivas.stream()
                .mapToDouble(Provisao::getValorMensal)
                .sum();

        double reservaImprevisto = 0.0;
        Optional<PlanejamentoMensal> planoOpt = planejamentoRepository.buscarPorMes(ano, mes);
        if (planoOpt.isPresent()) {
            reservaImprevisto = planoOpt.get().getReservaImprevisto();
        }

        double saldoDisponivel = FinanceCalculator.saldoDisponivelReal(
                receitaRealizada, totalGasto, totalProvisoesMensais, reservaImprevisto);

        List<SaldoCategoria> saldosPorCategoria = new ArrayList<>();
        if (planoOpt.isPresent()) {
            PlanejamentoMensal plano = planoOpt.get();
            List<PlanejamentoCategoria> itens = planejamentoRepository.listarItensPorPlano(plano.getId());
            for (PlanejamentoCategoria item : itens) {
                double limite = item.getLimite();
                double gasto  = lancamentoRepository.somarPorCategoria(item.getCategoriaId(), ano, mes);
                double saldo  = FinanceCalculator.saldoCategoria(limite, gasto);
                StatusSaldo status = FinanceCalculator.statusCategoria(limite, gasto);

                String nome = categoriaRepository.buscarPorId(item.getCategoriaId())
                        .map(Categoria::getNome)
                        .orElse(item.getCategoriaId());

                saldosPorCategoria.add(new SaldoCategoria(item.getCategoriaId(), nome, limite, gasto, saldo, status));
            }
        }

        return new SaldoMensalResult(
                receitaRealizada,
                totalGasto,
                totalProvisoesMensais,
                reservaImprevisto,
                saldoDisponivel,
                saldosPorCategoria);
    }
}
