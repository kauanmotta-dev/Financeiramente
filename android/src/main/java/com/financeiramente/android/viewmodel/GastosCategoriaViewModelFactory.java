package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;

public class GastosCategoriaViewModelFactory implements ViewModelProvider.Factory {

    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;

    public GastosCategoriaViewModelFactory(CategoriaRepository categoriaRepository,
                                           LancamentoRepository lancamentoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new GastosCategoriaViewModel(categoriaRepository, lancamentoRepository);
    }
}
