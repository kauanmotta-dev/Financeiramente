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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartaoListViewModel extends ViewModel {

    private final CartaoCreditoRepository cartaoRepository;
    private final FaturaRepository faturaRepository;
    private final DesativarCartaoCreditoUseCase desativarCartao;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<CartaoComUtilizacao>> cartoes = new MutableLiveData<>();
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
                List<CartaoComUtilizacao> comUtil = new ArrayList<>();
                for (CartaoCredito c : lista) {
                    double limite = c.getLimite() != null ? c.getLimite().doubleValue() : 0.0;
                    double utilizado = 0.0;
                    double pct = 0.0;
                    if (limite > 0.0) {
                        utilizado = faturaRepository.somarTotalUtilizadoPorCartao(c.getId()).doubleValue();
                        pct = Math.min(100.0, (utilizado / limite) * 100.0);
                    }
                    comUtil.add(new CartaoComUtilizacao(c, utilizado, pct));
                }
                mainHandler.post(() -> cartoes.setValue(comUtil));
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
                List<CartaoComUtilizacao> comUtil = new ArrayList<>();
                for (CartaoCredito c : lista) {
                    double limite = c.getLimite() != null ? c.getLimite().doubleValue() : 0.0;
                    double utilizado = 0.0;
                    double pct = 0.0;
                    if (limite > 0.0) {
                        utilizado = faturaRepository.somarTotalUtilizadoPorCartao(c.getId()).doubleValue();
                        pct = Math.min(100.0, (utilizado / limite) * 100.0);
                    }
                    comUtil.add(new CartaoComUtilizacao(c, utilizado, pct));
                }
                mainHandler.post(() -> {
                    cartoes.setValue(comUtil);
                    sucesso.setValue(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<List<CartaoComUtilizacao>> getCartoes() { return cartoes; }
    public LiveData<String> getErro() { return erro; }
    public LiveData<Boolean> getSucesso() { return sucesso; }

    public static final class CartaoComUtilizacao {
        private final CartaoCredito cartao;
        private final double utilizado;
        private final double percentualUso;

        public CartaoComUtilizacao(CartaoCredito cartao, double utilizado, double percentualUso) {
            this.cartao = cartao;
            this.utilizado = utilizado;
            this.percentualUso = percentualUso;
        }

        public CartaoCredito getCartao() { return cartao; }
        public double getUtilizado() { return utilizado; }
        public double getPercentualUso() { return percentualUso; }
    }
}
