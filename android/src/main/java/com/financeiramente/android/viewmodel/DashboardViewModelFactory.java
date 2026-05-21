package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.usecase.CalcularSaldoMensalUseCase;
import com.financeiramente.core.usecase.DeletarLancamentoUseCase;

public class DashboardViewModelFactory implements ViewModelProvider.Factory {

    private final CalcularSaldoMensalUseCase calcularSaldo;
    private final DeletarLancamentoUseCase deletar;
    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteMetaRepository;

    public DashboardViewModelFactory(CalcularSaldoMensalUseCase calcularSaldo,
                                     DeletarLancamentoUseCase deletar,
                                     MetaRepository metaRepository,
                                     AporteMetaRepository aporteMetaRepository) {
        this.calcularSaldo = calcularSaldo;
        this.deletar = deletar;
        this.metaRepository = metaRepository;
        this.aporteMetaRepository = aporteMetaRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DashboardViewModel(calcularSaldo, deletar, metaRepository, aporteMetaRepository);
    }
}
