package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.usecase.meta.CriarMetaUseCase;
import com.financeiramente.core.usecase.meta.DeletarMetaUseCase;
import com.financeiramente.core.usecase.meta.EditarMetaUseCase;

public class MetasViewModelFactory implements ViewModelProvider.Factory {

    private final CriarMetaUseCase criarMeta;
    private final EditarMetaUseCase editarMeta;
    private final DeletarMetaUseCase deletarMeta;
    private final MetaRepository repository;

    public MetasViewModelFactory(CriarMetaUseCase criarMeta,
                                  EditarMetaUseCase editarMeta,
                                  DeletarMetaUseCase deletarMeta,
                                  MetaRepository repository) {
        this.criarMeta = criarMeta;
        this.editarMeta = editarMeta;
        this.deletarMeta = deletarMeta;
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MetasViewModel(criarMeta, editarMeta, deletarMeta, repository);
    }
}
