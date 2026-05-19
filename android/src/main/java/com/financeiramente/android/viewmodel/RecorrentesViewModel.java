package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.LancamentoRecorrente;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.domain.vo.TipoRecorrencia;
import com.financeiramente.core.repository.LancamentoRecorrenteRepository;
import com.financeiramente.core.usecase.CriarLancamentoRecorrenteUseCase;
import com.financeiramente.core.usecase.DesativarLancamentoRecorrenteUseCase;
import com.financeiramente.core.usecase.EditarLancamentoRecorrenteUseCase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecorrentesViewModel extends ViewModel {

    private final CriarLancamentoRecorrenteUseCase criarRecorrente;
    private final EditarLancamentoRecorrenteUseCase editarRecorrente;
    private final DesativarLancamentoRecorrenteUseCase desativarRecorrente;
    private final LancamentoRecorrenteRepository repository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<LancamentoRecorrente>> recorrentes = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sucesso = new MutableLiveData<>();

    public RecorrentesViewModel(CriarLancamentoRecorrenteUseCase criarRecorrente,
                                 EditarLancamentoRecorrenteUseCase editarRecorrente,
                                 DesativarLancamentoRecorrenteUseCase desativarRecorrente,
                                 LancamentoRecorrenteRepository repository) {
        this.criarRecorrente = criarRecorrente;
        this.editarRecorrente = editarRecorrente;
        this.desativarRecorrente = desativarRecorrente;
        this.repository = repository;
        carregar();
    }

    public void carregar() {
        executor.execute(() -> {
            try {
                List<LancamentoRecorrente> lista = repository.listarAtivos();
                mainHandler.post(() -> recorrentes.setValue(lista));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void criar(String descricao, double valor, TipoLancamento tipo,
                      String categoriaId, TipoRecorrencia recorrencia, Integer dia) {
        executor.execute(() -> {
            try {
                criarRecorrente.executar(descricao, valor, tipo, categoriaId, recorrencia, dia);
                List<LancamentoRecorrente> lista = repository.listarAtivos();
                mainHandler.post(() -> {
                    recorrentes.setValue(lista);
                    sucesso.setValue(true);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void editar(String id, String descricao, double valor, TipoLancamento tipo,
                       String categoriaId, TipoRecorrencia recorrencia, Integer dia) {
        executor.execute(() -> {
            try {
                editarRecorrente.executar(id, descricao, valor, tipo, categoriaId, recorrencia, dia);
                List<LancamentoRecorrente> lista = repository.listarAtivos();
                mainHandler.post(() -> {
                    recorrentes.setValue(lista);
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
                desativarRecorrente.executar(id);
                List<LancamentoRecorrente> lista = repository.listarAtivos();
                mainHandler.post(() -> recorrentes.setValue(lista));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<List<LancamentoRecorrente>> getRecorrentes() { return recorrentes; }
    public LiveData<String> getErro() { return erro; }
    public LiveData<Boolean> getSucesso() { return sucesso; }

    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}
