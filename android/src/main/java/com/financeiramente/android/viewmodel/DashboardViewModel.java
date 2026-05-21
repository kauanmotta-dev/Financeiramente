package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.AporteMeta;
import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.usecase.CalcularSaldoMensalUseCase;
import com.financeiramente.core.usecase.DeletarLancamentoUseCase;
import com.financeiramente.core.usecase.SaldoMensalResult;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardViewModel extends ViewModel {

    private final CalcularSaldoMensalUseCase calcularSaldo;
    private final DeletarLancamentoUseCase deletar;
    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteMetaRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<SaldoMensalResult> saldoMensal = new MutableLiveData<>();
    private final MutableLiveData<Double> percentualMetas = new MutableLiveData<>(0.0);
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    private int ano;
    private int mes;

    public DashboardViewModel(CalcularSaldoMensalUseCase calcularSaldo,
                              DeletarLancamentoUseCase deletar,
                              MetaRepository metaRepository,
                              AporteMetaRepository aporteMetaRepository) {
        this.calcularSaldo = calcularSaldo;
        this.deletar = deletar;
        this.metaRepository = metaRepository;
        this.aporteMetaRepository = aporteMetaRepository;
        LocalDate hoje = LocalDate.now();
        this.ano = hoje.getYear();
        this.mes = hoje.getMonthValue();
        carregarDashboard();
    }

    public void carregarDashboard() {
        executor.execute(() -> {
            try {
                SaldoMensalResult resultado = calcularSaldo.executar(ano, mes);
                List<Meta> metasAtivas = metaRepository.listarAtivas();
                double percentual = calcularPercentualMetas(metasAtivas, resultado.getReceitaRealizada(), ano, mes);
                mainHandler.post(() -> {
                    saldoMensal.setValue(resultado);
                    percentualMetas.setValue(percentual);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    private double calcularPercentualMetas(List<Meta> metasAtivas, double receitaMensal, int ano, int mes) {
        if (metasAtivas == null || metasAtivas.isEmpty()) return 0.0;
        if (receitaMensal <= 0.0) return 0.0;

        String prefixoMes = String.format("%04d-%02d", ano, mes);
        double totalAportesMes = 0.0;

        for (Meta meta : metasAtivas) {
            List<AporteMeta> aportes = aporteMetaRepository.listarPorMeta(meta.getId());
            if (aportes == null || aportes.isEmpty()) continue;
            for (AporteMeta aporte : aportes) {
                String data = aporte.getData();
                if (data != null && data.startsWith(prefixoMes)) {
                    totalAportesMes += aporte.getValor();
                }
            }
        }

        return (totalAportesMes / receitaMensal) * 100.0;
    }

    public void setMes(int ano, int mes) {
        this.ano = ano;
        this.mes = mes;
        carregarDashboard();
    }

    public int getAno()  { return ano; }
    public int getMes()  { return mes; }

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

    public LiveData<SaldoMensalResult> getSaldoMensal() { return saldoMensal; }
    public LiveData<Double> getPercentualMetas()        { return percentualMetas; }
    public LiveData<String> getErro()                   { return erro; }

    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}
