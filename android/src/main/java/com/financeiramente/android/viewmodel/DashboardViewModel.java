package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.usecase.CalcularSaldoMensalUseCase;
import com.financeiramente.core.usecase.SaldoMensalResult;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardViewModel extends ViewModel {

    private final CalcularSaldoMensalUseCase calcularSaldo;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<SaldoMensalResult> saldoMensal = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    private int ano;
    private int mes;

    public DashboardViewModel(CalcularSaldoMensalUseCase calcularSaldo) {
        this.calcularSaldo = calcularSaldo;
        LocalDate hoje = LocalDate.now();
        this.ano = hoje.getYear();
        this.mes = hoje.getMonthValue();
        carregarDashboard();
    }

    public void carregarDashboard() {
        executor.execute(() -> {
            try {
                SaldoMensalResult resultado = calcularSaldo.executar(ano, mes);
                mainHandler.post(() -> saldoMensal.setValue(resultado));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void setMes(int ano, int mes) {
        this.ano = ano;
        this.mes = mes;
        carregarDashboard();
    }

    public int getAno()  { return ano; }
    public int getMes()  { return mes; }

    public LiveData<SaldoMensalResult> getSaldoMensal() { return saldoMensal; }
    public LiveData<String> getErro()                   { return erro; }

    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}
