package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.usecase.meta.CalcularProjecaoMetaUseCase;
import com.financeiramente.core.usecase.meta.DeletarAporteMetaUseCase;
import com.financeiramente.core.usecase.meta.RegistrarAporteMetaUseCase;

public class MetaDetalheViewModelFactory implements ViewModelProvider.Factory {

    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteRepository;
    private final RegistrarAporteMetaUseCase registrarAporte;
    private final DeletarAporteMetaUseCase deletarAporte;
    private final CalcularProjecaoMetaUseCase calcularProjecao;

    public MetaDetalheViewModelFactory(MetaRepository metaRepository,
                                        AporteMetaRepository aporteRepository,
                                        RegistrarAporteMetaUseCase registrarAporte,
                                        DeletarAporteMetaUseCase deletarAporte,
                                        CalcularProjecaoMetaUseCase calcularProjecao) {
        this.metaRepository = metaRepository;
        this.aporteRepository = aporteRepository;
        this.registrarAporte = registrarAporte;
        this.deletarAporte = deletarAporte;
        this.calcularProjecao = calcularProjecao;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MetaDetalheViewModel(metaRepository, aporteRepository,
                registrarAporte, deletarAporte, calcularProjecao);
    }
}
