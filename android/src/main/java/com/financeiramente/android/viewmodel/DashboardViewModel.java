package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.CartaoCredito;
import com.financeiramente.core.repository.CartaoCreditoRepository;
import com.financeiramente.core.repository.FaturaRepository;
import com.financeiramente.core.usecase.saldo.CalcularSaldoDashboardUseCase;
import com.financeiramente.core.usecase.saldo.SaldoDashboardResult;
import com.financeiramente.core.usecase.meta.CalcularTotalAportesMesUseCase;
import com.financeiramente.core.usecase.lancamento.DeletarLancamentoUseCase;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardViewModel extends ViewModel {

    private final CalcularSaldoDashboardUseCase calcularSaldo;
    private final DeletarLancamentoUseCase deletar;
    private final CalcularTotalAportesMesUseCase calcularTotalAportesMes;
    private final CartaoCreditoRepository cartaoRepository;
    private final FaturaRepository faturaRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<SaldoDashboardResult> saldoDashboard = new MutableLiveData<>();
    private final MutableLiveData<Double> percentualMetas = new MutableLiveData<>(0.0);
    private final MutableLiveData<List<CartaoLimiteResumo>> cartoesLimite = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    private int ano;
    private int mes;

    public DashboardViewModel(CalcularSaldoDashboardUseCase calcularSaldo,
                              DeletarLancamentoUseCase deletar,
                              CalcularTotalAportesMesUseCase calcularTotalAportesMes,
                              CartaoCreditoRepository cartaoRepository,
                              FaturaRepository faturaRepository) {
        this.calcularSaldo = calcularSaldo;
        this.deletar = deletar;
        this.calcularTotalAportesMes = calcularTotalAportesMes;
        this.cartaoRepository = cartaoRepository;
        this.faturaRepository = faturaRepository;
        LocalDate hoje = LocalDate.now();
        this.ano = hoje.getYear();
        this.mes = hoje.getMonthValue();
        carregarDashboard();
    }

    public void carregarDashboard() {
        executor.execute(() -> {
            try {
                SaldoDashboardResult resultado = calcularSaldo.executar(ano, mes);
                BigDecimal totalAportesMes = calcularTotalAportesMes.executar(ano, mes);
                List<CartaoLimiteResumo> resumoCartoes = carregarResumoCartoes();
                double percentual = resultado.getTotalReceita() > 0.0
                        ? totalAportesMes.doubleValue() / resultado.getTotalReceita() * 100.0
                        : 0.0;

                mainHandler.post(() -> {
                    saldoDashboard.setValue(resultado);
                    percentualMetas.setValue(percentual);
                    cartoesLimite.setValue(resumoCartoes);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    private List<CartaoLimiteResumo> carregarResumoCartoes() {
        List<CartaoCredito> cartoes = cartaoRepository.listarAtivos();
        List<CartaoLimiteResumo> resumo = new ArrayList<>();
        for (CartaoCredito cartao : cartoes) {
            if (cartao.getLimite() == null || cartao.getLimite().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            double limite = cartao.getLimite().doubleValue();
            double utilizado = faturaRepository.somarTotalUtilizadoPorCartao(cartao.getId()).doubleValue();
            double percentual = limite > 0.0 ? (utilizado / limite) * 100.0 : 0.0;
            resumo.add(new CartaoLimiteResumo(
                    cartao.getId(),
                    cartao.getNome(),
                    limite,
                    utilizado,
                    percentual));
        }
        return resumo;
    }

    public void setMes(int ano, int mes) {
        this.ano = ano;
        this.mes = mes;
        carregarDashboard();
    }

    public int getAno() { return ano; }
    public int getMes() { return mes; }

    public void deletarLancamento(String id) {
        executor.execute(() -> {
            try {
                deletar.executar(id);
                mainHandler.post(this::carregarDashboard);
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<SaldoDashboardResult> getSaldoDashboard() { return saldoDashboard; }
    public LiveData<Double> getPercentualMetas()             { return percentualMetas; }
    public LiveData<List<CartaoLimiteResumo>> getCartoesLimite() { return cartoesLimite; }
    public LiveData<String> getErro()                         { return erro; }

    @Override
    protected void onCleared() {
        executor.shutdown();
    }

    public static final class CartaoLimiteResumo {
        private final String cartaoId;
        private final String nome;
        private final double limite;
        private final double utilizado;
        private final double percentualUso;

        public CartaoLimiteResumo(String cartaoId, String nome, double limite, double utilizado, double percentualUso) {
            this.cartaoId = cartaoId;
            this.nome = nome;
            this.limite = limite;
            this.utilizado = utilizado;
            this.percentualUso = percentualUso;
        }

        public String getCartaoId() { return cartaoId; }
        public String getNome() { return nome; }
        public double getLimite() { return limite; }
        public double getUtilizado() { return utilizado; }
        public double getPercentualUso() { return percentualUso; }
    }
}

