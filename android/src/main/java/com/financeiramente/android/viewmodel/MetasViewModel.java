package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.usecase.meta.CriarMetaUseCase;
import com.financeiramente.core.usecase.meta.DeletarMetaUseCase;
import com.financeiramente.core.usecase.meta.EditarMetaUseCase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MetasViewModel extends ViewModel {

    private final CriarMetaUseCase criarMeta;
    private final EditarMetaUseCase editarMeta;
    private final DeletarMetaUseCase deletarMeta;
    private final MetaRepository repository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<Meta>> metas = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sucesso = new MutableLiveData<>();

    public MetasViewModel(CriarMetaUseCase criarMeta,
                          EditarMetaUseCase editarMeta,
                          DeletarMetaUseCase deletarMeta,
                          MetaRepository repository) {
        this.criarMeta = criarMeta;
        this.editarMeta = editarMeta;
        this.deletarMeta = deletarMeta;
        this.repository = repository;
        carregar();
    }

    public void carregar() {
        executor.execute(() -> {
            try {
                List<Meta> lista = repository.listarAtivas();
                mainHandler.post(() -> metas.setValue(lista));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void criar(String nome, double valorObjetivo, double valorInicial, String dataAlvo, String descricao) {
        executor.execute(() -> {
            try {
                criarMeta.executar(nome, valorObjetivo, valorInicial, dataAlvo, descricao);
                List<Meta> lista = repository.listarAtivas();
                mainHandler.post(() -> {
                    metas.setValue(lista);
                    sucesso.setValue(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void editar(String id, String nome, double valorObjetivo, double valorInicial, String dataAlvo, String descricao) {
        executor.execute(() -> {
            try {
                editarMeta.executar(id, nome, valorObjetivo, valorInicial, dataAlvo, descricao);
                List<Meta> lista = repository.listarAtivas();
                mainHandler.post(() -> {
                    metas.setValue(lista);
                    sucesso.setValue(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void excluir(String id) {
        executor.execute(() -> {
            try {
                deletarMeta.executar(id);
                List<Meta> lista = repository.listarAtivas();
                mainHandler.post(() -> metas.setValue(lista));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<List<Meta>> getMetas() { return metas; }
    public LiveData<String> getErro() { return erro; }
    public LiveData<Boolean> getSucesso() { return sucesso; }
}
