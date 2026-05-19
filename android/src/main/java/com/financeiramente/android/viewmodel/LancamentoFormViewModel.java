package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Provisao;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.ProvisaoRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.EditarLancamentoUseCase;
import com.financeiramente.core.usecase.RegistrarLancamentoInput;
import com.financeiramente.core.usecase.RegistrarLancamentoUseCase;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LancamentoFormViewModel extends ViewModel {

    private final RegistrarLancamentoUseCase registrar;
    private final EditarLancamentoUseCase editar;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;
    private final LancamentoRepository lancamentoRepository;
    private final ProvisaoRepository provisaoRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<Categoria>> categorias = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<Provisao>> provisoes = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Lancamento> lancamentoCarregado = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sucesso = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    public LancamentoFormViewModel(RegistrarLancamentoUseCase registrar,
                                   EditarLancamentoUseCase editar,
                                   CategoriaRepository categoriaRepository,
                                   TagRepository tagRepository,
                                   LancamentoRepository lancamentoRepository,
                                   ProvisaoRepository provisaoRepository) {
        this.registrar = registrar;
        this.editar = editar;
        this.categoriaRepository = categoriaRepository;
        this.tagRepository = tagRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.provisaoRepository = provisaoRepository;
        carregarDadosAuxiliares();
    }

    private void carregarDadosAuxiliares() {
        executor.execute(() -> {
            List<Categoria> cats = categoriaRepository.listarTodas();
            List<Tag> ts = tagRepository.listarTodas();
            List<Provisao> provs = provisaoRepository.listarAtivas();
            mainHandler.post(() -> {
                categorias.setValue(cats);
                tags.setValue(ts);
                provisoes.setValue(provs);
            });
        });
    }

    public void carregarLancamento(String id) {
        executor.execute(() -> {
            lancamentoRepository.buscarPorId(id).ifPresent(l ->
                    mainHandler.post(() -> lancamentoCarregado.setValue(l)));
        });
    }

    public void salvar(double valor, TipoLancamento tipo, String data, String descricao,
                       String categoriaId, String provisaoId, List<String> tagIds) {
        executor.execute(() -> {
            try {
                RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                        valor, tipo, data, descricao, categoriaId, null, provisaoId, tagIds);
                registrar.executar(input);
                mainHandler.post(() -> sucesso.setValue(true));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void editar(String id, double valor, TipoLancamento tipo, String data,
                       String descricao, String categoriaId, String provisaoId, List<String> tagIds) {
        executor.execute(() -> {
            try {
                RegistrarLancamentoInput input = new RegistrarLancamentoInput(
                        valor, tipo, data, descricao, categoriaId, null, provisaoId, tagIds);
                editar.executar(id, input);
                mainHandler.post(() -> sucesso.setValue(true));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<List<Categoria>> getCategorias() { return categorias; }
    public LiveData<List<Tag>> getTags() { return tags; }
    public LiveData<List<Provisao>> getProvisoes() { return provisoes; }
    public LiveData<Lancamento> getLancamentoCarregado() { return lancamentoCarregado; }
    public LiveData<Boolean> getSucesso() { return sucesso; }
    public LiveData<String> getErro() { return erro; }

    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}
