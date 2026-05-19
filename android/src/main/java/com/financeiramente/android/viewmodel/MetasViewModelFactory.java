package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.usecase.CriarMetaUseCase;
import com.financeiramente.core.usecase.DesativarMetaUseCase;
import com.financeiramente.core.usecase.EditarMetaUseCase;

public class MetasViewModelFactory implements ViewModelProvider.Factory {

    private final CriarMetaUseCase criarMeta;
    private final EditarMetaUseCase editarMeta;
    private final DesativarMetaUseCase desativarMeta;
    private final MetaRepository repository;

    public MetasViewModelFactory(CriarMetaUseCase criarMeta,
                                  EditarMetaUseCase editarMeta,
                                  DesativarMetaUseCase desativarMeta,
                                  MetaRepository repository) {
        this.criarMeta = criarMeta;
        this.editarMeta = editarMeta;
        this.desativarMeta = desativarMeta;
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MetasViewModel(criarMeta, editarMeta, desativarMeta, repository);
    }
}
