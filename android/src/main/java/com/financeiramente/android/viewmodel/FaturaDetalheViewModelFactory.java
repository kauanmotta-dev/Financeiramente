package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.usecase.fatura.AlterarStatusFaturaUseCase;
import com.financeiramente.core.usecase.fatura.AnteciparLancamentosFaturaUseCase;
import com.financeiramente.core.usecase.fatura.AtualizarStatusFaturasUseCase;
import com.financeiramente.core.usecase.fatura.PagarFaturaUseCase;

public class FaturaDetalheViewModelFactory implements ViewModelProvider.Factory {

    private final FaturaRepository faturaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final PagarFaturaUseCase pagarFatura;
    private final AtualizarStatusFaturasUseCase atualizarStatus;
    private final AlterarStatusFaturaUseCase alterarStatusFatura;
    private final AnteciparLancamentosFaturaUseCase anteciparLancamentos;

    public FaturaDetalheViewModelFactory(FaturaRepository faturaRepository,
                                          LancamentoRepository lancamentoRepository,
                                          CategoriaRepository categoriaRepository,
                                          PagarFaturaUseCase pagarFatura,
                                          AtualizarStatusFaturasUseCase atualizarStatus,
                                          AlterarStatusFaturaUseCase alterarStatusFatura,
                                          AnteciparLancamentosFaturaUseCase anteciparLancamentos) {
        this.faturaRepository = faturaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.categoriaRepository = categoriaRepository;
        this.pagarFatura = pagarFatura;
        this.atualizarStatus = atualizarStatus;
        this.alterarStatusFatura = alterarStatusFatura;
        this.anteciparLancamentos = anteciparLancamentos;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new FaturaDetalheViewModel(faturaRepository, lancamentoRepository, categoriaRepository, pagarFatura, atualizarStatus, alterarStatusFatura, anteciparLancamentos);
    }
}
