package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.relatorio.GerarRelatorioUseCase;

public class RelatoriosViewModelFactory implements ViewModelProvider.Factory {

    private final GerarRelatorioUseCase gerarRelatorio;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;
    private final LancamentoRepository lancamentoRepository;

    public RelatoriosViewModelFactory(GerarRelatorioUseCase gerarRelatorio,
                                      CategoriaRepository categoriaRepository,
                                      TagRepository tagRepository,
                                      LancamentoRepository lancamentoRepository) {
        this.gerarRelatorio       = gerarRelatorio;
        this.categoriaRepository  = categoriaRepository;
        this.tagRepository        = tagRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new RelatoriosViewModel(
                gerarRelatorio, categoriaRepository, tagRepository, lancamentoRepository);
    }
}
