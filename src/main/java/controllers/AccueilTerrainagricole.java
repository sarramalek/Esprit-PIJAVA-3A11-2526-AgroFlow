package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class AccueilTerrainagricole {

        @FXML
        void ouvrirTerrains(MouseEvent event) {
            chargerPage(event, "/agricoleaffichageterrain.fxml", "Gestion des Terrains");
        }

        @FXML
        void ouvrirPlantes(MouseEvent event) {
            chargerPage(event, "/agricoleaffichageplante.fxml", "Liste des Plantes");
        }

        @FXML
        void ouvrirRotations(MouseEvent event) {
            chargerPage(event, "/agricoleaffichagerotation.fxml", "Gestion des Rotations");
        }

        private void chargerPage(MouseEvent event, String fxmlPath, String titre) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

                stage.setScene(new Scene(root));
                stage.setTitle(titre);
                stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

                stage.show();
            } catch (IOException e) {
                System.err.println("Erreur de chargement FXML : " + fxmlPath);
                e.printStackTrace();
            }
        }
    }

