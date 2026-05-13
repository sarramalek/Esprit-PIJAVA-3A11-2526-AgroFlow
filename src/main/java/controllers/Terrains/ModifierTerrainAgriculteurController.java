package controllers.Terrains;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Terrains.terrain;
import services.Terrains.TerrainService;

import java.io.IOException;

public class ModifierTerrainAgriculteurController {
    @FXML private TextField txtNom;
    @FXML private TextField txtSurface;
    @FXML private ComboBox<String> comboTypeSol;
    @FXML private TextField txtLocalisation;
    @FXML private TextField txtPH;

    private final TerrainService ts = new TerrainService();
    private terrain terrainSelectionne;

    @FXML
    public void initialize() {
        comboTypeSol.setItems(FXCollections.observableArrayList(
                "Argileux", "Sableux", "Limoneux", "Calcaire", "Humifère"
        ));
    }

    public void initialiserAvecTerrain(terrain t) {
        this.terrainSelectionne = t;
        txtNom.setText(t.getNom_terrain());
        txtSurface.setText(String.valueOf(t.getSurface()));
        comboTypeSol.setValue(t.getType_sol());
        txtLocalisation.setText(t.getLocalisation());
        txtPH.setText(String.valueOf(t.getP_h()));
    }

    @FXML
    public void handleModifier(ActionEvent event) {
        String nom = txtNom.getText().trim();
        String surfaceStr = txtSurface.getText().trim();
        String typeSol = comboTypeSol.getValue() != null ? comboTypeSol.getValue().trim() : "";
        String localisation = txtLocalisation.getText().trim();
        String phStr = txtPH.getText().trim();

        if (terrainSelectionne == null || nom.isEmpty() || surfaceStr.isEmpty() || typeSol.isEmpty() || localisation.isEmpty() || phStr.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }

        try {
            float surface = Float.parseFloat(surfaceStr);
            float ph = Float.parseFloat(phStr);
            if (surface <= 0 || ph < 0 || ph > 14) {
                showAlert("Erreur", "Surface > 0 et pH entre 0 et 14.", Alert.AlertType.ERROR);
                return;
            }

            terrainSelectionne.setNom_terrain(nom);
            terrainSelectionne.setSurface(surface);
            terrainSelectionne.setType_sol(typeSol);
            terrainSelectionne.setLocalisation(localisation);
            terrainSelectionne.setP_h(ph);
            ts.modifier(terrainSelectionne);
            showAlert("Succès", "Terrain modifié avec succès !", Alert.AlertType.INFORMATION);
            retourListe(event);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Surface et pH doivent être numériques.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void retourListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/agricoleaffichageterrain.fxml"));
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
