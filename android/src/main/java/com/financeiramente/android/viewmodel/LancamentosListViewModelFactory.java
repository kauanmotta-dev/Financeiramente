package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.usecase.DeletarLancamentoUseCase;
import com.financeiramente.core.usecase.ListarLancamentosUseCase;

public class LancamentosListViewModelFactory implements ViewModelProvider.Factory {

    private final ListarLancamentosUseCase listar;
    private final DeletarLancamentoUseCase deletar;

    public LancamentosListViewModelFactory(ListarLancamentosUseCase listar,
                                           DeletarLancamentoUseCase deletar) {
        this.listar = listar;
        this.deletar = deletar;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new LancamentosListViewModel(listar, deletar);
    }
}
