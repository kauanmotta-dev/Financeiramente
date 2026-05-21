package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.DeletarLancamentoUseCase;
import com.financeiramente.core.usecase.ListarLancamentosUseCase;

public class LancamentosListViewModelFactory implements ViewModelProvider.Factory {

    private final ListarLancamentosUseCase listar;
    private final DeletarLancamentoUseCase deletar;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;

    public LancamentosListViewModelFactory(ListarLancamentosUseCase listar,
                                           DeletarLancamentoUseCase deletar,
                                           CategoriaRepository categoriaRepository,
                                           TagRepository tagRepository) {
        this.listar = listar;
        this.deletar = deletar;
        this.categoriaRepository = categoriaRepository;
        this.tagRepository = tagRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new LancamentosListViewModel(listar, deletar, categoriaRepository, tagRepository);
    }
}

