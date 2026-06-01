package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.usecase.saldo.CalcularSaldoDashboardUseCase;
import com.financeiramente.core.usecase.meta.CalcularTotalAportesMesUseCase;
import com.financeiramente.core.usecase.lancamento.DeletarLancamentoUseCase;

public class DashboardViewModelFactory implements ViewModelProvider.Factory {

    private final CalcularSaldoDashboardUseCase calcularSaldo;
    private final DeletarLancamentoUseCase deletar;
    private final CalcularTotalAportesMesUseCase calcularTotalAportesMes;
    private final CartaoCreditoRepository cartaoRepository;
    private final FaturaRepository faturaRepository;

    public DashboardViewModelFactory(CalcularSaldoDashboardUseCase calcularSaldo,
                                     DeletarLancamentoUseCase deletar,
                                     CalcularTotalAportesMesUseCase calcularTotalAportesMes,
                                     CartaoCreditoRepository cartaoRepository,
                                     FaturaRepository faturaRepository) {
        this.calcularSaldo = calcularSaldo;
        this.deletar = deletar;
        this.calcularTotalAportesMes = calcularTotalAportesMes;
        this.cartaoRepository = cartaoRepository;
        this.faturaRepository = faturaRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DashboardViewModel(
                calcularSaldo,
                deletar,
            calcularTotalAportesMes,
            cartaoRepository,
            faturaRepository);
    }
}

