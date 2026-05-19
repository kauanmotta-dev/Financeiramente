package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.LancamentoRecorrenteRepository;
import com.financeiramente.core.usecase.CriarLancamentoRecorrenteUseCase;
import com.financeiramente.core.usecase.DesativarLancamentoRecorrenteUseCase;
import com.financeiramente.core.usecase.EditarLancamentoRecorrenteUseCase;

public class RecorrentesViewModelFactory implements ViewModelProvider.Factory {

    private final CriarLancamentoRecorrenteUseCase criarRecorrente;
    private final EditarLancamentoRecorrenteUseCase editarRecorrente;
    private final DesativarLancamentoRecorrenteUseCase desativarRecorrente;
    private final LancamentoRecorrenteRepository repository;

    public RecorrentesViewModelFactory(CriarLancamentoRecorrenteUseCase criarRecorrente,
                                        EditarLancamentoRecorrenteUseCase editarRecorrente,
                                        DesativarLancamentoRecorrenteUseCase desativarRecorrente,
                                        LancamentoRecorrenteRepository repository) {
        this.criarRecorrente = criarRecorrente;
        this.editarRecorrente = editarRecorrente;
        this.desativarRecorrente = desativarRecorrente;
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new RecorrentesViewModel(criarRecorrente, editarRecorrente, desativarRecorrente, repository);
    }
}
