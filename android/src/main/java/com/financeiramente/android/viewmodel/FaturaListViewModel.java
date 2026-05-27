package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.usecase.fatura.AtualizarStatusFaturasUseCase;
import com.financeiramente.core.usecase.lancamento.CancelarRecorrenciaCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.ListarComprasCartaoUseCase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaturaListViewModel extends ViewModel {

    private final FaturaRepository faturaRepository;
    private final AtualizarStatusFaturasUseCase atualizarStatus;
    private final ListarComprasCartaoUseCase listarComprasCartaoUseCase;
    private final CancelarRecorrenciaCartaoUseCase cancelarRecorrenciaCartaoUseCase;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<Fatura>> faturas = new MutableLiveData<>();
    private final MutableLiveData<List<CompraCartao>> cobrancasRecorrentes = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    private String cartaoId;

    public FaturaListViewModel(FaturaRepository faturaRepository,
                               AtualizarStatusFaturasUseCase atualizarStatus,
                               ListarComprasCartaoUseCase listarComprasCartaoUseCase,
                               CancelarRecorrenciaCartaoUseCase cancelarRecorrenciaCartaoUseCase) {
        this.faturaRepository = faturaRepository;
        this.atualizarStatus = atualizarStatus;
        this.listarComprasCartaoUseCase = listarComprasCartaoUseCase;
        this.cancelarRecorrenciaCartaoUseCase = cancelarRecorrenciaCartaoUseCase;
    }

    public void init(String cartaoId) {
        this.cartaoId = cartaoId;
        carregar();
    }

    public void carregar() {
        if (cartaoId == null) return;
        executor.execute(() -> {
            try {
                atualizarStatus.executar();
                List<Fatura> lista = faturaRepository.listarPorCartao(cartaoId);
                List<CompraCartao> comprasCartao = listarComprasCartaoUseCase.executar(cartaoId).stream()
                        .map(resumo -> resumo.getCompra())
                        .filter(compra -> compra.getTipo() == TipoCompraCartao.RECORRENTE)
                        .filter(CompraCartao::isAtivo)
                        .collect(java.util.stream.Collectors.toList());
                mainHandler.post(() -> faturas.setValue(lista));
                mainHandler.post(() -> cobrancasRecorrentes.setValue(comprasCartao));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void cancelarRecorrencia(String compraCartaoId) {
        executor.execute(() -> {
            try {
                cancelarRecorrenciaCartaoUseCase.executar(compraCartaoId);
                List<Fatura> lista = faturaRepository.listarPorCartao(cartaoId);
                List<CompraCartao> comprasCartao = listarComprasCartaoUseCase.executar(cartaoId).stream()
                        .map(resumo -> resumo.getCompra())
                        .filter(compra -> compra.getTipo() == TipoCompraCartao.RECORRENTE)
                        .filter(CompraCartao::isAtivo)
                        .collect(java.util.stream.Collectors.toList());
                mainHandler.post(() -> {
                    faturas.setValue(lista);
                    cobrancasRecorrentes.setValue(comprasCartao);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<List<Fatura>> getFaturas() { return faturas; }
    public LiveData<List<CompraCartao>> getCobrancasRecorrentes() { return cobrancasRecorrentes; }
    public LiveData<String> getErro() { return erro; }
}
