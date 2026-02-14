package controllers;

import entities.terrain;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.TerrainService;

import java.io.IOException;

public class ModifierTerrainController {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtSurface;
    @FXML
    private TextField txtTypeSol;
    @FXML
    private TextField txtLocalisation;
    @FXML
    private TextField txtPH;

    private final TerrainService ts = new TerrainService();
    private terrain terrainSelectionne;

    // Méthode pour initialiser les champs avec le terrain sélectionné
    public void initialiserAvecTerrain(terrain t) {
        this.terrainSelectionne = t;
        txtNom.setText(t.getNom_terrain());
        txtSurface.setText(String.valueOf(t.getSurface()));
        txtTypeSol.setText(t.getType_sol());
        txtLocalisation.setText(t.getLocalisation());
        txtPH.setText(String.valueOf(t.getP_h()));
    }

    @FXML
    public void handleModifier(ActionEvent event) {
        // 1. Récupération des données
        String nom = txtNom.getText().trim();
        String surfaceStr = txtSurface.getText().trim();
        String typeSol = txtTypeSol.getText().trim();
        String localisation = txtLocalisation.getText().trim();
        String phStr = txtPH.getText().trim();

        // 2. Validation des champs vides
        if (nom.isEmpty() || surfaceStr.isEmpty() || typeSol.isEmpty() || localisation.isEmpty() || phStr.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }

        // 3. Validation : Nom ne doit pas contenir de chiffres
        if (nom.matches(".*\\d.*")) {
            showAlert("Erreur", "Le nom du terrain ne doit pas contenir de chiffres.", Alert.AlertType.ERROR);
            return;
        }

        // 4. Validation : Type de sol ne doit pas contenir de chiffres
        if (typeSol.matches(".*\\d.*")) {
            showAlert("Erreur", "Le type de sol ne doit pas contenir de chiffres.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // 5. Conversion des types numériques
            float surface = Float.parseFloat(surfaceStr);
            float ph = Float.parseFloat(phStr);

            // 6. Validation : Surface doit être positive
            if (surface <= 0) {
                showAlert("Erreur", "La surface doit être supérieure à 0.", Alert.AlertType.ERROR);
                return;
            }

            // 7. Validation : pH doit être entre 0 et 14
            if (ph < 0 || ph > 14) {
                showAlert("Erreur", "Le pH doit être compris entre 0 et 14.", Alert.AlertType.ERROR);
                return;
            }

            // 8. Mise à jour de l'objet existant
            terrainSelectionne.setNom_terrain(nom);
            terrainSelectionne.setSurface(surface);
            terrainSelectionne.setType_sol(typeSol);
            terrainSelectionne.setLocalisation(localisation);
            terrainSelectionne.setP_h(ph);

            // 9. Modification dans la base
            ts.modifier(terrainSelectionne);

            // 10. Succès et retour à la liste
            showAlert("Succès", "Le terrain '" + nom + "' a été modifié avec succès !", Alert.AlertType.INFORMATION);
            retourListe(event);

        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "La surface et le pH doivent être des nombres (ex: 100.5 et 6.5).", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void retourListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageTerrain.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean etaitMaximise = stage.isMaximized();  // ← LIGNE 1 : Sauvegarder

            stage.setScene(new Scene(root));

            stage.setMaximized(etaitMaximise);
            stage.setTitle("Gestion des Terrains");
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