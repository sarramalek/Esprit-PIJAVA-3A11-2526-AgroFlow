package controllers.Materiels;

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
        naviguerVers("/MaterielsInterface/AffichageMachine.fxml", event);
    }

    @FXML
    private void ouvrirMaintenances(MouseEvent event) {
        naviguerVers("/MaterielsInterface/AfficherMaintenances.fxml", event);
    }

    @FXML
    private void ouvrirAchats(MouseEvent event) {
        naviguerVers("/MaterielsInterface/AfficherAchats.fxml", event);
    }

    // Navigation vers les autres modules depuis la sidebar
    @FXML
    private void naviguerAnimaux(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AnimalsInterface/AfficherAnimaux.fxml", event);
    }

    @FXML
    private void naviguerMateriels(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/MaterielsInterface/AccueilMateriel.fxml", event);
    }

    @FXML
    private void naviguerStocks(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/StocksInterface/afficherarticle.fxml", event);
    }

    @FXML
    private void naviguerTerrains(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/TerrainsInterface/AfficherTerrains.fxml", event);
    }

    @FXML
    private void naviguerEvenements(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/EventsInterface/AccueilEvenement.fxml", event);
    }

    @FXML
    private void naviguerUsers(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/UsersInterface/Acceuil.fxml", event);
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