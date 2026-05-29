package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.CompraCartao;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.vo.TipoCompraCartao;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.usecase.fatura.AlterarStatusFaturaUseCase;
import com.financeiramente.core.usecase.fatura.AtualizarStatusFaturasUseCase;
import com.financeiramente.core.usecase.lancamento.DeletarCompraCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.CancelarRecorrenciaCartaoUseCase;
import com.financeiramente.core.usecase.lancamento.ListarComprasCartaoUseCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaturaListViewModel extends ViewModel {

    private final FaturaRepository faturaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final AtualizarStatusFaturasUseCase atualizarStatus;
    private final AlterarStatusFaturaUseCase alterarStatusFatura;
    private final ListarComprasCartaoUseCase listarComprasCartaoUseCase;
    private final CancelarRecorrenciaCartaoUseCase cancelarRecorrenciaCartaoUseCase;
    private final DeletarCompraCartaoUseCase deletarCompraCartaoUseCase;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<FaturaResumo>> faturas = new MutableLiveData<>();
    private final MutableLiveData<List<CompraCartao>> comprasRecorrentes = new MutableLiveData<>();
    private final MutableLiveData<List<CompraCartao>> comprasParceladas = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    private String cartaoId;

    public FaturaListViewModel(FaturaRepository faturaRepository,
                               LancamentoRepository lancamentoRepository,
                               AtualizarStatusFaturasUseCase atualizarStatus,
                               AlterarStatusFaturaUseCase alterarStatusFatura,
                               ListarComprasCartaoUseCase listarComprasCartaoUseCase,
                               CancelarRecorrenciaCartaoUseCase cancelarRecorrenciaCartaoUseCase,
                               DeletarCompraCartaoUseCase deletarCompraCartaoUseCase) {
        this.faturaRepository = faturaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.atualizarStatus = atualizarStatus;
        this.alterarStatusFatura = alterarStatusFatura;
        this.listarComprasCartaoUseCase = listarComprasCartaoUseCase;
        this.cancelarRecorrenciaCartaoUseCase = cancelarRecorrenciaCartaoUseCase;
        this.deletarCompraCartaoUseCase = deletarCompraCartaoUseCase;
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
                carregarDados();
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void cancelarRecorrencia(String compraCartaoId) {
        executor.execute(() -> {
            try {
                cancelarRecorrenciaCartaoUseCase.executar(compraCartaoId);
                carregarDados();
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void excluirCompraCartao(String compraCartaoId) {
        executor.execute(() -> {
            try {
                deletarCompraCartaoUseCase.executar(compraCartaoId);
                carregarDados();
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void alterarStatus(String faturaId, StatusFatura novoStatus,
                              double valorFatura, double valorPago) {
        executor.execute(() -> {
            try {
                alterarStatusFatura.executar(faturaId, novoStatus, valorFatura, valorPago);
                carregarDados();
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    private void carregarDados() {
        List<Fatura> lista = faturaRepository.listarPorCartao(cartaoId);
        List<FaturaResumo> listaResumo = new ArrayList<>(lista.size());
        for (Fatura fatura : lista) {
            double valorTotal = lancamentoRepository.somarPorFatura(fatura.getId()).doubleValue();
            listaResumo.add(new FaturaResumo(fatura, valorTotal));
        }

        List<CompraCartao> recorrentes = new ArrayList<>();
        List<CompraCartao> parceladas = new ArrayList<>();
        listarComprasCartaoUseCase.executar(cartaoId).forEach(resumo -> {
            CompraCartao compra = resumo.getCompra();
            if (compra.getTipo() == TipoCompraCartao.RECORRENTE) {
                recorrentes.add(compra);
            } else {
                parceladas.add(compra);
            }
        });

        mainHandler.post(() -> {
            faturas.setValue(listaResumo);
            comprasRecorrentes.setValue(recorrentes);
            comprasParceladas.setValue(parceladas);
        });
    }

    public LiveData<List<FaturaResumo>> getFaturas() { return faturas; }
    public LiveData<List<CompraCartao>> getComprasRecorrentes() { return comprasRecorrentes; }
    public LiveData<List<CompraCartao>> getComprasParceladas() { return comprasParceladas; }
    public LiveData<String> getErro() { return erro; }

    public static final class FaturaResumo {
        private final Fatura fatura;
        private final double valorTotal;

        public FaturaResumo(Fatura fatura, double valorTotal) {
            this.fatura = fatura;
            this.valorTotal = valorTotal;
        }

        public Fatura getFatura() {
            return fatura;
        }

        public double getValorTotal() {
            return valorTotal;
        }
    }
}
