package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Fatura;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.vo.StatusFatura;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.usecase.fatura.AlterarStatusFaturaUseCase;
import com.financeiramente.core.usecase.fatura.AnteciparLancamentosFaturaUseCase;
import com.financeiramente.core.usecase.fatura.AtualizarStatusFaturasUseCase;
import com.financeiramente.core.usecase.fatura.FaturaDetalheResult;
import com.financeiramente.core.usecase.fatura.PagarFaturaUseCase;
import com.financeiramente.core.usecase.fatura.PagamentoFaturaResult;
import com.financeiramente.core.util.DomainException;

import java.math.BigDecimal;
import java.time.YearMonth;
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
    private final AlterarStatusFaturaUseCase alterarStatusFatura;
    private final AnteciparLancamentosFaturaUseCase anteciparLancamentos;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<FaturaDetalheResult> detalhe = new MutableLiveData<>();
    private final MutableLiveData<Double> totalFatura = new MutableLiveData<>(0.0);
    private final MutableLiveData<Map<String, Categoria>> categoriasMap = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<PagamentoFaturaResult> resultadoPagamento = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();
    private final MutableLiveData<Boolean> ehFaturaAtual = new MutableLiveData<>(false);

    private final MutableLiveData<String> faturaAntecipadaId = new MutableLiveData<>();
    private final MutableLiveData<Boolean> faturaAntecipadaExcluida = new MutableLiveData<>();

    private String faturaId;

    public FaturaDetalheViewModel(FaturaRepository faturaRepository,
                                   LancamentoRepository lancamentoRepository,
                                   CategoriaRepository categoriaRepository,
                                   PagarFaturaUseCase pagarFatura,
                                   AtualizarStatusFaturasUseCase atualizarStatus,
                                   AlterarStatusFaturaUseCase alterarStatusFatura,
                                   AnteciparLancamentosFaturaUseCase anteciparLancamentos) {
        this.faturaRepository = faturaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.categoriaRepository = categoriaRepository;
        this.pagarFatura = pagarFatura;
        this.atualizarStatus = atualizarStatus;
        this.alterarStatusFatura = alterarStatusFatura;
        this.anteciparLancamentos = anteciparLancamentos;
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
                boolean faturaAtualDoCartao = isFaturaAtualDoCartao(fatura);
                mainHandler.post(() -> {
                    ehFaturaAtual.setValue(faturaAtualDoCartao);
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
                boolean faturaAtualDoCartao = isFaturaAtualDoCartao(fatura);
                mainHandler.post(() -> {
                    resultadoPagamento.setValue(result);
                    ehFaturaAtual.setValue(faturaAtualDoCartao);
                    detalhe.setValue(detResult);
                    totalFatura.setValue(total);
                    categoriasMap.setValue(catsMap);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void alterarStatus(StatusFatura novoStatus, double valorFatura, double valorPago) {
        if (faturaId == null) return;
        executor.execute(() -> {
            try {
                PagamentoFaturaResult result = alterarStatusFatura.executar(faturaId, novoStatus, valorFatura, valorPago);
                Fatura fatura = faturaRepository.buscarPorId(faturaId).orElse(null);
                List<Lancamento> lancamentos = lancamentoRepository.listarPorFatura(faturaId);
                double total = lancamentoRepository.somarPorFatura(faturaId).doubleValue();
                List<Categoria> cats = categoriaRepository.listarTodas();
                Map<String, Categoria> catsMap = new HashMap<>();
                for (Categoria c : cats) catsMap.put(c.getId(), c);
                FaturaDetalheResult detResult = fatura != null ? new FaturaDetalheResult(fatura, lancamentos, total) : null;
                boolean faturaAtualDoCartao = isFaturaAtualDoCartao(fatura);
                mainHandler.post(() -> {
                    if (result != null) resultadoPagamento.setValue(result);
                    ehFaturaAtual.setValue(faturaAtualDoCartao);
                    detalhe.setValue(detResult);
                    totalFatura.setValue(total);
                    categoriasMap.setValue(catsMap);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void anteciparLancamentos() {
        if (faturaId == null) return;
        executor.execute(() -> {
            try {
                String faturaAtualId = faturaId;
                anteciparLancamentos.executar(faturaAtualId);
                boolean faturaAindaExiste = faturaRepository.buscarPorId(faturaAtualId).isPresent();
                mainHandler.post(() -> {
                    if (faturaAindaExiste) {
                        faturaAntecipadaId.setValue(faturaAtualId);
                    } else {
                        faturaAntecipadaExcluida.setValue(true);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void anteciparLancamento(String lancamentoId) {
        if (lancamentoId == null || lancamentoId.trim().isEmpty()) return;
        executor.execute(() -> {
            try {
                Lancamento lancamento = lancamentoRepository.buscarPorId(lancamentoId)
                        .orElseThrow(() -> new DomainException("Lançamento não encontrado."));

                String faturaOrigemId = lancamento.getFaturaId();
                if (faturaOrigemId == null || faturaOrigemId.trim().isEmpty()) {
                    throw new DomainException("Este lançamento não está vinculado a uma fatura.");
                }

                Fatura faturaOrigem = faturaRepository.buscarPorId(faturaOrigemId)
                        .orElseThrow(() -> new DomainException("Fatura de origem não encontrada."));

                if (faturaOrigem.getStatus() == StatusFatura.PAGO
                        || faturaOrigem.getStatus() == StatusFatura.PAGO_PARCIAL) {
                    throw new DomainException("Não é possível antecipar lançamento de fatura já paga.");
                }

                Fatura faturaDestino = faturaRepository.buscarFaturaAbertaPorCartao(faturaOrigem.getCartaoId())
                        .orElseThrow(() -> new DomainException(
                                "Nenhuma fatura aberta encontrada para o cartão."));

                YearMonth mesOrigem = YearMonth.parse(faturaOrigem.getMes());
                YearMonth mesDestino = YearMonth.parse(faturaDestino.getMes());
                if (!mesOrigem.isAfter(mesDestino)) {
                    throw new DomainException("Apenas lançamentos de faturas futuras podem ser antecipados.");
                }

                lancamento.setFaturaId(faturaDestino.getId());
                lancamento.setAtualizadoEm(System.currentTimeMillis());
                lancamentoRepository.atualizar(lancamento);

                mainHandler.post(() -> faturaAntecipadaId.setValue(faturaOrigemId));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void limparResultadoPagamento() {
        resultadoPagamento.setValue(null);
    }

    public void limparEventoFaturaAntecipada() {
        faturaAntecipadaId.setValue(null);
    }

    public void limparEventoFaturaAntecipadaExcluida() {
        faturaAntecipadaExcluida.setValue(null);
    }

    private boolean isFaturaAtualDoCartao(Fatura fatura) {
        if (fatura == null || fatura.getCartaoId() == null) return false;
        return faturaRepository.buscarFaturaAbertaPorCartao(fatura.getCartaoId())
                .map(aberta -> aberta.getId().equals(fatura.getId()))
                .orElse(false);
    }

    public LiveData<FaturaDetalheResult> getDetalhe() { return detalhe; }
    public LiveData<Double> getTotalFatura() { return totalFatura; }
    public LiveData<Map<String, Categoria>> getCategoriasMap() { return categoriasMap; }
    public LiveData<PagamentoFaturaResult> getResultadoPagamento() { return resultadoPagamento; }
    public LiveData<String> getFaturaAntecipadaId() { return faturaAntecipadaId; }
    public LiveData<Boolean> getFaturaAntecipadaExcluida() { return faturaAntecipadaExcluida; }
    public LiveData<Boolean> getEhFaturaAtual() { return ehFaturaAtual; }
    public LiveData<String> getErro() { return erro; }
}
