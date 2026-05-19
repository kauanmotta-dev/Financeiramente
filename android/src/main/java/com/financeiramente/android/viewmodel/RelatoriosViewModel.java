package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.FiltroRelatorio;
import com.financeiramente.core.usecase.GerarRelatorioUseCase;
import com.financeiramente.core.usecase.RelatorioResult;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RelatoriosViewModel extends ViewModel {

    private final GerarRelatorioUseCase gerarRelatorio;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<RelatorioResult> resultado = new MutableLiveData<>();
    private final MutableLiveData<List<Categoria>> categorias = new MutableLiveData<>();
    private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>();
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    public RelatoriosViewModel(GerarRelatorioUseCase gerarRelatorio,
                               CategoriaRepository categoriaRepository,
                               TagRepository tagRepository) {
        this.gerarRelatorio      = gerarRelatorio;
        this.categoriaRepository = categoriaRepository;
        this.tagRepository       = tagRepository;
        carregarFiltros();
        filtrarMesAtual();
    }

    /** Carrega categorias e tags para os spinners de filtro. */
    public void carregarFiltros() {
        executor.execute(() -> {
            try {
                List<Categoria> cats = categoriaRepository.listarTodas();
                List<Tag> tgs = tagRepository.listarTodas();
                mainHandler.post(() -> {
                    categorias.setValue(cats);
                    tags.setValue(tgs);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    /** Filtra pelo mês atual (atalho padrão). */
    public void filtrarMesAtual() {
        LocalDate hoje = LocalDate.now();
        String inicio = String.format("%04d-%02d-01", hoje.getYear(), hoje.getMonthValue());
        String fim = hoje.toString();
        filtrar(new FiltroRelatorio(inicio, fim, null, null, null));
    }

    /** Filtra pela semana atual (últimos 7 dias). */
    public void filtrarSemanaAtual() {
        LocalDate hoje = LocalDate.now();
        String inicio = hoje.minusDays(6).toString();
        String fim = hoje.toString();
        filtrar(new FiltroRelatorio(inicio, fim, null, null, null));
    }

    /** Executa o relatório com o filtro fornecido. */
    public void filtrar(FiltroRelatorio filtro) {
        executor.execute(() -> {
            try {
                RelatorioResult res = gerarRelatorio.executar(filtro);
                mainHandler.post(() -> resultado.setValue(res));
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<RelatorioResult> getResultado()    { return resultado; }
    public LiveData<List<Categoria>> getCategorias()   { return categorias; }
    public LiveData<List<Tag>> getTags()               { return tags; }
    public LiveData<String> getErro()                  { return erro; }

    @Override
    protected void onCleared() {
        executor.shutdown();
        super.onCleared();
    }
}
