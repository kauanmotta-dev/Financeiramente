package com.financeiramente.android.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.financeiramente.android.ui.common.CategoriaVisualFallback;
import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.vo.TipoCategoria;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRepository;

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

    public enum ModoExibicao {
        GASTOS,
        RECEBIDOS
    }

    public static class SecaoTipoCategoria {
        private final TipoCategoria tipo;
        private final List<GastoCategoriaNode> categorias;
        private final double valorUtilizadoSecao;
        private final double valorTotalSecao;

        public SecaoTipoCategoria(TipoCategoria tipo,
                                  List<GastoCategoriaNode> categorias,
                                  double valorUtilizadoSecao,
                                  double valorTotalSecao) {
            this.tipo = tipo;
            this.categorias = categorias;
            this.valorUtilizadoSecao = valorUtilizadoSecao;
            this.valorTotalSecao = valorTotalSecao;
        }

        public TipoCategoria getTipo() {
            return tipo;
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
        private final List<GastoCategoriaNode> filhas;

        public GastoCategoriaNode(String id,
                                  String nome,
                                  String icone,
                                  String cor,
                                  TipoCategoria tipo,
                                  double valorTotal,
                                  double valorUtilizado,
                                  List<GastoCategoriaNode> filhas) {
            this.id = id;
            this.nome = nome;
            this.icone = icone;
            this.cor = cor;
            this.tipo = tipo;
            this.valorTotal = valorTotal;
            this.valorUtilizado = valorUtilizado;
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

        NodeTotals(GastoCategoriaNode node, double total, double utilizado) {
            this.node = node;
            this.total = total;
            this.utilizado = utilizado;
        }
    }

    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<SecaoTipoCategoria>> secoes = new MutableLiveData<>();
    private final MutableLiveData<String> competenciaLabel = new MutableLiveData<>();
    private final MutableLiveData<Double> totalRecebido = new MutableLiveData<>(0d);
    private final MutableLiveData<Double> totalGasto = new MutableLiveData<>(0d);
    private final MutableLiveData<String> erro = new MutableLiveData<>();

    private final List<SecaoTipoCategoria> secoesCalculadas = new ArrayList<>();
    private int ano;
    private int mes;
    private ModoExibicao modoExibicao = ModoExibicao.GASTOS;

    public GastosCategoriaViewModel(CategoriaRepository categoriaRepository,
                                    LancamentoRepository lancamentoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.lancamentoRepository = lancamentoRepository;

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

                Map<String, Double> utilizadoPorCategoria = new HashMap<>();
                for (Categoria categoria : todas) {
                    double utilizado = lancamentoRepository.somarPorCategoria(
                            categoria.getId(), ano, mes);
                    utilizadoPorCategoria.put(categoria.getId(), utilizado);
                }

                List<GastoCategoriaNode> essenciais = new ArrayList<>();
                List<GastoCategoriaNode> naoEssenciais = new ArrayList<>();
                List<GastoCategoriaNode> receitas = new ArrayList<>();
                double totalEssenciais = 0d;
                double totalNaoEssenciais = 0d;
                double totalReceitas = 0d;
                double limiteEssenciais = 0d;
                double limiteNaoEssenciais = 0d;
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
                    } else if (raiz.getTipo() == TipoCategoria.RECEITA) {
                        receitas.add(totals.node);
                        totalReceitas += totals.utilizado;
                        limiteReceitas += totals.total;
                    }
                }

                List<SecaoTipoCategoria> secoesLocal = new ArrayList<>();
                if (!essenciais.isEmpty()) {
                    secoesLocal.add(new SecaoTipoCategoria(
                            TipoCategoria.ESSENCIAL,
                            essenciais,
                            totalEssenciais,
                            limiteEssenciais));
                }
                if (!naoEssenciais.isEmpty()) {
                    secoesLocal.add(new SecaoTipoCategoria(
                            TipoCategoria.NAO_ESSENCIAL,
                            naoEssenciais,
                            totalNaoEssenciais,
                            limiteNaoEssenciais));
                }
                if (!receitas.isEmpty()) {
                    secoesLocal.add(new SecaoTipoCategoria(
                            TipoCategoria.RECEITA,
                            receitas,
                            totalReceitas,
                            limiteReceitas));
                }

                double recebidoMes = lancamentoRepository.somarPorTipoEMes(
                        TipoLancamento.RECEITA, ano, mes);
                double gastoMes = lancamentoRepository.somarPorTipoEMes(
                        TipoLancamento.DESPESA, ano, mes);

                mainHandler.post(() -> {
                    secoesCalculadas.clear();
                    secoesCalculadas.addAll(secoesLocal);
                    totalRecebido.setValue(recebidoMes);
                    totalGasto.setValue(gastoMes);
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
            if (modoExibicao == ModoExibicao.GASTOS && secao.getTipo() == TipoCategoria.RECEITA) {
                continue;
            }
            if (modoExibicao == ModoExibicao.RECEBIDOS && secao.getTipo() != TipoCategoria.RECEITA) {
                continue;
            }
            filtradas.add(secao);
        }
        secoes.setValue(filtradas);
    }

    private NodeTotals construirNo(Categoria categoria,
                                   Map<String, List<Categoria>> filhasPorPai,
                                   Map<String, Double> utilizadoPorCategoria,
                                   String nomePai) {
        List<Categoria> filhas = filhasPorPai.getOrDefault(categoria.getId(), Collections.emptyList());
        List<GastoCategoriaNode> filhasNo = new ArrayList<>();

        double totalAgregado = categoria.getLimiteMensal() != null ? categoria.getLimiteMensal() : 0d;
        double utilizadoAgregado = utilizadoPorCategoria.getOrDefault(categoria.getId(), 0d);

        for (Categoria filha : filhas) {
            NodeTotals filhaTotals = construirNo(filha, filhasPorPai, utilizadoPorCategoria, categoria.getNome());
            filhasNo.add(filhaTotals.node);
            totalAgregado += filhaTotals.total;
            utilizadoAgregado += filhaTotals.utilizado;
        }

        GastoCategoriaNode node = new GastoCategoriaNode(
                categoria.getId(),
                categoria.getNome(),
                CategoriaVisualFallback.icone(categoria, nomePai),
                CategoriaVisualFallback.cor(categoria, nomePai),
                categoria.getTipo(),
                totalAgregado,
                utilizadoAgregado,
                filhasNo);

        return new NodeTotals(node, totalAgregado, utilizadoAgregado);
    }

    private void atualizarCompetenciaLabel() {
        Month month = Month.of(mes);
        Locale localePtBr = Locale.forLanguageTag("pt-BR");
        String nomeMes = month.getDisplayName(TextStyle.FULL, localePtBr);
        String mesFormatado = nomeMes.substring(0, 1).toUpperCase(localePtBr)
            + nomeMes.substring(1);
        competenciaLabel.setValue(mesFormatado + " " + ano);
    }

    @Override
    protected void onCleared() {
        executor.shutdown();
        super.onCleared();
    }
}
