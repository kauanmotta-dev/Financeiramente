package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.usecase.CriarCategoriaUseCase;
import com.financeiramente.core.usecase.DeletarCategoriaUseCase;
import com.financeiramente.core.usecase.EditarCategoriaUseCase;
import com.financeiramente.core.usecase.ReordenarCategoriasUseCase;

public class CategoriasViewModelFactory implements ViewModelProvider.Factory {

    private final CategoriaRepository categoriaRepository;
    private final CriarCategoriaUseCase criarCategoriaUseCase;
    private final EditarCategoriaUseCase editarCategoriaUseCase;
    private final DeletarCategoriaUseCase deletarCategoriaUseCase;
    private final ReordenarCategoriasUseCase reordenarCategoriasUseCase;

    public CategoriasViewModelFactory(CategoriaRepository categoriaRepository,
                                      CriarCategoriaUseCase criarCategoriaUseCase,
                                      EditarCategoriaUseCase editarCategoriaUseCase,
                                      DeletarCategoriaUseCase deletarCategoriaUseCase,
                                      ReordenarCategoriasUseCase reordenarCategoriasUseCase) {
        this.categoriaRepository = categoriaRepository;
        this.criarCategoriaUseCase = criarCategoriaUseCase;
        this.editarCategoriaUseCase = editarCategoriaUseCase;
        this.deletarCategoriaUseCase = deletarCategoriaUseCase;
        this.reordenarCategoriasUseCase = reordenarCategoriasUseCase;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CategoriasViewModel(
                categoriaRepository,
                criarCategoriaUseCase,
                editarCategoriaUseCase,
                deletarCategoriaUseCase,
                reordenarCategoriasUseCase);
    }
}
