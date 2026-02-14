package controllers;

import entities.plante;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.PlanteService;

import java.io.IOException;

public class AjoutPlanteController {

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtVariete;

    @FXML
    private TextField txtBesoinEau;

    @FXML
    private TextField txtCycle;

    private final PlanteService ps = new PlanteService();

    @FXML
    void ajouterPlante(ActionEvent event) {
        // 1. Récupération des données
        String nom = txtNom.getText();
        String variete = txtVariete.getText();
        String besoinEauStr = txtBesoinEau.getText();
        String cycleStr = txtCycle.getText();

        // 2. Validation simple
        if (nom.isEmpty() || variete.isEmpty() || besoinEauStr.isEmpty() || cycleStr.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // 3. Conversion des types numériques
            float besoinEau = Float.parseFloat(besoinEauStr);
            int cycle = Integer.parseInt(cycleStr);

            // 4. Création et ajout de l'objet
            plante p = new plante(0, nom, variete, besoinEau, cycle);
            ps.ajouter(p);

            // 5. Succès et réinitialisation
            showAlert("Succès", "La plante '" + nom + "' a été ajoutée avec succès !", Alert.AlertType.INFORMATION);
            nettoyerChamps();

        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "Le besoin en eau doit être un nombre (ex: 0.5) et le cycle un entier.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void retourListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichagePlante.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));boolean etaitMaximise = stage.isMaximized();  // ← LIGNE 1 : Sauvegarder

            stage.setScene(new Scene(root));

            stage.setMaximized(etaitMaximise);
            stage.setTitle("Gestion des Plantes");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à la liste : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void nettoyerChamps() {
        txtNom.clear();
        txtVariete.clear();
        txtBesoinEau.clear();
        txtCycle.clear();
    }

    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}