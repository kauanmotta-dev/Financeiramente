package com.financeiramente.desktop.ui;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.LancamentoRecorrente;
import com.financeiramente.core.domain.vo.TipoLancamento;
import com.financeiramente.core.domain.vo.TipoRecorrencia;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.LancamentoRecorrenteRepository;
import com.financeiramente.core.usecase.CriarLancamentoRecorrenteUseCase;
import com.financeiramente.core.usecase.DesativarLancamentoRecorrenteUseCase;
import com.financeiramente.core.usecase.EditarLancamentoRecorrenteUseCase;
import com.financeiramente.desktop.app.AppContext;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;

public class RecorrentesController {

    @FXML private ListView<LancamentoRecorrente> lvRecorrentes;
    @FXML private Label lblTituloForm;
    @FXML private TextField tfDescricao;
    @FXML private TextField tfValor;
    @FXML private RadioButton rbDespesa;
    @FXML private RadioButton rbReceita;
    @FXML private ToggleGroup tgTipo;
    @FXML private Spinner<String> spCategoria;
    @FXML private Spinner<String> spRecorrencia;
    @FXML private TextField tfDia;
    @FXML private Label lblErro;
    @FXML private Label lblStatus;
    @FXML private javafx.scene.control.Button btnEditar;
    @FXML private javafx.scene.control.Button btnDesativar;

    private final CriarLancamentoRecorrenteUseCase criarUseCase;
    private final EditarLancamentoRecorrenteUseCase editarUseCase;
    private final DesativarLancamentoRecorrenteUseCase desativarUseCase;
    private final LancamentoRecorrenteRepository repository;
    private final CategoriaRepository categoriaRepository;

    private List<Categoria> categorias;
    private String editandoId; // null = novo

    private static final String[] RECORRENCIAS = {"Mensal", "Semanal", "Diária", "Anual"};

    public RecorrentesController() {
        AppContext ctx = AppContext.get();
        this.criarUseCase       = ctx.getCriarLancamentoRecorrenteUseCase();
        this.editarUseCase      = ctx.getEditarLancamentoRecorrenteUseCase();
        this.desativarUseCase   = ctx.getDesativarLancamentoRecorrenteUseCase();
        this.repository         = ctx.getLancamentoRecorrenteRepository();
        this.categoriaRepository = ctx.getCategoriaRepository();
    }

    @FXML
    public void initialize() {
        configurarSpinnerRecorrencia();
        configurarListView();
        carregarDados();

        lvRecorrentes.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean temSelecao = sel != null;
            btnEditar.setDisable(!temSelecao);
            btnDesativar.setDisable(!temSelecao);
        });

        limparFormulario();
    }

    private void configurarSpinnerRecorrencia() {
        spRecorrencia.setValueFactory(
                new SpinnerValueFactory.ListSpinnerValueFactory<>(
                        FXCollections.observableArrayList(RECORRENCIAS)));
    }

    private void configurarListView() {
        lvRecorrentes.setCellFactory(lv -> new ListCell<LancamentoRecorrente>() {
            @Override
            protected void updateItem(LancamentoRecorrente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String sinal = item.getTipo() == TipoLancamento.RECEITA ? "+" : "-";
                    setText(String.format("%s  |  %sR$ %.2f  |  %s",
                            item.getDescricao(),
                            sinal,
                            item.getValor(),
                            item.getRecorrencia().name().toLowerCase()));
                }
            }
        });
    }

    private void carregarDados() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                categorias = categoriaRepository.listarTodas();
                List<LancamentoRecorrente> lista = repository.listarAtivos();
                javafx.application.Platform.runLater(() -> {
                    // Popular spinner de categorias
                    List<String> nomes = new java.util.ArrayList<>();
                    for (Categoria c : categorias) nomes.add(c.getNome());
                    spCategoria.setValueFactory(
                            new SpinnerValueFactory.ListSpinnerValueFactory<>(
                                    FXCollections.observableArrayList(nomes)));

                    lvRecorrentes.setItems(FXCollections.observableArrayList(lista));
                    lblStatus.setText("Total: " + lista.size() + " recorrente(s) ativo(s)");
                });
                return null;
            }
        };
        task.setOnFailed(e -> lblStatus.setText("Erro ao carregar dados."));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void novoRecorrente() {
        editandoId = null;
        limparFormulario();
        lblTituloForm.setText("Novo Recorrente");
    }

    @FXML
    private void editarSelecionado() {
        LancamentoRecorrente sel = lvRecorrentes.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        editandoId = sel.getId();
        lblTituloForm.setText("Editar Recorrente");
        preencherFormulario(sel);
    }

    @FXML
    private void desativarSelecionado() {
        LancamentoRecorrente sel = lvRecorrentes.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Desativar recorrente");
        confirm.setContentText("Desativar \"" + sel.getDescricao() + "\"?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == javafx.scene.control.ButtonType.OK) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        desativarUseCase.executar(sel.getId());
                        return null;
                    }
                };
                task.setOnSucceeded(e -> carregarDados());
                task.setOnFailed(e -> lblStatus.setText("Erro ao desativar."));
                new Thread(task).start();
            }
        });
    }

    @FXML
    private void salvar() {
        lblErro.setVisible(false);
        String descricao = tfDescricao.getText().trim();
        String valorStr  = tfValor.getText().replace(",", ".").trim();
        String diaStr    = tfDia.getText().trim();

        if (descricao.isEmpty()) {
            lblErro.setText("Descrição é obrigatória.");
            lblErro.setVisible(true);
            return;
        }
        double valor;
        try {
            valor = Double.parseDouble(valorStr);
        } catch (NumberFormatException e) {
            lblErro.setText("Valor inválido.");
            lblErro.setVisible(true);
            return;
        }

        if (categorias == null || categorias.isEmpty()) {
            lblErro.setText("Nenhuma categoria disponível.");
            lblErro.setVisible(true);
            return;
        }

        String categoriaId = categorias.get(spCategoria.getValueFactory().getValue() == null ? 0
                : indexOfCategoria(spCategoria.getValue())).getId();
        TipoLancamento tipo = rbReceita.isSelected() ? TipoLancamento.RECEITA : TipoLancamento.DESPESA;
        TipoRecorrencia recorrencia = recorrenciaDaSelecao(spRecorrencia.getValue());
        Integer dia = diaStr.isEmpty() ? null : Integer.parseInt(diaStr);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                if (editandoId == null) {
                    criarUseCase.executar(descricao, valor, tipo, categoriaId, recorrencia, dia);
                } else {
                    editarUseCase.executar(editandoId, descricao, valor, tipo, categoriaId, recorrencia, dia);
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            limparFormulario();
            carregarDados();
        });
        task.setOnFailed(e -> {
            lblErro.setText(task.getException().getMessage());
            lblErro.setVisible(true);
        });
        new Thread(task).start();
    }

    @FXML
    private void cancelar() {
        limparFormulario();
        editandoId = null;
    }

    private void limparFormulario() {
        tfDescricao.clear();
        tfValor.clear();
        tfDia.clear();
        rbDespesa.setSelected(true);
        lblErro.setVisible(false);
        lblTituloForm.setText("Novo Recorrente");
        editandoId = null;
    }

    private void preencherFormulario(LancamentoRecorrente rec) {
        tfDescricao.setText(rec.getDescricao());
        tfValor.setText(String.format(Locale.getDefault(), "%.2f", rec.getValor()));
        if (rec.getDiaRecorrencia() != null) {
            tfDia.setText(String.valueOf(rec.getDiaRecorrencia()));
        }
        if (rec.getTipo() == TipoLancamento.RECEITA) {
            rbReceita.setSelected(true);
        } else {
            rbDespesa.setSelected(true);
        }
        spRecorrencia.getValueFactory().setValue(recorrenciaParaLabel(rec.getRecorrencia()));

        // Selecionar categoria
        if (categorias != null) {
            for (int i = 0; i < categorias.size(); i++) {
                if (categorias.get(i).getId().equals(rec.getCategoriaId())) {
                    spCategoria.getValueFactory().setValue(categorias.get(i).getNome());
                    break;
                }
            }
        }
    }

    private int indexOfCategoria(String nome) {
        if (categorias == null) return 0;
        for (int i = 0; i < categorias.size(); i++) {
            if (categorias.get(i).getNome().equals(nome)) return i;
        }
        return 0;
    }

    private TipoRecorrencia recorrenciaDaSelecao(String label) {
        if (label == null) return TipoRecorrencia.MENSAL;
        switch (label) {
            case "Semanal": return TipoRecorrencia.SEMANAL;
            case "Diária":  return TipoRecorrencia.DIARIA;
            case "Anual":   return TipoRecorrencia.ANUAL;
            default:        return TipoRecorrencia.MENSAL;
        }
    }

    private String recorrenciaParaLabel(TipoRecorrencia r) {
        switch (r) {
            case SEMANAL: return "Semanal";
            case DIARIA:  return "Diária";
            case ANUAL:   return "Anual";
            default:      return "Mensal";
        }
    }
}
