package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.RegistrarLancamentoUseCase;

public class LancamentoFormViewModelFactory implements ViewModelProvider.Factory {

    private final RegistrarLancamentoUseCase registrar;
    private final EditarLancamentoUseCase editar;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;
    private final LancamentoRepository lancamentoRepository;

    public LancamentoFormViewModelFactory(RegistrarLancamentoUseCase registrar,
                                          EditarLancamentoUseCase editar,
                                          CategoriaRepository categoriaRepository,
                                          TagRepository tagRepository,
                                          LancamentoRepository lancamentoRepository) {
        this.registrar = registrar;
        this.editar = editar;
        this.categoriaRepository = categoriaRepository;
        this.tagRepository = tagRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new LancamentoFormViewModel(registrar, editar,
                categoriaRepository, tagRepository, lancamentoRepository);
    }
}
