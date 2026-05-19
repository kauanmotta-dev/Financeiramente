package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.PlanejamentoRepository;
import com.financeiramente.core.usecase.ConfirmarPlanejamentoUseCase;
import com.financeiramente.core.usecase.CriarPlanejamentoMensalUseCase;
import com.financeiramente.core.usecase.DefinirComoPlanosPadraoUseCase;
import com.financeiramente.core.usecase.DefinirLimiteCategoriaUseCase;

public class PlanejamentoViewModelFactory implements ViewModelProvider.Factory {

    private final CriarPlanejamentoMensalUseCase criarPlanejamento;
    private final ConfirmarPlanejamentoUseCase confirmarPlanejamento;
    private final DefinirLimiteCategoriaUseCase definirLimite;
    private final DefinirComoPlanosPadraoUseCase definirPadrao;
    private final PlanejamentoRepository planejamentoRepository;
    private final CategoriaRepository categoriaRepository;

    public PlanejamentoViewModelFactory(CriarPlanejamentoMensalUseCase criarPlanejamento,
                                         ConfirmarPlanejamentoUseCase confirmarPlanejamento,
                                         DefinirLimiteCategoriaUseCase definirLimite,
                                         DefinirComoPlanosPadraoUseCase definirPadrao,
                                         PlanejamentoRepository planejamentoRepository,
                                         CategoriaRepository categoriaRepository) {
        this.criarPlanejamento = criarPlanejamento;
        this.confirmarPlanejamento = confirmarPlanejamento;
        this.definirLimite = definirLimite;
        this.definirPadrao = definirPadrao;
        this.planejamentoRepository = planejamentoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new PlanejamentoViewModel(
                criarPlanejamento,
                confirmarPlanejamento,
                definirLimite,
                definirPadrao,
                planejamentoRepository,
                categoriaRepository);
    }
}
