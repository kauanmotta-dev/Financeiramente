package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.usecase.CalcularSaldoMensalUseCase;

public class DashboardViewModelFactory implements ViewModelProvider.Factory {

    private final CalcularSaldoMensalUseCase calcularSaldo;

    public DashboardViewModelFactory(CalcularSaldoMensalUseCase calcularSaldo) {
        this.calcularSaldo = calcularSaldo;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DashboardViewModel(calcularSaldo);
    }
}
