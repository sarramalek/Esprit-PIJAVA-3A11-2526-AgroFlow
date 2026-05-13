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
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;
import models.Terrains.plante;
import models.Terrains.rotation;
import models.Terrains.terrain;
import models.User.Personne;
import services.Terrains.PlanteService;
import services.Terrains.RotationService;
import services.Terrains.TerrainService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

public class ModifierRotationAgriculteurController {
    @FXML private ComboBox<terrain> comboTerrain;
    @FXML private ComboBox<plante> comboPlante;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<String> comboStatus;

    private final RotationService rs = new RotationService();
    private final TerrainService ts = new TerrainService();
    private final PlanteService ps = new PlanteService();
    private Personne currentUser;
    private rotation rotationSelectionnee;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();
        List<terrain> terrains = currentUser != null ? ts.afficherParCin(currentUser.getCin()) : ts.afficherTous();
        comboTerrain.setItems(FXCollections.observableArrayList(terrains));
        comboTerrain.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(terrain item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_terrain());
            }
        });
        comboTerrain.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(terrain item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_terrain());
            }
        });

        comboPlante.setItems(FXCollections.observableArrayList(ps.afficherToutes()));
        comboPlante.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(plante item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_p() + " (" + item.getVariete() + ")");
            }
        });
        comboPlante.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(plante item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_p() + " (" + item.getVariete() + ")");
            }
        });

        comboStatus.setItems(FXCollections.observableArrayList("En cours", "Terminée"));
    }

    public void initialiserAvecRotation(rotation r) {
        this.rotationSelectionnee = r;
        comboTerrain.getItems().stream().filter(t -> t.getId_terrain() == r.getId_terrain()).findFirst().ifPresent(comboTerrain::setValue);
        comboPlante.getItems().stream().filter(p -> p.getId_plante() == r.getId_plante()).findFirst().ifPresent(comboPlante::setValue);
        if (r.getDate_debut_t() != null) dateDebut.setValue(new Date(r.getDate_debut_t().getTime()).toLocalDate());
        if (r.getDate_fin_t() != null) dateFin.setValue(new Date(r.getDate_fin_t().getTime()).toLocalDate());
        comboStatus.setValue(r.getStatus() == 1 ? "En cours" : "Terminée");
    }

    @FXML
    public void handleModifier(ActionEvent event) {
        if (rotationSelectionnee == null || comboTerrain.getValue() == null || comboPlante.getValue() == null ||
                dateDebut.getValue() == null || dateFin.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }
        if (dateFin.getValue().isBefore(dateDebut.getValue())) {
            showAlert("Erreur", "La date de fin doit être après la date de début.", Alert.AlertType.ERROR);
            return;
        }
        rotationSelectionnee.setId_terrain(comboTerrain.getValue().getId_terrain());
        rotationSelectionnee.setId_plante(comboPlante.getValue().getId_plante());
        rotationSelectionnee.setDate_debut_t(Date.valueOf(dateDebut.getValue()));
        rotationSelectionnee.setDate_fin_t(Date.valueOf(dateFin.getValue()));
        rotationSelectionnee.setStatus("En cours".equals(comboStatus.getValue()) ? 1 : 0);
        rs.modifier(rotationSelectionnee);
        showAlert("Succès", "Rotation modifiée avec succès !", Alert.AlertType.INFORMATION);
        retourListe(event);
    }

    @FXML
    public void retourListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/agricoleaffichagerotation.fxml"));
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
