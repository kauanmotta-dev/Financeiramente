package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.ProvisaoRepository;
import com.financeiramente.core.usecase.CriarProvisaoUseCase;
import com.financeiramente.core.usecase.DesativarProvisaoUseCase;
import com.financeiramente.core.usecase.EditarProvisaoUseCase;

public class ProvisoesViewModelFactory implements ViewModelProvider.Factory {

    private final CriarProvisaoUseCase criarProvisao;
    private final EditarProvisaoUseCase editarProvisao;
    private final DesativarProvisaoUseCase desativarProvisao;
    private final ProvisaoRepository repository;

    public ProvisoesViewModelFactory(CriarProvisaoUseCase criarProvisao,
                                      EditarProvisaoUseCase editarProvisao,
                                      DesativarProvisaoUseCase desativarProvisao,
                                      ProvisaoRepository repository) {
        this.criarProvisao = criarProvisao;
        this.editarProvisao = editarProvisao;
        this.desativarProvisao = desativarProvisao;
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ProvisoesViewModel(criarProvisao, editarProvisao, desativarProvisao, repository);
    }
}
