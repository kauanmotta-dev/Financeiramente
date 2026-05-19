package com.financeiramente.desktop.ui;

import com.financeiramente.core.domain.entity.Categoria;
import com.financeiramente.core.domain.entity.PlanejamentoCategoria;
import com.financeiramente.core.domain.entity.PlanejamentoMensal;
import com.financeiramente.core.repository.CategoriaRepository;
import com.financeiramente.core.repository.PlanejamentoRepository;
import com.financeiramente.core.usecase.ConfirmarPlanejamentoUseCase;
import com.financeiramente.core.usecase.CriarPlanejamentoMensalUseCase;
import com.financeiramente.core.usecase.DefinirComoPlanosPadraoUseCase;
import com.financeiramente.core.usecase.DefinirLimiteCategoriaUseCase;
import com.financeiramente.desktop.app.AppContext;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlanejamentoController {

    @FXML private Label lblTitulo;
    @FXML private Label lblBadgeConfirmado;
    @FXML private TextField tfReceitaEsperada;
    @FXML private TextField tfReservaImprevisto;
    @FXML private ProgressBar pbGlobal;
    @FXML private Label lblProgressoTexto;
    @FXML private ListView<Categoria> lvCategorias;
    @FXML private Label lblStatus;

    private final CriarPlanejamentoMensalUseCase criarPlanejamento;
    private final ConfirmarPlanejamentoUseCase confirmarPlanejamento;
    private final DefinirLimiteCategoriaUseCase definirLimite;
    private final DefinirComoPlanosPadraoUseCase definirPadrao;
    private final PlanejamentoRepository planejamentoRepository;
    private final CategoriaRepository categoriaRepository;

    private PlanejamentoMensal planoAtual;
    private List<PlanejamentoCategoria> itensCategoria;
    /** Mapa de limites editados pelo usuário na sessão atual. */
    private final Map<String, Double> limitesEditados = new HashMap<>();

    private final int ano;
    private final int mes;

    public PlanejamentoController() {
        AppContext ctx = AppContext.get();
        this.criarPlanejamento = ctx.getCriarPlanejamentoMensalUseCase();
        this.confirmarPlanejamento = ctx.getConfirmarPlanejamentoUseCase();
        this.definirLimite = ctx.getDefinirLimiteCategoriaUseCase();
        this.definirPadrao = ctx.getDefinirComoPlanosPadraoUseCase();
        this.planejamentoRepository = ctx.getPlanejamentoRepository();
        this.categoriaRepository = ctx.getCategoriaRepository();

        LocalDate hoje = LocalDate.now();
        this.ano = hoje.getYear();
        this.mes = hoje.getMonthValue();
    }

    @FXML
    public void initialize() {
        configurarListView();
        carregarPlano();
    }

    private void configurarListView() {
        lvCategorias.setCellFactory(lv -> new ListCell<>() {
            private final TextField tfLimite = new TextField();
            private final Label lblNome = new Label();
            private final HBox hbox = new HBox(8.0, lblNome, tfLimite);

            {
                HBox.setHgrow(lblNome, Priority.ALWAYS);
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.setPadding(new Insets(4, 8, 4, 8));
                tfLimite.setPromptText("Limite (R$)");
                tfLimite.setPrefWidth(120.0);

                tfLimite.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        Categoria cat = getItem();
                        if (cat == null || planoAtual == null) return;
                        try {
                            String txt = tfLimite.getText().replace(",", ".");
                            double limite = txt.isEmpty() ? 0.0 : Double.parseDouble(txt);
                            limitesEditados.put(cat.getId(), limite);
                            salvarLimiteCategoria(cat.getId(), limite);
                        } catch (NumberFormatException ignored) {
                            // Valor inválido — ignora
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Categoria cat, boolean empty) {
                super.updateItem(cat, empty);
                if (empty || cat == null) {
                    setGraphic(null);
                    return;
                }
                lblNome.setText(cat.getNome());

                // Preenche limite atual
                double limiteAtual = 0.0;
                if (limitesEditados.containsKey(cat.getId())) {
                    limiteAtual = limitesEditados.get(cat.getId());
                } else if (itensCategoria != null) {
                    for (PlanejamentoCategoria item : itensCategoria) {
                        if (item.getCategoriaId().equals(cat.getId())) {
                            limiteAtual = item.getLimite();
                            break;
                        }
                    }
                }
                tfLimite.setText(limiteAtual > 0
                        ? String.format(Locale.getDefault(), "%.2f", limiteAtual)
                        : "");

                setGraphic(hbox);
            }
        });
    }

    private void carregarPlano() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                planoAtual = criarPlanejamento.executar(ano, mes);
                itensCategoria = planejamentoRepository.listarItensPorPlano(planoAtual.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            atualizarCabecalho();
            carregarCategorias();
        });
        task.setOnFailed(e -> mostrarErro("Erro ao carregar planejamento: "
                + task.getException().getMessage()));

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void atualizarCabecalho() {
        if (planoAtual == null) return;

        String[] meses = {"Janeiro","Fevereiro","Março","Abril","Maio","Junho",
                "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"};
        String nomeMes = meses[planoAtual.getMes() - 1];
        lblTitulo.setText(String.format("Planejamento — %s %d", nomeMes, planoAtual.getAno()));

        lblBadgeConfirmado.setVisible(planoAtual.isConfirmado());
        lblBadgeConfirmado.setManaged(planoAtual.isConfirmado());

        if (planoAtual.getReceitaEsperada() > 0) {
            tfReceitaEsperada.setText(
                    String.format(Locale.getDefault(), "%.2f", planoAtual.getReceitaEsperada()));
        }
        if (planoAtual.getReservaImprevisto() > 0) {
            tfReservaImprevisto.setText(
                    String.format(Locale.getDefault(), "%.2f", planoAtual.getReservaImprevisto()));
        }
    }

    private void carregarCategorias() {
        ObservableList<Categoria> cats = FXCollections.observableArrayList(
                categoriaRepository.listarTodas());
        lvCategorias.setItems(cats);
        atualizarProgresso();
    }

    private void atualizarProgresso() {
        if (itensCategoria == null || planoAtual == null) return;
        double totalLimites = itensCategoria.stream().mapToDouble(PlanejamentoCategoria::getLimite).sum();
        double receita = planoAtual.getReceitaEsperada();
        if (receita > 0) {
            pbGlobal.setProgress(totalLimites / receita);
            lblProgressoTexto.setText(String.format(Locale.getDefault(),
                    "R$ %.0f / R$ %.0f planejados", totalLimites, receita));
        } else {
            pbGlobal.setProgress(0);
            lblProgressoTexto.setText("Sem receita esperada definida");
        }
    }

    private void salvarLimiteCategoria(String categoriaId, double limite) {
        if (planoAtual == null) return;
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                definirLimite.executar(planoAtual, categoriaId, limite);
                itensCategoria = planejamentoRepository.listarItensPorPlano(planoAtual.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            atualizarProgresso();
            lblStatus.setText("Limite salvo.");
        });
        task.setOnFailed(e -> mostrarErro("Erro ao salvar limite: "
                + task.getException().getMessage()));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void onSalvarCabecalho() {
        if (planoAtual == null) return;
        try {
            double receita = parseDouble(tfReceitaEsperada.getText());
            double reserva = parseDouble(tfReservaImprevisto.getText());
            planoAtual.setReceitaEsperada(receita);
            planoAtual.setReservaImprevisto(reserva);

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    planejamentoRepository.atualizar(planoAtual);
                    return null;
                }
            };
            task.setOnSucceeded(e -> {
                atualizarProgresso();
                lblStatus.setText("Receita e reserva salvos.");
            });
            task.setOnFailed(e -> mostrarErro("Erro: " + task.getException().getMessage()));
            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
        } catch (NumberFormatException e) {
            mostrarErro("Informe valores numéricos válidos.");
        }
    }

    @FXML
    private void onConfirmarPlano() {
        if (planoAtual == null) return;
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                confirmarPlanejamento.executar(planoAtual);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            atualizarCabecalho();
            lblStatus.setText("Plano confirmado!");
        });
        task.setOnFailed(e -> mostrarErro("Erro: " + task.getException().getMessage()));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void onDefinirPadrao() {
        if (planoAtual == null) return;
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                definirPadrao.executar(planoAtual);
                return null;
            }
        };
        task.setOnSucceeded(e -> lblStatus.setText("Definido como plano padrão."));
        task.setOnFailed(e -> mostrarErro("Erro: " + task.getException().getMessage()));
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) return 0.0;
        return Double.parseDouble(text.trim().replace(",", "."));
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
