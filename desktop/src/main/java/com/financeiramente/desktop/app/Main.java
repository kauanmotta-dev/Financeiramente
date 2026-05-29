package com.financeiramente.desktop.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Ponto de entrada da aplicação Desktop JavaFX.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/financeiramente/desktop/fxml/main.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(
            getClass().getResource("/com/financeiramente/desktop/css/app.css")
                      .toExternalForm()
        );
        primaryStage.setTitle("Financeiramente");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(
            getClass().getResourceAsStream("/com/financeiramente/desktop/img/logo.png")
        )));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
