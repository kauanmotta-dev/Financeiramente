package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.android.ui.common.CategoriaVisualFallback;
import com.financeiramente.core.domain.entity.AporteMeta;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;
import com.financeiramente.core.repository.MetaRepository;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GastosCategoriaViewModel extends ViewModel {

    private static final String SECAO_ID_INVESTIMENTOS = "investimentos";

    public enum ModoExibicao {
        GASTOS,
        RECEBIDOS
    }

    public static class SecaoTipoCategoria {
        private final String id;
        private final TipoCategoria tipo;
        private final String titulo;
        private final boolean investimentos;
        private final List<GastoCategoriaNode> categorias;
        private final double valorUtilizadoSecao;
        private final double valorTotalSecao;

        public SecaoTipoCategoria(String id,
                                  TipoCategoria tipo,
                                  String titulo,
                                  boolean investimentos,
                                  List<GastoCategoriaNode> categorias,
                                  double valorUtilizadoSecao,
                                  double valorTotalSecao) {
            this.id = id;
            this.tipo = tipo;
            this.titulo = titulo;
            this.investimentos = investimentos;
            this.categorias = categorias;
            this.valorUtilizadoSecao = valorUtilizadoSecao;
            this.valorTotalSecao = valorTotalSecao;
        }

        public String getId() {
            return id;
        }

        public TipoCategoria getTipo() {
            return tipo;
        }

        public String getTitulo() {
            return titulo;
        }

        public boolean isInvestimentos() {
            return investimentos;
        }

        public List<GastoCategoriaNode> getCategorias() {
            return categorias;
        }

        public double getValorUtilizadoSecao() {
            return valorUtilizadoSecao;
        }

        public double getValorTotalSecao() {
            return valorTotalSecao;
        }
    }

    public static class GastoCategoriaNode {
        private final String id;
        private final String nome;
        private final String icone;
        private final String cor;
        private final TipoCategoria tipo;
        private final double valorTotal;
        private final double valorUtilizado;
        private final double valorCreditoUtilizado;
        private final double valorNaoCreditoUtilizado;
        private final boolean investimento;
        private final double valorAcumulado;
        private final List<GastoCategoriaNode> filhas;

        public GastoCategoriaNode(String id,
                                  String nome,
                                  String icone,
                                  String cor,
                                  TipoCategoria tipo,
                                  double valorTotal,
                                  double valorUtilizado,
                                  double valorCreditoUtilizado,
                                  double valorNaoCreditoUtilizado,
                                  boolean investimento,
                                  double valorAcumulado,
                                  List<GastoCategoriaNode> filhas) {
            this.id = id;
            this.nome = nome;
            this.icone = icone;
            this.cor = cor;
            this.tipo = tipo;
            this.valorTotal = valorTotal;
            this.valorUtilizado = valorUtilizado;
            this.valorCreditoUtilizado = valorCreditoUtilizado;
            this.valorNaoCreditoUtilizado = valorNaoCreditoUtilizado;
            this.investimento = investimento;
            this.valorAcumulado = valorAcumulado;
            this.filhas = filhas;
        }

        public String getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public String getIcone() {
            return icone;
        }

        public String getCor() {
            return cor;
        }

        public TipoCategoria getTipo() {
            return tipo;
        }

        public double getValorTotal() {
            return valorTotal;
        }

        public double getValorUtilizado() {
            return valorUtilizado;
        }

        public double getValorCreditoUtilizado() {
            return valorCreditoUtilizado;
        }

        public double getValorNaoCreditoUtilizado() {
            return valorNaoCreditoUtilizado;
        }

        public boolean isInvestimento() {
            return investimento;
        }

        public double getValorAcumulado() {
            return valorAcumulado;
        }

        public List<GastoCategoriaNode> getFilhas() {
            return filhas;
        }

        public boolean possuiFilhas() {
            return !filhas.isEmpty();
        }
    }

    private static class NodeTotals {
        final GastoCategoriaNode node;
        final double total;
        final double utilizado;
        final double creditoUtilizado;
        final double naoCreditoUtilizado;

        NodeTotals(GastoCategoriaNode node,
                   double total,
                   double utilizado,
                   double creditoUtilizado,
                   double naoCreditoUtilizado) {
            this.node = node;
            this.total = total;
            this.utilizado = utilizado;
            this.creditoUtilizado = creditoUtilizado;
            this.naoCreditoUtilizado = naoCreditoUtilizado;
        }
    }

    private static class UtilizadoCategoria {
        double utilizado;
        double creditoUtilizado;
        double naoCreditoUtilizado;
    }

    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteMetaRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<SecaoTipoCategoria>> secoes = new MutableLiveData<>();
    private final MutableLiveData<String> competenciaLabel = new MutableLiveData<>();
    private final MutableLiveData<Double> totalRecebido = new MutableLiveData<>(0d);
    private final MutableLiveData<Double> totalGasto = new MutableLiveData<>(0d);
    private final MutableLiveData<Double> totalPrevistoGasto = new MutableLiveData<>(0d);
    private final MutableLiveData<Double> totalInvestido = new MutableLiveData<>(0d);
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    private final List<SecaoTipoCategoria> secoesCalculadas = new ArrayList<>();
    private int ano;
    private int mes;
    private ModoExibicao modoExibicao = ModoExibicao.GASTOS;

    public GastosCategoriaViewModel(CategoriaRepository categoriaRepository,
                                    LancamentoRepository lancamentoRepository,
                                    MetaRepository metaRepository,
                                    AporteMetaRepository aporteMetaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.metaRepository = metaRepository;
        this.aporteMetaRepository = aporteMetaRepository;

        LocalDate hoje = LocalDate.now();
        this.ano = hoje.getYear();
        this.mes = hoje.getMonthValue();
        atualizarCompetenciaLabel();
        carregar();
    }

    public LiveData<List<SecaoTipoCategoria>> getSecoes() {
        return secoes;
    }

    public LiveData<String> getCompetenciaLabel() {
        return competenciaLabel;
    }

    public LiveData<Double> getTotalRecebido() {
        return totalRecebido;
    }

    public LiveData<Double> getTotalGasto() {
        return totalGasto;
    }

    public LiveData<Double> getTotalPrevistoGasto() {
        return totalPrevistoGasto;
    }

    public LiveData<Double> getTotalInvestido() {
        return totalInvestido;
    }

    public LiveData<String> getErro() {
        return erro;
    }

    public void setModoExibicao(ModoExibicao modoExibicao) {
        this.modoExibicao = modoExibicao;
        publicarSecoesPorModo();
    }

    public void proximoMes() {
        if (mes == 12) {
            mes = 1;
            ano++;
        } else {
            mes++;
        }
        atualizarCompetenciaLabel();
        carregar();
    }

    public void mesAnterior() {
        if (mes == 1) {
            mes = 12;
            ano--;
        } else {
            mes--;
        }
        atualizarCompetenciaLabel();
        carregar();
    }

    public void carregar() {
        executor.execute(() -> {
            try {
                List<Categoria> todas = categoriaRepository.listarTodas();
                List<Categoria> raizes = categoriaRepository.listarRaizes();

                Map<String, List<Categoria>> filhasPorPai = new HashMap<>();
                for (Categoria categoria : todas) {
                    if (categoria.getPaiId() == null) {
                        continue;
                    }
                    List<Categoria> filhas = filhasPorPai.computeIfAbsent(
                            categoria.getPaiId(), key -> new ArrayList<>());
                    filhas.add(categoria);
                }
                for (List<Categoria> filhas : filhasPorPai.values()) {
                    filhas.sort(Comparator.comparingInt(Categoria::getOrdem));
                }

                Map<String, UtilizadoCategoria> utilizadoPorCategoria = new HashMap<>();
                List<Lancamento> lancamentosMes = lancamentoRepository.listarPorMes(ano, mes);
                for (Lancamento lancamento : lancamentosMes) {
                    if (ehLancamentoDerivadoDeFatura(lancamento)) {
                        continue;
                    }

                    String categoriaId = lancamento.getCategoriaId();
                    if (categoriaId == null || categoriaId.trim().isEmpty()) {
                        continue;
                    }
                    UtilizadoCategoria utilizado = utilizadoPorCategoria.computeIfAbsent(
                            categoriaId, key -> new UtilizadoCategoria());
                    double valor = lancamento.getValor().doubleValue();
                    utilizado.utilizado += valor;

                    boolean gastoCredito = lancamento.getTipo() == TipoLancamento.DESPESA
                            && lancamento.getCompraCartaoId() != null
                            && !lancamento.getCompraCartaoId().trim().isEmpty();
                    if (gastoCredito) {
                        utilizado.creditoUtilizado += valor;
                    } else {
                        utilizado.naoCreditoUtilizado += valor;
                    }
                }

                List<GastoCategoriaNode> essenciais = new ArrayList<>();
                List<GastoCategoriaNode> naoEssenciais = new ArrayList<>();
                List<GastoCategoriaNode> semCategoria = new ArrayList<>();
                List<GastoCategoriaNode> receitas = new ArrayList<>();
                List<GastoCategoriaNode> investimentos = new ArrayList<>();
                double totalEssenciais = 0d;
                double totalNaoEssenciais = 0d;
                double totalSemCategoria = 0d;
                double totalReceitas = 0d;
                double totalInvestidoMes = 0d;
                double limiteEssenciais = 0d;
                double limiteNaoEssenciais = 0d;
                double limiteSemCategoria = 0d;
                double limiteReceitas = 0d;

                raizes.sort(Comparator.comparingInt(Categoria::getOrdem));
                for (Categoria raiz : raizes) {
                    NodeTotals totals = construirNo(raiz, filhasPorPai, utilizadoPorCategoria, null);
                    if (raiz.getTipo() == TipoCategoria.ESSENCIAL) {
                        essenciais.add(totals.node);
                        totalEssenciais += totals.utilizado;
                        limiteEssenciais += totals.total;
                    } else if (raiz.getTipo() == TipoCategoria.NAO_ESSENCIAL) {
                        naoEssenciais.add(totals.node);
                        totalNaoEssenciais += totals.utilizado;
                        limiteNaoEssenciais += totals.total;
                    } else if (raiz.getTipo() == TipoCategoria.SEM_TIPO) {
                        semCategoria.add(totals.node);
                        totalSemCategoria += totals.utilizado;
                        limiteSemCategoria += totals.total;
                    } else if (raiz.getTipo() == TipoCategoria.RECEITA) {
                        receitas.add(totals.node);
                        totalReceitas += totals.utilizado;
                        limiteReceitas += totals.total;
                    }
                }

                List<Meta> metasAtivas = metaRepository.listarAtivas();
                if (metasAtivas != null) {
                    metasAtivas.sort(Comparator.comparing(Meta::getNome, String.CASE_INSENSITIVE_ORDER));
                    String prefixoMes = String.format(Locale.US, "%04d-%02d", ano, mes);
                    for (Meta meta : metasAtivas) {
                        double investidoMesMeta = 0d;
                        List<AporteMeta> aportes = aporteMetaRepository.listarPorMeta(meta.getId());
                        if (aportes != null) {
                            for (AporteMeta aporte : aportes) {
                                String data = aporte.getData();
                                if (data != null && data.startsWith(prefixoMes)) {
                                    investidoMesMeta += aporte.getValor().doubleValue();
                                }
                            }
                        }

                        double valorObjetivo = meta.getValorObjetivo() != null
                                ? meta.getValorObjetivo().doubleValue() : 0d;
                        double valorAtual = meta.getValorAtual() != null
                                ? meta.getValorAtual().doubleValue() : 0d;
                        investimentos.add(new GastoCategoriaNode(
                                meta.getId(),
                                meta.getNome(),
                                "🎯",
                                "#FB923C",
                                TipoCategoria.RECEITA,
                                valorObjetivo,
                                investidoMesMeta,
                                0d,
                                0d,
                                true,
                                valorAtual,
                                Collections.emptyList()));
                        totalInvestidoMes += investidoMesMeta;
                    }
                }

                List<SecaoTipoCategoria> secoesLocal = new ArrayList<>();
                if (!essenciais.isEmpty()) {
                    secoesLocal.add(new SecaoTipoCategoria(
                            TipoCategoria.ESSENCIAL.name(),
                            TipoCategoria.ESSENCIAL,
                            null,
                            false,
                            essenciais,
                            totalEssenciais,
                            limiteEssenciais));
                }
                if (!naoEssenciais.isEmpty()) {
                    secoesLocal.add(new SecaoTipoCategoria(
                            TipoCategoria.NAO_ESSENCIAL.name(),
                            TipoCategoria.NAO_ESSENCIAL,
                            null,
                            false,
                            naoEssenciais,
                            totalNaoEssenciais,
                            limiteNaoEssenciais));
                }
                if (!semCategoria.isEmpty()) {
                    secoesLocal.add(new SecaoTipoCategoria(
                            TipoCategoria.SEM_TIPO.name(),
                            TipoCategoria.SEM_TIPO,
                            null,
                            false,
                            semCategoria,
                            totalSemCategoria,
                            limiteSemCategoria));
                }
                if (!receitas.isEmpty()) {
                    secoesLocal.add(new SecaoTipoCategoria(
                            TipoCategoria.RECEITA.name(),
                            TipoCategoria.RECEITA,
                            null,
                            false,
                            receitas,
                            totalReceitas,
                            limiteReceitas));
                }
                if (!investimentos.isEmpty()) {
                    secoesLocal.add(new SecaoTipoCategoria(
                            SECAO_ID_INVESTIMENTOS,
                            TipoCategoria.RECEITA,
                            "Investimentos",
                            true,
                            investimentos,
                            totalInvestidoMes,
                            0d));
                }

                double recebidoMes = lancamentoRepository.somarPorTipoEMes(
                        TipoLancamento.RECEITA, ano, mes).doubleValue();
                double gastoMes = lancamentoRepository.somarPorTipoEMes(
                        TipoLancamento.DESPESA, ano, mes).doubleValue();
                double totalPrevistoMes = limiteEssenciais + limiteNaoEssenciais;
                double totalInvestidoMesFinal = totalInvestidoMes;

                mainHandler.post(() -> {
                    secoesCalculadas.clear();
                    secoesCalculadas.addAll(secoesLocal);
                    totalRecebido.setValue(recebidoMes);
                    totalGasto.setValue(gastoMes);
                    totalPrevistoGasto.setValue(totalPrevistoMes);
                    totalInvestido.setValue(totalInvestidoMesFinal);
                    publicarSecoesPorModo();
                });
            } catch (Exception e) {
                mainHandler.post(() -> erro.setValue(e.getMessage()));
            }
        });
    }

    private void publicarSecoesPorModo() {
        List<SecaoTipoCategoria> filtradas = new ArrayList<>();
        for (SecaoTipoCategoria secao : secoesCalculadas) {
            if (modoExibicao == ModoExibicao.GASTOS
                    && (secao.getTipo() == TipoCategoria.RECEITA || secao.isInvestimentos())) {
                continue;
            }
            if (modoExibicao == ModoExibicao.RECEBIDOS
                    && secao.getTipo() != TipoCategoria.RECEITA
                    && !secao.isInvestimentos()) {
                continue;
            }
            filtradas.add(secao);
        }
        secoes.setValue(filtradas);
    }

    private boolean ehLancamentoDerivadoDeFatura(Lancamento lancamento) {
        if (lancamentoRepository.buscarFaturaIdPorLancamentoPagamento(lancamento.getId()).isPresent()) {
            return true;
        }

        String descricao = lancamento.getDescricao();
        if (descricao == null) {
            return false;
        }

        String normalizada = descricao.trim().toLowerCase(Locale.ROOT);
        return "ajuste de fatura".equals(normalizada)
                || normalizada.startsWith("saldo anterior (")
                || normalizada.startsWith("pagamento ");
    }

    private NodeTotals construirNo(Categoria categoria,
                                   Map<String, List<Categoria>> filhasPorPai,
                                   Map<String, UtilizadoCategoria> utilizadoPorCategoria,
                                   String nomePai) {
        List<Categoria> filhas = filhasPorPai.getOrDefault(categoria.getId(), Collections.emptyList());
        List<GastoCategoriaNode> filhasNo = new ArrayList<>();

        double limiteProprio = categoria.getLimiteMensal() != null ? categoria.getLimiteMensal().doubleValue() : 0d;
        double somaLimitesFilhas = 0d;
        UtilizadoCategoria utilizadoDireto = utilizadoPorCategoria.get(categoria.getId());
        double utilizadoAgregado = utilizadoDireto != null ? utilizadoDireto.utilizado : 0d;
        double creditoAgregado = utilizadoDireto != null ? utilizadoDireto.creditoUtilizado : 0d;
        double naoCreditoAgregado = utilizadoDireto != null ? utilizadoDireto.naoCreditoUtilizado : 0d;

        for (Categoria filha : filhas) {
            NodeTotals filhaTotals = construirNo(filha, filhasPorPai, utilizadoPorCategoria, categoria.getNome());
            filhasNo.add(filhaTotals.node);
            somaLimitesFilhas += filhaTotals.total;
            utilizadoAgregado += filhaTotals.utilizado;
            creditoAgregado += filhaTotals.creditoUtilizado;
            naoCreditoAgregado += filhaTotals.naoCreditoUtilizado;
        }

        // Regra de negócio: limite efetivo é o limite da própria categoria,
        // ou (se não existir) a soma dos limites das subcategorias.
        double totalAgregado = limiteProprio > 0d ? limiteProprio : somaLimitesFilhas;

        GastoCategoriaNode node = new GastoCategoriaNode(
                categoria.getId(),
                categoria.getNome(),
                CategoriaVisualFallback.icone(categoria, nomePai),
                CategoriaVisualFallback.cor(categoria, nomePai),
                categoria.getTipo(),
                totalAgregado,
                utilizadoAgregado,
                creditoAgregado,
                naoCreditoAgregado,
                false,
                0d,
                filhasNo);

            return new NodeTotals(node,
                totalAgregado,
                utilizadoAgregado,
                creditoAgregado,
                naoCreditoAgregado);
    }

    private void atualizarCompetenciaLabel() {
        Month month = Month.of(mes);
        Locale localePtBr = Locale.forLanguageTag("pt-BR");
        String nomeMes = month.getDisplayName(TextStyle.SHORT, localePtBr);
        String mesFormatado = nomeMes.replace(".", "").toUpperCase(localePtBr);
        if (mesFormatado.length() > 3) {
            mesFormatado = mesFormatado.substring(0, 3);
        }
        int anoCurto = ano % 100;
        competenciaLabel.setValue(String.format(localePtBr, "%s %02d", mesFormatado, anoCurto));
    }

    @Override
    protected void onCleared() {
        executor.shutdown();
        super.onCleared();
    }
}
