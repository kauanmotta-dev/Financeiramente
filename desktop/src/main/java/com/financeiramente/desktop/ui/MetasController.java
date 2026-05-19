package com.financeiramente.desktop.ui;

import com.financeiramente.core.domain.entity.AporteMeta;
import com.financeiramente.core.domain.entity.Meta;
import com.financeiramente.core.repository.AporteMetaRepository;
import com.financeiramente.core.repository.MetaRepository;
import com.financeiramente.core.usecase.CalcularProjecaoMetaUseCase;
import com.financeiramente.core.usecase.CriarMetaUseCase;
import com.financeiramente.core.usecase.DeletarAporteMetaUseCase;
import com.financeiramente.core.usecase.DesativarMetaUseCase;
import com.financeiramente.core.usecase.EditarMetaUseCase;
import com.financeiramente.core.usecase.RegistrarAporteMetaUseCase;
import com.financeiramente.desktop.app.AppContext;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class MetasController {

    // Lista
    @FXML private ListView<Meta> lvMetas;
    @FXML private javafx.scene.control.Button btnEditar;
    @FXML private javafx.scene.control.Button btnDesativar;

    // Formulário meta
    @FXML private Label lblTituloForm;
    @FXML private TextField tfNome;
    @FXML private TextField tfValorObjetivo;
    @FXML private TextField tfDataAlvo;
    @FXML private TextField tfDescricao;
    @FXML private Label lblErroMeta;

    // Detalhe / aportes
    @FXML private Label lblDetalheNome;
    @FXML private ProgressBar pbProgresso;
    @FXML private Label lblPercentual;
    @FXML private Label lblValores;
    @FXML private Label lblProjecao;
    @FXML private ListView<AporteMeta> lvAportes;
    @FXML private TextField tfAporteValor;
    @FXML private TextField tfAporteData;
    @FXML private TextField tfAporteDescricao;
    @FXML private Label lblErroAporte;

    @FXML private Label lblStatus;

    private final CriarMetaUseCase criarMeta;
    private final EditarMetaUseCase editarMeta;
    private final DesativarMetaUseCase desativarMeta;
    private final RegistrarAporteMetaUseCase registrarAporte;
    private final DeletarAporteMetaUseCase deletarAporte;
    private final CalcularProjecaoMetaUseCase calcularProjecao;
    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteRepository;

    private String editandoMetaId;   // null = nova
    private Meta metaSelecionada;

    public MetasController() {
        AppContext ctx = AppContext.get();
        this.criarMeta        = ctx.getCriarMetaUseCase();
        this.editarMeta       = ctx.getEditarMetaUseCase();
        this.desativarMeta    = ctx.getDesativarMetaUseCase();
        this.registrarAporte  = ctx.getRegistrarAporteMetaUseCase();
        this.deletarAporte    = ctx.getDeletarAporteMetaUseCase();
        this.calcularProjecao = ctx.getCalcularProjecaoMetaUseCase();
        this.metaRepository   = ctx.getMetaRepository();
        this.aporteRepository = ctx.getAporteMetaRepository();
    }

    @FXML
    public void initialize() {
        configurarListMetas();
        configurarListAportes();
        carregarMetas();
        limparFormularioMeta();
        tfAporteData.setText(LocalDate.now().toString());

        lvMetas.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            boolean tem = sel != null;
            btnEditar.setDisable(!tem);
            btnDesativar.setDisable(!tem);
            metaSelecionada = sel;
            if (tem) carregarDetalhe(sel);
        });
    }

    private void configurarListMetas() {
        lvMetas.setCellFactory(lv -> new ListCell<Meta>() {
            @Override
            protected void updateItem(Meta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                double pct = item.getValorObjetivo() > 0
                        ? (item.getValorAtual() / item.getValorObjetivo()) * 100 : 0;
                setText(String.format("%s  —  %.1f%%  (R$ %.2f / R$ %.2f)",
                        item.getNome(), Math.min(pct, 100),
                        item.getValorAtual(), item.getValorObjetivo()));
            }
        });
    }

    private void configurarListAportes() {
        lvAportes.setCellFactory(lv -> new ListCell<AporteMeta>() {
            @Override
            protected void updateItem(AporteMeta item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                String desc = item.getDescricao() != null && !item.getDescricao().isEmpty()
                        ? " — " + item.getDescricao() : "";
                setText(String.format("%s  |  R$ %.2f%s", item.getData(), item.getValor(), desc));
            }
        });
    }

    private void carregarMetas() {
        Task<List<Meta>> task = new Task<>() {
            @Override protected List<Meta> call() { return metaRepository.listarAtivas(); }
        };
        task.setOnSucceeded(e -> lvMetas.getItems().setAll(task.getValue()));
        task.setOnFailed(e -> lblStatus.setText("Erro ao carregar metas."));
        Thread t = new Thread(task); t.setDaemon(true); t.start();
    }

    private void carregarDetalhe(Meta meta) {
        lblDetalheNome.setText(meta.getNome());
        double pct = meta.getValorObjetivo() > 0
                ? (meta.getValorAtual() / meta.getValorObjetivo()) * 100 : 0;
        pbProgresso.setProgress(Math.min(pct, 100) / 100.0);
        lblPercentual.setText(String.format(Locale.getDefault(), "%.1f%%", Math.min(pct, 100)));
        lblValores.setText(String.format(Locale.getDefault(),
                "R$ %.2f / R$ %.2f", meta.getValorAtual(), meta.getValorObjetivo()));

        Task<List<Object>> task = new Task<>() {
            @Override
            protected List<Object> call() {
                List<Object> result = new ArrayList<>();
                result.add(aporteRepository.listarPorMeta(meta.getId()));
                result.add(calcularProjecao.executar(meta.getId()));
                return result;
            }
        };
        task.setOnSucceeded(e -> {
            @SuppressWarnings("unchecked")
            List<AporteMeta> aportes = (List<AporteMeta>) task.getValue().get(0);
            @SuppressWarnings("unchecked")
            Optional<LocalDate> proj = (Optional<LocalDate>) task.getValue().get(1);
            lvAportes.setItems(FXCollections.observableArrayList(aportes));
            lblProjecao.setText(proj.map(d -> "Projeção: " + d.toString())
                    .orElse("Projeção: sem dados suficientes"));
        });
        Thread t = new Thread(task); t.setDaemon(true); t.start();
    }

    @FXML
    private void novaMeta() {
        editandoMetaId = null;
        limparFormularioMeta();
        lblTituloForm.setText("Nova Meta");
    }

    @FXML
    private void editarSelecionado() {
        Meta sel = lvMetas.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        editandoMetaId = sel.getId();
        lblTituloForm.setText("Editar Meta");
        tfNome.setText(sel.getNome());
        tfValorObjetivo.setText(String.format(Locale.US, "%.2f", sel.getValorObjetivo()));
        tfDataAlvo.setText(sel.getDataAlvo() != null ? sel.getDataAlvo() : "");
        tfDescricao.setText(sel.getDescricao() != null ? sel.getDescricao() : "");
        lblErroMeta.setVisible(false);
    }

    @FXML
    private void desativarSelecionado() {
        Meta sel = lvMetas.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Desativar \"" + sel.getNome() + "\"?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == javafx.scene.control.ButtonType.OK) {
                Task<Void> task = new Task<>() {
                    @Override protected Void call() { desativarMeta.executar(sel.getId()); return null; }
                };
                task.setOnSucceeded(e -> { carregarMetas(); limparFormularioMeta(); lblStatus.setText("Meta desativada."); });
                task.setOnFailed(e -> lblStatus.setText("Erro ao desativar."));
                Thread t = new Thread(task); t.setDaemon(true); t.start();
            }
        });
    }

    @FXML
    private void salvarMeta() {
        lblErroMeta.setVisible(false);
        String nome = tfNome.getText().trim();
        String valorStr = tfValorObjetivo.getText().trim();
        if (nome.isEmpty()) { lblErroMeta.setText("Nome é obrigatório."); lblErroMeta.setVisible(true); return; }
        double valorObjetivo;
        try {
            valorObjetivo = Double.parseDouble(valorStr.replace(',', '.'));
        } catch (NumberFormatException e) {
            lblErroMeta.setText("Valor inválido."); lblErroMeta.setVisible(true); return;
        }
        String dataAlvo = tfDataAlvo.getText().trim().isEmpty() ? null : tfDataAlvo.getText().trim();
        String descricao = tfDescricao.getText().trim().isEmpty() ? null : tfDescricao.getText().trim();
        final String idEdicao = editandoMetaId;
        Task<Meta> task = new Task<>() {
            @Override
            protected Meta call() {
                if (idEdicao == null) return criarMeta.executar(nome, valorObjetivo, dataAlvo, descricao);
                else return editarMeta.executar(idEdicao, nome, valorObjetivo, dataAlvo, descricao);
            }
        };
        task.setOnSucceeded(e -> { carregarMetas(); limparFormularioMeta(); lblStatus.setText("Meta salva."); });
        task.setOnFailed(e -> {
            String msg = task.getException() != null ? task.getException().getMessage() : "Erro";
            lblErroMeta.setText(msg); lblErroMeta.setVisible(true);
        });
        Thread t = new Thread(task); t.setDaemon(true); t.start();
    }

    @FXML
    private void cancelarMeta() { limparFormularioMeta(); }

    @FXML
    private void salvarAporte() {
        if (metaSelecionada == null) { lblStatus.setText("Selecione uma meta primeiro."); return; }
        lblErroAporte.setVisible(false);
        String valorStr = tfAporteValor.getText().trim();
        String data = tfAporteData.getText().trim();
        if (valorStr.isEmpty() || data.isEmpty()) {
            lblErroAporte.setText("Valor e data são obrigatórios."); lblErroAporte.setVisible(true); return;
        }
        double valor;
        try { valor = Double.parseDouble(valorStr.replace(',', '.')); }
        catch (NumberFormatException e) { lblErroAporte.setText("Valor inválido."); lblErroAporte.setVisible(true); return; }
        String descricao = tfAporteDescricao.getText().trim().isEmpty() ? null : tfAporteDescricao.getText().trim();
        final String metaId = metaSelecionada.getId();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                registrarAporte.executar(metaId, valor, data, descricao);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            tfAporteValor.clear(); tfAporteDescricao.clear();
            tfAporteData.setText(LocalDate.now().toString());
            // Recarrega detalhe
            metaRepository.buscarPorId(metaId).ifPresent(m -> {
                lvMetas.getItems().replaceAll(mm -> mm.getId().equals(m.getId()) ? m : mm);
                metaSelecionada = m;
                carregarDetalhe(m);
            });
            carregarMetas();
            lblStatus.setText("Aporte registrado.");
        });
        task.setOnFailed(e -> {
            String msg = task.getException() != null ? task.getException().getMessage() : "Erro";
            lblErroAporte.setText(msg); lblErroAporte.setVisible(true);
        });
        Thread t = new Thread(task); t.setDaemon(true); t.start();
    }

    @FXML
    private void deletarAporteSelecionado() {
        if (metaSelecionada == null) return;
        AporteMeta sel = lvAportes.getSelectionModel().getSelectedItem();
        if (sel == null) { lblStatus.setText("Selecione um aporte para excluir."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Excluir este aporte?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == javafx.scene.control.ButtonType.OK) {
                final String metaId = metaSelecionada.getId();
                Task<Void> task = new Task<>() {
                    @Override protected Void call() { deletarAporte.executar(sel.getId(), metaId); return null; }
                };
                task.setOnSucceeded(e -> {
                    metaRepository.buscarPorId(metaId).ifPresent(m -> {
                        metaSelecionada = m;
                        carregarDetalhe(m);
                    });
                    carregarMetas();
                    lblStatus.setText("Aporte excluído.");
                });
                task.setOnFailed(e -> lblStatus.setText("Erro ao excluir aporte."));
                Thread t = new Thread(task); t.setDaemon(true); t.start();
            }
        });
    }

    private void limparFormularioMeta() {
        editandoMetaId = null;
        lblTituloForm.setText("Nova Meta");
        tfNome.clear(); tfValorObjetivo.clear(); tfDataAlvo.clear(); tfDescricao.clear();
        lblErroMeta.setVisible(false);
        btnEditar.setDisable(true); btnDesativar.setDisable(true);
        lvMetas.getSelectionModel().clearSelection();
        lblDetalheNome.setText("Selecione uma meta");
        pbProgresso.setProgress(0);
        lblPercentual.setText("0%");
        lblValores.setText("R$ 0,00 / R$ 0,00");
        lblProjecao.setText("");
        lvAportes.getItems().clear();
        metaSelecionada = null;
    }
}
