package controllers.Terrains;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Terrains.plante;
import services.Terrains.PlanteService;

import java.io.IOException;

public class AjoutPlanteAgriculteurController {
    @FXML private TextField txtNom;
    @FXML private TextField txtVariete;
    @FXML private TextField txtBesoinEau;
    @FXML private TextField txtCycle;

    private final PlanteService ps = new PlanteService();

    @FXML
    void ajouterPlante(ActionEvent event) {
        String nom = txtNom.getText().trim();
        String variete = txtVariete.getText().trim();
        String besoinEauStr = txtBesoinEau.getText().trim();
        String cycleStr = txtCycle.getText().trim();

        if (nom.isEmpty() || variete.isEmpty() || besoinEauStr.isEmpty() || cycleStr.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }

        try {
            float besoinEau = Float.parseFloat(besoinEauStr);
            int cycle = Integer.parseInt(cycleStr);
            if (besoinEau <= 0 || cycle <= 0) {
                showAlert("Erreur", "Besoin en eau et cycle doivent être > 0.", Alert.AlertType.ERROR);
                return;
            }
            ps.ajouter(new plante(0, nom, variete, besoinEau, cycle));
            showAlert("Succès", "Plante ajoutée avec succès !", Alert.AlertType.INFORMATION);
            retourListe(event);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Besoin en eau et cycle doivent être numériques.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void retourListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/agricoleaffichageplante.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();
            scene.setRoot(root);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de retourner à la liste.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
