package main; // Vérifie que ce package correspond à ton dossier src/main/java

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainGUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Charge la page d'affichage principale au lieu de l'ajout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherAnimaux.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600); // Taille stable
        primaryStage.setScene(scene);
        primaryStage.setTitle("AgroFlow - Gestion des Animaux");
        primaryStage.show();
    }

}