package controllers.Terrains;

import models.Terrains.rotation;
import models.Terrains.terrain;
import models.Terrains.plante;
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
import services.Terrains.RotationService;
import services.Terrains.TerrainService;
import services.Terrains.PlanteService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.ResourceBundle;

public class ModifierRotationController implements Initializable {

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
    private rotation rotationSelectionnee;

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
    }

    public void initialiserAvecRotation(rotation r) {
        this.rotationSelectionnee = r;

        // Sélectionner le terrain
        for (terrain t : comboTerrain.getItems()) {
            if (t.getId_terrain() == r.getId_terrain()) {
                comboTerrain.setValue(t);
                break;
            }
        }

        // Sélectionner la plante
        for (plante p : comboPlante.getItems()) {
            if (p.getId_plante() == r.getId_plante()) {
                comboPlante.setValue(p);
                break;
            }
        }

        // ✅ CORRECTION : Conversion java.util.Date → java.sql.Date → LocalDate
        try {
            if (r.getDate_debut_t() != null) {
                // Conversion de java.util.Date vers java.sql.Date
                java.util.Date utilDateDebut = r.getDate_debut_t();
                java.sql.Date sqlDateDebut = new java.sql.Date(utilDateDebut.getTime());
                java.time.LocalDate localDateDebut = sqlDateDebut.toLocalDate();
                dateDebut.setValue(localDateDebut);
            }

            if (r.getDate_fin_t() != null) {
                // Conversion de java.util.Date vers java.sql.Date
                java.util.Date utilDateFin = r.getDate_fin_t();
                java.sql.Date sqlDateFin = new java.sql.Date(utilDateFin.getTime());
                java.time.LocalDate localDateFin = sqlDateFin.toLocalDate();
                dateFin.setValue(localDateFin);
            }
        } catch (Exception e) {
            System.out.println("Erreur conversion date : " + e.getMessage());
            e.printStackTrace();
        }

        // Définir le statut
        comboStatus.setValue(r.getStatus() == 1 ? "En cours" : "Terminée");
    }

    @FXML
    public void handleModifier(ActionEvent event) {
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
            // 3. Mise à jour
            rotationSelectionnee.setId_terrain(comboTerrain.getValue().getId_terrain());
            rotationSelectionnee.setId_plante(comboPlante.getValue().getId_plante());
            rotationSelectionnee.setDate_debut_t(Date.valueOf(dateDebut.getValue()));
            rotationSelectionnee.setDate_fin_t(Date.valueOf(dateFin.getValue()));
            rotationSelectionnee.setStatus(comboStatus.getValue().equals("En cours") ? 1 : 0);

            // 4. Modification dans la base
            rs.modifier(rotationSelectionnee);

            // 5. Succès
            showAlert("Succès", "Rotation modifiée avec succès !", Alert.AlertType.INFORMATION);
            retourListe(event);

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void retourListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/AffichageRotation.fxml"));
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

    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}