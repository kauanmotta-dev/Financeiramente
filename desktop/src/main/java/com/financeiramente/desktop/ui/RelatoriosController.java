package com.financeiramente.desktop.ui;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.Lancamento;
import com.financeiramente.core.domain.entity.Tag;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.TagRepository;
import com.financeiramente.core.usecase.relatorio.FiltroRelatorio;
import com.financeiramente.core.usecase.relatorio.GerarRelatorioUseCase;
import com.financeiramente.core.usecase.relatorio.RelatorioResult;
import com.financeiramente.desktop.app.AppContext;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RelatoriosController {

    // Filtros
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFim;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<String> cbTag;
    @FXML private RadioButton rbTodos;
    @FXML private RadioButton rbReceitas;
    @FXML private RadioButton rbDespesas;
    @FXML private Label lblErro;

    // Resumo
    @FXML private Label lblTotalReceitas;
    @FXML private Label lblTotalDespesas;
    @FXML private Label lblSaldo;

    // Tabela por categoria
    @FXML private TableView<CatRow> tvCategorias;
    @FXML private TableColumn<CatRow, String> colCatNome;
    @FXML private TableColumn<CatRow, String> colCatTotal;

    // Tabela de lançamentos
    @FXML private TableView<LancRow> tvLancamentos;
    @FXML private TableColumn<LancRow, String> colData;
    @FXML private TableColumn<LancRow, String> colDescricao;
    @FXML private TableColumn<LancRow, String> colCategoria;
    @FXML private TableColumn<LancRow, String> colTipo;
    @FXML private TableColumn<LancRow, String> colValor;

    private final GerarRelatorioUseCase gerarRelatorio;
    private final CategoriaRepository categoriaRepository;
    private final TagRepository tagRepository;

    private List<Categoria> listaCategorias = new ArrayList<>();
    private List<Tag> listaTags = new ArrayList<>();

    public RelatoriosController() {
        AppContext ctx = AppContext.get();
        this.gerarRelatorio      = ctx.getGerarRelatorioUseCase();
        this.categoriaRepository = ctx.getCoreServices().getCategoriaRepository();
        this.tagRepository       = ctx.getCoreServices().getTagRepository();
    }

    @FXML
    public void initialize() {
        configurarTabelas();
        carregarFiltros();

        // Período padrão: mês atual
        LocalDate hoje = LocalDate.now();
        dpInicio.setValue(hoje.withDayOfMonth(1));
        dpFim.setValue(hoje);
    }

    private void configurarTabelas() {
        colCatNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCatTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
    }

    private void carregarFiltros() {
        Task<Void> task = new Task<>() {
            @Override protected Void call() {
                listaCategorias = categoriaRepository.listarTodas();
                listaTags       = tagRepository.listarTodas();
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            List<String> catNomes = new ArrayList<>();
            catNomes.add("Todas as categorias");
            for (Categoria c : listaCategorias) catNomes.add(c.getNome());
            cbCategoria.setItems(FXCollections.observableArrayList(catNomes));
            cbCategoria.getSelectionModel().selectFirst();

            List<String> tagNomes = new ArrayList<>();
            tagNomes.add("Todas as tags");
            for (Tag t : listaTags) tagNomes.add(t.getNome());
            cbTag.setItems(FXCollections.observableArrayList(tagNomes));
            cbTag.getSelectionModel().selectFirst();
        });
        task.setOnFailed(e -> mostrarErro(task.getException().getMessage()));
        new Thread(task).start();
    }

    @FXML
    private void filtrarSemana() {
        LocalDate hoje = LocalDate.now();
        dpInicio.setValue(hoje.minusDays(6));
        dpFim.setValue(hoje);
        executarRelatorio();
    }

    @FXML
    private void filtrarMes() {
        LocalDate hoje = LocalDate.now();
        dpInicio.setValue(hoje.withDayOfMonth(1));
        dpFim.setValue(hoje);
        executarRelatorio();
    }

    @FXML
    private void aplicarFiltros() {
        executarRelatorio();
    }

    private void executarRelatorio() {
        lblErro.setVisible(false);

        if (dpInicio.getValue() == null || dpFim.getValue() == null) {
            mostrarErro("Informe as datas de início e fim.");
            return;
        }

        String dataInicio = dpInicio.getValue().toString();
        String dataFim    = dpFim.getValue().toString();

        String categoriaId = null;
        int catIdx = cbCategoria.getSelectionModel().getSelectedIndex();
        if (catIdx > 0 && catIdx - 1 < listaCategorias.size()) {
            categoriaId = listaCategorias.get(catIdx - 1).getId();
        }

        String tagId = null;
        int tagIdx = cbTag.getSelectionModel().getSelectedIndex();
        if (tagIdx > 0 && tagIdx - 1 < listaTags.size()) {
            tagId = listaTags.get(tagIdx - 1).getId();
        }

        TipoLancamento tipo = null;
        if (rbReceitas.isSelected()) tipo = TipoLancamento.RECEITA;
        else if (rbDespesas.isSelected()) tipo = TipoLancamento.DESPESA;

        final FiltroRelatorio filtro = new FiltroRelatorio(dataInicio, dataFim, categoriaId, tagId, tipo);

        Task<RelatorioResult> task = new Task<>() {
            @Override protected RelatorioResult call() {
                return gerarRelatorio.executar(filtro);
            }
        };
        task.setOnSucceeded(e -> atualizarUI(task.getValue()));
        task.setOnFailed(e -> mostrarErro(task.getException().getMessage()));
        new Thread(task).start();
    }

    private void atualizarUI(RelatorioResult res) {
        // Resumo
        lblTotalReceitas.setText(String.format(Locale.getDefault(), "R$ %.2f", res.getTotalReceitas().doubleValue()));
        lblTotalDespesas.setText(String.format(Locale.getDefault(), "R$ %.2f", res.getTotalDespesas().doubleValue()));
        BigDecimal saldo = res.getSaldo();
        lblSaldo.setText(String.format(Locale.getDefault(), "R$ %.2f", saldo.doubleValue()));
        lblSaldo.setStyle(saldo.compareTo(BigDecimal.ZERO) >= 0
                ? "-fx-font-weight: bold; -fx-text-fill: #2E7D32;"
                : "-fx-font-weight: bold; -fx-text-fill: #C62828;");

        // Por categoria
        ObservableList<CatRow> catRows = FXCollections.observableArrayList();
        for (Map.Entry<String, BigDecimal> e : res.getTotalPorCategoria().entrySet()) {
            catRows.add(new CatRow(e.getKey(),
                String.format(Locale.getDefault(), "%.2f", e.getValue().doubleValue())));
        }
        tvCategorias.setItems(catRows);

        // Lançamentos — resolve nomes de categoria em background já feito no use case
        ObservableList<LancRow> lancRows = FXCollections.observableArrayList();
        for (Lancamento l : res.getLancamentos()) {
            lancRows.add(new LancRow(
                    l.getData(),
                    l.getDescricao(),
                    l.getCategoriaId(),
                    l.getTipo().name(),
                    String.format(Locale.getDefault(), "%.2f", l.getValor())));
        }
        tvLancamentos.setItems(lancRows);
    }

    private void mostrarErro(String msg) {
        Platform.runLater(() -> {
            lblErro.setText(msg != null ? msg : "Erro desconhecido.");
            lblErro.setVisible(true);
        });
    }

    // ── Row classes para TableView ──────────────────────────────────────────

    public static class CatRow {
        private final String nome;
        private final String total;
        public CatRow(String nome, String total) { this.nome = nome; this.total = total; }
        public String getNome()  { return nome; }
        public String getTotal() { return total; }
    }

    public static class LancRow {
        private final String data;
        private final String descricao;
        private final String categoria;
        private final String tipo;
        private final String valor;
        public LancRow(String data, String descricao, String categoria, String tipo, String valor) {
            this.data = data; this.descricao = descricao; this.categoria = categoria;
            this.tipo = tipo; this.valor = valor;
        }
        public String getData()       { return data; }
        public String getDescricao()  { return descricao; }
        public String getCategoria()  { return categoria; }
        public String getTipo()       { return tipo; }
        public String getValor()      { return valor; }
    }
}
