package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.usecase.fatura.AlterarStatusFaturaUseCase;
import com.financeiramente.core.usecase.fatura.AtualizarStatusFaturasUseCase;
import com.financeiramente.core.usecase.lancamento.DeletarCompraCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.CancelarRecorrenciaCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.ListarComprasCartaoUseCase;

public class FaturaListViewModelFactory implements ViewModelProvider.Factory {

    private final FaturaRepository faturaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final AtualizarStatusFaturasUseCase atualizarStatus;
    private final AlterarStatusFaturaUseCase alterarStatusFatura;
    private final ListarComprasCartaoUseCase listarComprasCartaoUseCase;
    private final CancelarRecorrenciaCartaoUseCase cancelarRecorrenciaCartaoUseCase;
    private final DeletarCompraCartaoUseCase deletarCompraCartaoUseCase;

    public FaturaListViewModelFactory(FaturaRepository faturaRepository,
                                      LancamentoRepository lancamentoRepository,
                                      AtualizarStatusFaturasUseCase atualizarStatus,
                                      AlterarStatusFaturaUseCase alterarStatusFatura,
                                      ListarComprasCartaoUseCase listarComprasCartaoUseCase,
                                      CancelarRecorrenciaCartaoUseCase cancelarRecorrenciaCartaoUseCase,
                                      DeletarCompraCartaoUseCase deletarCompraCartaoUseCase) {
        this.faturaRepository = faturaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.atualizarStatus = atualizarStatus;
        this.alterarStatusFatura = alterarStatusFatura;
        this.listarComprasCartaoUseCase = listarComprasCartaoUseCase;
        this.cancelarRecorrenciaCartaoUseCase = cancelarRecorrenciaCartaoUseCase;
        this.deletarCompraCartaoUseCase = deletarCompraCartaoUseCase;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new FaturaListViewModel(
                faturaRepository,
            lancamentoRepository,
                atualizarStatus,
                alterarStatusFatura,
                listarComprasCartaoUseCase,
            cancelarRecorrenciaCartaoUseCase,
            deletarCompraCartaoUseCase);
    }
}
