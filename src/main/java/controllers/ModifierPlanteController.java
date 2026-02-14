package controllers;

import entities.plante;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.PlanteService;

import java.io.IOException;

public class ModifierPlanteController {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtVariete;
    @FXML
    private TextField txtBesoinEau;
    @FXML
    private TextField txtCycle;

    private final PlanteService ps = new PlanteService();
    private plante planteSelectionnee;

    // Méthode pour initialiser les champs avec la plante sélectionnée
    public void initialiserAvecPlante(plante p) {
        this.planteSelectionnee = p;
        txtNom.setText(p.getNom_p());
        txtVariete.setText(p.getVariete());
        txtBesoinEau.setText(String.valueOf(p.getBesoin_eau()));
        txtCycle.setText(String.valueOf(p.getCycle_jours()));
    }

    @FXML
    public void handleModifier(ActionEvent event) {
        // 1. Récupération des données
        String nom = txtNom.getText().trim();
        String variete = txtVariete.getText().trim();
        String besoinEauStr = txtBesoinEau.getText().trim();
        String cycleStr = txtCycle.getText().trim();

        // 2. Validation des champs vides
        if (nom.isEmpty() || variete.isEmpty() || besoinEauStr.isEmpty() || cycleStr.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }

        // 3. Validation : Nom ne doit pas contenir de chiffres
        if (nom.matches(".*\\d.*")) {
            showAlert("Erreur", "Le nom ne doit pas contenir de chiffres.", Alert.AlertType.ERROR);
            return;
        }

        // 4. Validation : Variété ne doit pas contenir de chiffres
        if (variete.matches(".*\\d.*")) {
            showAlert("Erreur", "La variété ne doit pas contenir de chiffres.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // 5. Conversion des types numériques
            float besoinEau = Float.parseFloat(besoinEauStr);
            int cycle = Integer.parseInt(cycleStr);

            // 6. Mise à jour de l'objet existant
            planteSelectionnee.setNom_p(nom);
            planteSelectionnee.setVariete(variete);
            planteSelectionnee.setBesoin_eau(besoinEau);
            planteSelectionnee.setCycle_jours(cycle);

            // 7. Modification dans la base
            ps.modifier(planteSelectionnee);

            // 8. Succès et retour à la liste
            showAlert("Succès", "La plante '" + nom + "' a été modifiée avec succès !", Alert.AlertType.INFORMATION);
            retourListe(event);

        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "Le besoin en eau doit être un nombre (ex: 0.5) et le cycle un entier.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void retourListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichagePlante.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Plantes");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à la liste : " + e.getMessage(), Alert.AlertType.ERROR);
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