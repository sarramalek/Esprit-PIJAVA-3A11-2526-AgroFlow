package controllers;

import entities.rotation;
import entities.terrain;
import entities.plante;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.RotationService;
import services.TerrainService;
import services.PlanteService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class AjoutRotationController implements Initializable {

    @FXML
    private ComboBox<terrain> comboTerrain;
    @FXML
    private ComboBox<plante> comboPlante;
    @FXML
    private DatePicker dateDebut;
    @FXML
    private DatePicker dateFin;
    @FXML
    private ComboBox<String> comboStatus;

    private final RotationService rs = new RotationService();
    private final TerrainService ts = new TerrainService();
    private final PlanteService ps = new PlanteService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Charger les terrains
        List<terrain> terrains = ts.afficherTous();
        comboTerrain.setItems(FXCollections.observableArrayList(terrains));
        comboTerrain.setCellFactory(param -> new ListCell<terrain>() {
            @Override
            protected void updateItem(terrain item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_terrain());
            }
        });
        comboTerrain.setButtonCell(new ListCell<terrain>() {
            @Override
            protected void updateItem(terrain item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_terrain());
            }
        });

        // Charger les plantes
        List<plante> plantes = ps.afficherToutes();
        comboPlante.setItems(FXCollections.observableArrayList(plantes));
        comboPlante.setCellFactory(param -> new ListCell<plante>() {
            @Override
            protected void updateItem(plante item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_p() + " (" + item.getVariete() + ")");
            }
        });
        comboPlante.setButtonCell(new ListCell<plante>() {
            @Override
            protected void updateItem(plante item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_p() + " (" + item.getVariete() + ")");
            }
        });

        // Charger les statuts
        comboStatus.setItems(FXCollections.observableArrayList("En cours", "Terminée"));
        comboStatus.setValue("En cours");
    }

    @FXML
    void ajouterRotation(ActionEvent event) {
        // 1. Validation des champs
        if (comboTerrain.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un terrain.", Alert.AlertType.ERROR);
            return;
        }

        if (comboPlante.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une plante.", Alert.AlertType.ERROR);
            return;
        }

        if (dateDebut.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une date de début.", Alert.AlertType.ERROR);
            return;
        }

        if (dateFin.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une date de fin.", Alert.AlertType.ERROR);
            return;
        }

        // 2. Validation : Date fin doit être après date début
        if (dateFin.getValue().isBefore(dateDebut.getValue())) {
            showAlert("Erreur", "La date de fin doit être après la date de début.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // 3. Récupération des données
            int idTerrain = comboTerrain.getValue().getId_terrain();
            int idPlante = comboPlante.getValue().getId_plante();
            Date sqlDateDebut = Date.valueOf(dateDebut.getValue());
            Date sqlDateFin = Date.valueOf(dateFin.getValue());
            int status = comboStatus.getValue().equals("En cours") ? 1 : 0;

            // 4. Création et ajout
            rotation r = new rotation(0, idTerrain, idPlante, sqlDateDebut, sqlDateFin, status);
            rs.ajouter(r);

            // 5. Succès
            showAlert("Succès", "Rotation ajoutée avec succès !", Alert.AlertType.INFORMATION);
            nettoyerChamps();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void retourListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageRotation.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Rotations");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à la liste : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void nettoyerChamps() {
        comboTerrain.setValue(null);
        comboPlante.setValue(null);
        dateDebut.setValue(null);
        dateFin.setValue(null);
        comboStatus.setValue("En cours");
    }

    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}