package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.domain.vo.BandeiraCartao;
import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.usecase.cartao.CriarCartaoCreditoInput;
import com.financeiramente.core.usecase.cartao.CriarCartaoCreditoUseCase;
import com.financeiramente.core.usecase.cartao.EditarCartaoCreditoInput;
import com.financeiramente.core.usecase.cartao.EditarCartaoCreditoUseCase;
import com.financeiramente.core.util.FaturaDateCalculator;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartaoFormViewModel extends ViewModel {

    private final CriarCartaoCreditoUseCase criarCartao;
    private final EditarCartaoCreditoUseCase editarCartao;
    private final CartaoCreditoRepository cartaoRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<CartaoCredito> cartaoEditando = new MutableLiveData<>();
    private final MutableLiveData<String> previewFechamento = new MutableLiveData<>();
    private final MutableLiveData<String> previewVencimento = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sucesso = new MutableLiveData<>();

    public CartaoFormViewModel(CriarCartaoCreditoUseCase criarCartao,
                               EditarCartaoCreditoUseCase editarCartao,
                               CartaoCreditoRepository cartaoRepository) {
        this.criarCartao = criarCartao;
        this.editarCartao = editarCartao;
        this.cartaoRepository = cartaoRepository;
    }

    public void carregarCartao(String cartaoId) {
        executor.execute(() -> {
            try {
                CartaoCredito cartao = cartaoRepository.buscarPorId(cartaoId).orElse(null);
                mainHandler.post(() -> cartaoEditando.setValue(cartao));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void atualizarPreviewDatas(int diaVencimento, int diasParaFechamento) {
        try {
            YearMonth mes = YearMonth.now();
            String fechamento = FaturaDateCalculator.calcularDataFechamento(diaVencimento, diasParaFechamento, mes.toString());
            String vencimento = FaturaDateCalculator.calcularDataVencimento(diaVencimento, mes.toString());
            mainHandler.post(() -> {
                previewFechamento.setValue(fechamento);
                previewVencimento.setValue(vencimento);
            });
        } catch (Exception ignored) {
            mainHandler.post(() -> {
                previewFechamento.setValue(null);
                previewVencimento.setValue(null);
            });
        }
    }

    public void criar(String nome, int diaVencimento, int diasParaFechamento,
                      BigDecimal limite, BandeiraCartao bandeira, String icone, String cor) {
        executor.execute(() -> {
            try {
                CriarCartaoCreditoInput input = new CriarCartaoCreditoInput(
                        nome, diaVencimento, diasParaFechamento, limite, bandeira, icone, cor);
                criarCartao.executar(input);
                mainHandler.post(() -> sucesso.setValue(true));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void editar(String id, String nome, int diaVencimento, int diasParaFechamento,
                       BigDecimal limite, BandeiraCartao bandeira, String icone, String cor) {
        executor.execute(() -> {
            try {
                EditarCartaoCreditoInput input = new EditarCartaoCreditoInput(
                        id, nome, diaVencimento, diasParaFechamento, limite, bandeira, icone, cor);
                editarCartao.executar(input);
                mainHandler.post(() -> sucesso.setValue(true));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<CartaoCredito> getCartaoEditando() { return cartaoEditando; }
    public LiveData<String> getPreviewFechamento() { return previewFechamento; }
    public LiveData<String> getPreviewVencimento() { return previewVencimento; }
    public LiveData<String> getErro() { return erro; }
    public LiveData<Boolean> getSucesso() { return sucesso; }
}
