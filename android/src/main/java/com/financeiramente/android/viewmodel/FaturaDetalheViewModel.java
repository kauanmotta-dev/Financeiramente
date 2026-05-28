package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.usecase.fatura.AtualizarStatusFaturasUseCase;
import com.financeiramente.core.usecase.fatura.FaturaDetalheResult;
import com.financeiramente.core.usecase.fatura.PagarFaturaUseCase;
import com.financeiramente.core.usecase.fatura.PagamentoFaturaResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaturaDetalheViewModel extends ViewModel {

    private final FaturaRepository faturaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final PagarFaturaUseCase pagarFatura;
    private final AtualizarStatusFaturasUseCase atualizarStatus;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<FaturaDetalheResult> detalhe = new MutableLiveData<>();
    private final MutableLiveData<Double> totalFatura = new MutableLiveData<>(0.0);
    private final MutableLiveData<Map<String, Categoria>> categoriasMap = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<PagamentoFaturaResult> resultadoPagamento = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    private String faturaId;

    public FaturaDetalheViewModel(FaturaRepository faturaRepository,
                                   LancamentoRepository lancamentoRepository,
                                   CategoriaRepository categoriaRepository,
                                   PagarFaturaUseCase pagarFatura,
                                   AtualizarStatusFaturasUseCase atualizarStatus) {
        this.faturaRepository = faturaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.categoriaRepository = categoriaRepository;
        this.pagarFatura = pagarFatura;
        this.atualizarStatus = atualizarStatus;
    }

    public void init(String faturaId) {
        this.faturaId = faturaId;
        carregar();
    }

    public void carregar() {
        if (faturaId == null) return;
        executor.execute(() -> {
            try {
                atualizarStatus.executar();
                Fatura fatura = faturaRepository.buscarPorId(faturaId).orElse(null);
                if (fatura == null) return;
                List<Lancamento> lancamentos = lancamentoRepository.listarPorFatura(faturaId);
                double total = lancamentoRepository.somarPorFatura(faturaId).doubleValue();

                List<Categoria> cats = categoriaRepository.listarTodas();
                Map<String, Categoria> catsMap = new HashMap<>();
                for (Categoria c : cats) catsMap.put(c.getId(), c);

                FaturaDetalheResult result = new FaturaDetalheResult(fatura, lancamentos, total);
                mainHandler.post(() -> {
                    detalhe.setValue(result);
                    totalFatura.setValue(total);
                    categoriasMap.setValue(catsMap);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void pagar(double valorFatura, double valorPago) {
        if (faturaId == null) return;
        executor.execute(() -> {
            try {
                PagamentoFaturaResult result = pagarFatura.executar(faturaId, valorFatura, valorPago);
                // Recarregar após pagamento
                Fatura fatura = faturaRepository.buscarPorId(faturaId).orElse(null);
                List<Lancamento> lancamentos = lancamentoRepository.listarPorFatura(faturaId);
                double total = lancamentoRepository.somarPorFatura(faturaId).doubleValue();

                List<Categoria> cats = categoriaRepository.listarTodas();
                Map<String, Categoria> catsMap = new HashMap<>();
                for (Categoria c : cats) catsMap.put(c.getId(), c);

                FaturaDetalheResult detResult = fatura != null ? new FaturaDetalheResult(fatura, lancamentos, total) : null;
                mainHandler.post(() -> {
                    resultadoPagamento.setValue(result);
                    detalhe.setValue(detResult);
                    totalFatura.setValue(total);
                    categoriasMap.setValue(catsMap);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<FaturaDetalheResult> getDetalhe() { return detalhe; }
    public LiveData<Double> getTotalFatura() { return totalFatura; }
    public LiveData<Map<String, Categoria>> getCategoriasMap() { return categoriasMap; }
    public LiveData<PagamentoFaturaResult> getResultadoPagamento() { return resultadoPagamento; }
    public LiveData<String> getErro() { return erro; }
}
