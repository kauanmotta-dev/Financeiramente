package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.lancamento.DeletarLancamentoUseCase;
import com.financeiramente.core.usecase.lancamento.ListarLancamentosUseCase;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LancamentosListViewModel extends ViewModel {

    private final ListarLancamentosUseCase listar;
    private final DeletarLancamentoUseCase deletar;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<Lancamento>> lancamentos = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Map<String, Categoria>> categorias = new MutableLiveData<>(Collections.emptyMap());
    private final MutableLiveData<Map<String, List<Tag>>> tagsMap = new MutableLiveData<>(Collections.emptyMap());
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    private int ano;
    private int mes;

    public LancamentosListViewModel(ListarLancamentosUseCase listar,
                                    DeletarLancamentoUseCase deletar,
                                    CategoriaRepository categoriaRepository,
                                    TagRepository tagRepository) {
        this.listar = listar;
        this.deletar = deletar;
        this.categoriaRepository = categoriaRepository;
        this.tagRepository = tagRepository;
        LocalDate hoje = LocalDate.now();
        this.ano = hoje.getYear();
        this.mes = hoje.getMonthValue();
        carregarLancamentos();
    }

    public void carregarLancamentos() {
        executor.execute(() -> {
            List<Lancamento> lista = listar.porMes(ano, mes);

            // Mapa de categorias
            List<Categoria> cats = categoriaRepository.listarTodas();
            Map<String, Categoria> catsMap = new HashMap<>();
            for (Categoria c : cats) catsMap.put(c.getId(), c);

            // Mapa de tags por lancamento
            Map<String, List<Tag>> tMap = new HashMap<>();
            for (Lancamento l : lista) {
                List<Tag> tags = tagRepository.listarPorLancamento(l.getId());
                if (!tags.isEmpty()) tMap.put(l.getId(), tags);
            }

            mainHandler.post(() -> {
                lancamentos.setValue(lista);
                categorias.setValue(catsMap);
                tagsMap.setValue(tMap);
            });
        });
    }

    public void deletarLancamento(String id) {
        executor.execute(() -> {
            try {
                deletar.executar(id);
                List<Lancamento> lista = listar.porMes(ano, mes);
                mainHandler.post(() -> lancamentos.setValue(lista));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public void setMes(int ano, int mes) {
        this.ano = ano;
        this.mes = mes;
        carregarLancamentos();
    }

    public int getAno() { return ano; }
    public int getMes() { return mes; }

    public LiveData<List<Lancamento>> getLancamentos() { return lancamentos; }
    public LiveData<Map<String, Categoria>> getCategorias() { return categorias; }
    public LiveData<Map<String, List<Tag>>> getTagsMap() { return tagsMap; }
    public LiveData<String> getErro() { return erro; }

    @Override
    protected void onCleared() {
        executor.shutdown();
    }
}

