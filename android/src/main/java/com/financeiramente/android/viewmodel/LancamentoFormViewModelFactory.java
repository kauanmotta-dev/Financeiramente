package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.CompraCartaoRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.lancamento.EditarCompraCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.lancamento.RegistrarLancamentoUseCase;

public class LancamentoFormViewModelFactory implements ViewModelProvider.Factory {

    private final RegistrarLancamentoUseCase registrar;
    private final EditarLancamentoUseCase editar;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CompraCartaoRepository compraCartaoRepository;
    private final EditarCompraCartaoUseCase editarCompraCartaoUseCase;

    public LancamentoFormViewModelFactory(RegistrarLancamentoUseCase registrar,
                                          EditarLancamentoUseCase editar,
                                          CategoriaRepository categoriaRepository,
                                          TagRepository tagRepository,
                                          LancamentoRepository lancamentoRepository,
                                          CompraCartaoRepository compraCartaoRepository,
                                          EditarCompraCartaoUseCase editarCompraCartaoUseCase) {
        this.registrar = registrar;
        this.editar = editar;
        this.categoriaRepository = categoriaRepository;
        this.tagRepository = tagRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.compraCartaoRepository = compraCartaoRepository;
        this.editarCompraCartaoUseCase = editarCompraCartaoUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        LancamentoFormViewModel viewModel = new LancamentoFormViewModel(registrar, editar,
            categoriaRepository, tagRepository, lancamentoRepository);
        viewModel.setCompraCartaoSupport(editarCompraCartaoUseCase, compraCartaoRepository);
        return (T) viewModel;
    }
}
