package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class AccueilMaterielController {

    @FXML
    private void ouvrirMachines(MouseEvent event) {
        naviguerVers("/AffichageMachine.fxml", event);
    }

    @FXML
    private void ouvrirMaintenances(MouseEvent event) {
        naviguerVers("/AfficherMaintenances.fxml", event);
    }

    @FXML
    private void ouvrirAchats(MouseEvent event) {
        naviguerVers("/AfficherAchats.fxml", event);
    }

    // Navigation vers les autres modules depuis la sidebar
    @FXML
    private void naviguerAnimaux(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AfficherAnimaux.fxml", event);
    }

    @FXML
    private void naviguerMateriels(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AccueilMateriel.fxml", event);
    }

    @FXML
    private void naviguerStocks(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AfficherStocks.fxml", event);
    }

    @FXML
    private void naviguerTerrains(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AfficherTerrains.fxml", event);
    }

    @FXML
    private void naviguerEvenements(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AccueilEvenement.fxml", event);
    }

    @FXML
    private void naviguerUsers(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AfficherUsers.fxml", event);
    }

    @FXML
    private void deconnexion(javafx.event.ActionEvent event) {
        System.exit(0);
    }

    // Méthode pour naviguer depuis les cartes (MouseEvent)
    private void naviguerVers(String fxmlPath, MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible de naviguer vers la page demandée: " + fxmlPath, Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de la navigation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Méthode pour naviguer depuis les boutons de la sidebar (ActionEvent)
    private void naviguerDepuisBouton(String fxmlPath, javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible de naviguer vers la page demandée: " + fxmlPath, Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de la navigation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}