package com.financeiramente.android.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.usecase.cartao.CriarCartaoCreditoUseCase;
import com.financeiramente.core.usecase.cartao.EditarCartaoCreditoUseCase;

public class CartaoFormViewModelFactory implements ViewModelProvider.Factory {

    private final CriarCartaoCreditoUseCase criarCartao;
    private final EditarCartaoCreditoUseCase editarCartao;
    private final CartaoCreditoRepository cartaoRepository;

    public CartaoFormViewModelFactory(CriarCartaoCreditoUseCase criarCartao,
                                      EditarCartaoCreditoUseCase editarCartao,
                                      CartaoCreditoRepository cartaoRepository) {
        this.criarCartao = criarCartao;
        this.editarCartao = editarCartao;
        this.cartaoRepository = cartaoRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new CartaoFormViewModel(criarCartao, editarCartao, cartaoRepository);
    }
}
