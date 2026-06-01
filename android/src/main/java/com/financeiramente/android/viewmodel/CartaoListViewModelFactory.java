package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.usecase.cartao.DesativarCartaoCreditoUseCase;

public class CartaoListViewModelFactory implements ViewModelProvider.Factory {

    private final CartaoCreditoRepository cartaoRepository;
    private final FaturaRepository faturaRepository;
    private final DesativarCartaoCreditoUseCase desativarCartao;

    public CartaoListViewModelFactory(CartaoCreditoRepository cartaoRepository,
                                      FaturaRepository faturaRepository,
                                      DesativarCartaoCreditoUseCase desativarCartao) {
        this.cartaoRepository = cartaoRepository;
        this.faturaRepository = faturaRepository;
        this.desativarCartao = desativarCartao;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CartaoListViewModel(cartaoRepository, faturaRepository, desativarCartao);
    }
}
