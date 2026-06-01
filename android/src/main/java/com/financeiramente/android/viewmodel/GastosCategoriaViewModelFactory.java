package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.MetaRepository;

public class GastosCategoriaViewModelFactory implements ViewModelProvider.Factory {

    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteMetaRepository;

    public GastosCategoriaViewModelFactory(CategoriaRepository categoriaRepository,
                                           LancamentoRepository lancamentoRepository,
                                           MetaRepository metaRepository,
                                           AporteMetaRepository aporteMetaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.metaRepository = metaRepository;
        this.aporteMetaRepository = aporteMetaRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new GastosCategoriaViewModel(
                categoriaRepository,
                lancamentoRepository,
                metaRepository,
                aporteMetaRepository);
    }
}
