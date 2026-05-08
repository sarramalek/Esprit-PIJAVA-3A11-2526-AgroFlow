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
import javafx.scene.control.ListCell;
import javafx.scene.control.DatePicker;
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

public class AjoutRotationAgriculteurController {
    @FXML private ComboBox<terrain> comboTerrain;
    @FXML private ComboBox<plante> comboPlante;
    @FXML private DatePicker dateDebut;
    @FXML private DatePicker dateFin;
    @FXML private ComboBox<String> comboStatus;

    private final RotationService rs = new RotationService();
    private final TerrainService ts = new TerrainService();
    private final PlanteService ps = new PlanteService();
    private Personne currentUser;

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
        comboStatus.setValue("En cours");
    }

    @FXML
    void ajouterRotation(ActionEvent event) {
        if (comboTerrain.getValue() == null || comboPlante.getValue() == null ||
                dateDebut.getValue() == null || dateFin.getValue() == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }
        if (dateFin.getValue().isBefore(dateDebut.getValue())) {
            showAlert("Erreur", "La date de fin doit être après la date de début.", Alert.AlertType.ERROR);
            return;
        }
        rotation r = new rotation(0,
                comboTerrain.getValue().getId_terrain(),
                comboPlante.getValue().getId_plante(),
                Date.valueOf(dateDebut.getValue()),
                Date.valueOf(dateFin.getValue()),
                "En cours".equals(comboStatus.getValue()) ? 1 : 0
        );
        rs.ajouter(r);
        showAlert("Succès", "Rotation ajoutée avec succès !", Alert.AlertType.INFORMATION);
        retourListe(event);
    }

    @FXML
    void retourListe(ActionEvent event) {
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
