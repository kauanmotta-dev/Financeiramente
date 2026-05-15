package com.financeiramente.desktop.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller da tela principal (container) da aplicação Desktop.
 */
public class MainController {

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        if (statusLabel != null) {
            statusLabel.setText("Financeiramente — pronto.");
        }
    }
}
