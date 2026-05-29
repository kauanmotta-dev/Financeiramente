package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.relatorio.GerarRelatorioUseCase;

public class RelatoriosViewModelFactory implements ViewModelProvider.Factory {

    private final GerarRelatorioUseCase gerarRelatorio;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;
    private final LancamentoRepository lancamentoRepository;
    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteMetaRepository;

    public RelatoriosViewModelFactory(GerarRelatorioUseCase gerarRelatorio,
                                      CategoriaRepository categoriaRepository,
                                      TagRepository tagRepository,
                                      LancamentoRepository lancamentoRepository,
                                      MetaRepository metaRepository,
                                      AporteMetaRepository aporteMetaRepository) {
        this.gerarRelatorio       = gerarRelatorio;
        this.categoriaRepository  = categoriaRepository;
        this.tagRepository        = tagRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.metaRepository = metaRepository;
        this.aporteMetaRepository = aporteMetaRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new RelatoriosViewModel(
                gerarRelatorio,
                categoriaRepository,
                tagRepository,
                lancamentoRepository,
                metaRepository,
                aporteMetaRepository);
    }
}
