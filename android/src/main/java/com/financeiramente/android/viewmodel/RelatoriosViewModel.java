package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.core.domain.entity.AporteMeta;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.relatorio.FiltroRelatorio;
import com.financeiramente.core.usecase.relatorio.GerarRelatorioUseCase;
import com.financeiramente.core.usecase.relatorio.RelatorioResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RelatoriosViewModel extends ViewModel {

    private final GerarRelatorioUseCase gerarRelatorio;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;
    private final LancamentoRepository lancamentoRepository;
    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteMetaRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<RelatorioResult> resultado = new MutableLiveData<>();
    private final MutableLiveData<List<Categoria>> categorias = new MutableLiveData<>();
    private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Categoria>> categoriasMap = new MutableLiveData<>(new LinkedHashMap<>());
    private final MutableLiveData<Map<String, List<Tag>>> tagsPorLancamento = new MutableLiveData<>(new LinkedHashMap<>());
    private final MutableLiveData<BigDecimal> totalInvestidoPeriodo = new MutableLiveData<>(BigDecimal.ZERO);
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    // Dados históricos dos últimos 6 meses para os gráficos de barras e linha
    private final MutableLiveData<float[]> receitasMensais = new MutableLiveData<>();
    private final MutableLiveData<float[]> despesasMensais = new MutableLiveData<>();
    private final MutableLiveData<String[]> labelsMeses = new MutableLiveData<>();

    public RelatoriosViewModel(GerarRelatorioUseCase gerarRelatorio,
                               CategoriaRepository categoriaRepository,
                               TagRepository tagRepository,
                               LancamentoRepository lancamentoRepository,
                               MetaRepository metaRepository,
                               AporteMetaRepository aporteMetaRepository) {
        this.gerarRelatorio       = gerarRelatorio;
        this.categoriaRepository  = categoriaRepository;
        this.tagRepository        = tagRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.metaRepository = metaRepository;
        this.aporteMetaRepository = aporteMetaRepository;
        carregarFiltros();
        filtrarMesAtual();
        carregarDadosMensais();
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

    /** Carrega receitas e despesas dos últimos 6 meses para os gráficos históricos. */
    public void carregarDadosMensais() {
        executor.execute(() -> {
            try {
                LocalDate hoje = LocalDate.now();
                String[] mesesAbrev = {"Jan","Fev","Mar","Abr","Mai","Jun",
                                       "Jul","Ago","Set","Out","Nov","Dez"};
                float[] receitas = new float[6];
                float[] despesas = new float[6];
                String[] labels  = new String[6];
                for (int i = 5; i >= 0; i--) {
                    LocalDate mes = hoje.minusMonths(i);
                    receitas[5 - i] = lancamentoRepository.somarPorTipoEMes(
                            TipoLancamento.RECEITA, mes.getYear(), mes.getMonthValue()).floatValue();
                    despesas[5 - i] = lancamentoRepository.somarPorTipoEMes(
                            TipoLancamento.DESPESA, mes.getYear(), mes.getMonthValue()).floatValue();
                    labels[5 - i] = mesesAbrev[mes.getMonthValue() - 1];
                }
                mainHandler.post(() -> {
                    receitasMensais.setValue(receitas);
                    despesasMensais.setValue(despesas);
                    labelsMeses.setValue(labels);
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

    /** Filtra pelo ano atual. */
    public void filtrarAnoAtual() {
        LocalDate hoje = LocalDate.now();
        String inicio = String.format("%04d-01-01", hoje.getYear());
        String fim = hoje.toString();
        filtrar(new FiltroRelatorio(inicio, fim, null, null, null));
    }

    /** Executa o relatório com o filtro fornecido. */
    public void filtrar(FiltroRelatorio filtro) {
        executor.execute(() -> {
            try {
                RelatorioResult res = gerarRelatorio.executar(filtro);
                List<Categoria> todasCategorias = categoriaRepository.listarTodas();
                Map<String, Categoria> mapCategorias = new LinkedHashMap<>();
                for (Categoria categoria : todasCategorias) {
                    mapCategorias.put(categoria.getId(), categoria);
                }

                Map<String, List<Tag>> mapTags = new LinkedHashMap<>();
                for (com.financeiramente.core.domain.entity.Lancamento lancamento : res.getLancamentos()) {
                    mapTags.put(lancamento.getId(), tagRepository.listarPorLancamento(lancamento.getId()));
                }
                BigDecimal totalInvestido = calcularTotalInvestidoNoPeriodo(
                        filtro.getDataInicio(),
                        filtro.getDataFim());

                mainHandler.post(() -> {
                    categoriasMap.setValue(mapCategorias);
                    tagsPorLancamento.setValue(mapTags);
                    totalInvestidoPeriodo.setValue(totalInvestido);
                    resultado.setValue(res);
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    public LiveData<RelatorioResult> getResultado()   { return resultado; }
    public LiveData<List<Categoria>> getCategorias()  { return categorias; }
    public LiveData<List<Tag>> getTags()              { return tags; }
    public LiveData<Map<String, Categoria>> getCategoriasMap() { return categoriasMap; }
    public LiveData<Map<String, List<Tag>>> getTagsPorLancamento() { return tagsPorLancamento; }
    public LiveData<BigDecimal> getTotalInvestidoPeriodo() { return totalInvestidoPeriodo; }
    public LiveData<String> getErro()                 { return erro; }
    public LiveData<float[]> getReceitasMensais()     { return receitasMensais; }
    public LiveData<float[]> getDespesasMensais()     { return despesasMensais; }
    public LiveData<String[]> getLabelsMeses()        { return labelsMeses; }

    @Override
    protected void onCleared() {
        executor.shutdown();
        super.onCleared();
    }

    private BigDecimal calcularTotalInvestidoNoPeriodo(String dataInicio, String dataFim) {
        LocalDate inicio = LocalDate.parse(dataInicio);
        LocalDate fim = LocalDate.parse(dataFim);
        BigDecimal total = BigDecimal.ZERO;
        List<Meta> metasAtivas = metaRepository.listarAtivas();
        if (metasAtivas == null || metasAtivas.isEmpty()) {
            return total;
        }

        for (Meta meta : metasAtivas) {
            List<AporteMeta> aportes = aporteMetaRepository.listarPorMeta(meta.getId());
            if (aportes == null || aportes.isEmpty()) {
                continue;
            }
            for (AporteMeta aporte : aportes) {
                String dataAporte = aporte.getData();
                if (dataAporte == null || dataAporte.trim().isEmpty()) {
                    continue;
                }
                LocalDate data = LocalDate.parse(dataAporte.trim());
                if ((data.isEqual(inicio) || data.isAfter(inicio))
                        && (data.isEqual(fim) || data.isBefore(fim))) {
                    total = total.add(aporte.getValor());
                }
            }
        }
        return total;
    }
}
