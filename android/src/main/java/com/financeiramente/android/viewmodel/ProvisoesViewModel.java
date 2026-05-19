package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.Provisao;
import com.financeiramente.core.repository.ProvisaoRepository;
import com.financeiramente.core.usecase.CriarProvisaoUseCase;
import com.financeiramente.core.usecase.DesativarProvisaoUseCase;
import com.financeiramente.core.usecase.EditarProvisaoUseCase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProvisoesViewModel extends ViewModel {

    private final CriarProvisaoUseCase criarProvisao;
    private final EditarProvisaoUseCase editarProvisao;
    private final DesativarProvisaoUseCase desativarProvisao;
    private final ProvisaoRepository repository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<Provisao>> provisoes = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sucesso = new MutableLiveData<>();

    public ProvisoesViewModel(CriarProvisaoUseCase criarProvisao,
                               EditarProvisaoUseCase editarProvisao,
                               DesativarProvisaoUseCase desativarProvisao,
                               ProvisaoRepository repository) {
        this.criarProvisao = criarProvisao;
        this.editarProvisao = editarProvisao;
        this.desativarProvisao = desativarProvisao;
        this.repository = repository;
        carregar();
    }

    public void carregar() {
        executor.execute(() -> {
            try {
                List<Provisao> lista = repository.listarAtivas();
                mainHandler.post(() -> provisoes.setValue(lista));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void criar(String nome, double totalAnual, double valorMensal, String categoriaId) {
        executor.execute(() -> {
            try {
                criarProvisao.executar(nome, totalAnual, valorMensal, categoriaId);
                List<Provisao> lista = repository.listarAtivas();
                mainHandler.post(() -> {
                    provisoes.setValue(lista);
                    sucesso.setValue(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void editar(String id, String nome, double totalAnual, double valorMensal, String categoriaId) {
        executor.execute(() -> {
            try {
                editarProvisao.executar(id, nome, totalAnual, valorMensal, categoriaId);
                List<Provisao> lista = repository.listarAtivas();
                mainHandler.post(() -> {
                    provisoes.setValue(lista);
                    sucesso.setValue(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void desativar(String id) {
        executor.execute(() -> {
            try {
                desativarProvisao.executar(id);
                List<Provisao> lista = repository.listarAtivas();
                mainHandler.post(() -> provisoes.setValue(lista));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<List<Provisao>> getProvisoes() { return provisoes; }
    public LiveData<String> getErro() { return erro; }
    public LiveData<Boolean> getSucesso() { return sucesso; }

    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}
