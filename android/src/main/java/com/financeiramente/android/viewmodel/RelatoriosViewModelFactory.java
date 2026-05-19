package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.GerarRelatorioUseCase;

public class RelatoriosViewModelFactory implements ViewModelProvider.Factory {

    private final GerarRelatorioUseCase gerarRelatorio;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;

    public RelatoriosViewModelFactory(GerarRelatorioUseCase gerarRelatorio,
                                      CategoriaRepository categoriaRepository,
                                      TagRepository tagRepository) {
        this.gerarRelatorio      = gerarRelatorio;
        this.categoriaRepository = categoriaRepository;
        this.tagRepository       = tagRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new RelatoriosViewModel(gerarRelatorio, categoriaRepository, tagRepository);
    }
}
