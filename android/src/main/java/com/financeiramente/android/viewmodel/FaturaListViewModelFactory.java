package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.usecase.fatura.AtualizarStatusFaturasUseCase;
import com.financeiramente.core.usecase.lancamento.CancelarRecorrenciaCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.ListarComprasCartaoUseCase;

public class FaturaListViewModelFactory implements ViewModelProvider.Factory {

    private final FaturaRepository faturaRepository;
    private final AtualizarStatusFaturasUseCase atualizarStatus;
    private final ListarComprasCartaoUseCase listarComprasCartaoUseCase;
    private final CancelarRecorrenciaCartaoUseCase cancelarRecorrenciaCartaoUseCase;

    public FaturaListViewModelFactory(FaturaRepository faturaRepository,
                                      AtualizarStatusFaturasUseCase atualizarStatus,
                                      ListarComprasCartaoUseCase listarComprasCartaoUseCase,
                                      CancelarRecorrenciaCartaoUseCase cancelarRecorrenciaCartaoUseCase) {
        this.faturaRepository = faturaRepository;
        this.atualizarStatus = atualizarStatus;
        this.listarComprasCartaoUseCase = listarComprasCartaoUseCase;
        this.cancelarRecorrenciaCartaoUseCase = cancelarRecorrenciaCartaoUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new FaturaListViewModel(
                faturaRepository,
                atualizarStatus,
                listarComprasCartaoUseCase,
                cancelarRecorrenciaCartaoUseCase);
    }
}
