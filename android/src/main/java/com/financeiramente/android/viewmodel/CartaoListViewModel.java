package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.usecase.cartao.DesativarCartaoCreditoUseCase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartaoListViewModel extends ViewModel {

    private final CartaoCreditoRepository cartaoRepository;
    private final FaturaRepository faturaRepository;
    private final DesativarCartaoCreditoUseCase desativarCartao;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<CartaoCredito>> cartoes = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sucesso = new MutableLiveData<>();

    public CartaoListViewModel(CartaoCreditoRepository cartaoRepository,
                               FaturaRepository faturaRepository,
                               DesativarCartaoCreditoUseCase desativarCartao) {
        this.cartaoRepository = cartaoRepository;
        this.faturaRepository = faturaRepository;
        this.desativarCartao = desativarCartao;
        carregar();
    }

    public void carregar() {
        executor.execute(() -> {
            try {
                List<CartaoCredito> lista = cartaoRepository.listarAtivos();
                mainHandler.post(() -> cartoes.setValue(lista));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void desativar(String cartaoId) {
        executor.execute(() -> {
            try {
                desativarCartao.executar(cartaoId);
                List<CartaoCredito> lista = cartaoRepository.listarAtivos();
                mainHandler.post(() -> {
                    cartoes.setValue(lista);
                    sucesso.setValue(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public double getLimiteUtilizado(String cartaoId) {
        try {
            return faturaRepository.somarTotalUtilizadoPorCartao(cartaoId).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public LiveData<List<CartaoCredito>> getCartoes() { return cartoes; }
    public LiveData<String> getErro() { return erro; }
    public LiveData<Boolean> getSucesso() { return sucesso; }
}
