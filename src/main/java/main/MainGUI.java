package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainGUI extends Application {

    // JavaFX a besoin d'un constructeur vide par défaut.
    // Si tu en as écrit un avec des paramètres, supprime-le.
    public MainGUI() {
        // Laisser vide
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Chargement du fichier FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/StocksInterface/afficherarticle.fxml"));
            Parent root = loader.load();

            // Création de la scène
            Scene scene = new Scene(root, 1050, 650);

            primaryStage.setScene(scene);
            primaryStage.setTitle("AgroFlow - Gestion des Stocks");

            // Empêcher la fenêtre d'être trop petite
            primaryStage.setMinWidth(1050);
            primaryStage.setMinHeight(650);

            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement du fichier FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }
}