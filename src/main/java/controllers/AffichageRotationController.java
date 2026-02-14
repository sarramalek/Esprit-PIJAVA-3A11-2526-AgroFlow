package controllers;

import entities.rotation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.RotationService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;

public class AffichageRotationController implements Initializable {

    @FXML
    private TableView<rotation> tableRotations;
    @FXML
    private TableColumn<rotation, String> colTerrain;
    @FXML
    private TableColumn<rotation, String> colPlante;
    @FXML
    private TableColumn<rotation, String> colVariete;
    @FXML
    private TableColumn<rotation, Date> colDateDebut;
    @FXML
    private TableColumn<rotation, Date> colDateFin;
    @FXML
    private TableColumn<rotation, String> colStatus;

    private final RotationService rs = new RotationService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTableau();
        chargerDonnees();
    }

    private void configurerTableau() {
        colTerrain.setCellValueFactory(new PropertyValueFactory<>("nom_terrain"));
        colPlante.setCellValueFactory(new PropertyValueFactory<>("nom_plante"));
        colVariete.setCellValueFactory(new PropertyValueFactory<>("variete_plante"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut_t"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin_t"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));
    }

    private void chargerDonnees() {
        ObservableList<rotation> liste = FXCollections.observableArrayList(rs.afficherToutes());
        tableRotations.setItems(liste);
    }

    @FXML
    public void versModifier(ActionEvent actionEvent) {
        rotation rotationSelectionnee = tableRotations.getSelectionModel().getSelectedItem();

        if (rotationSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une rotation à modifier.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierRotation.fxml"));
            Parent root = loader.load();

            ModifierRotationController controller = loader.getController();
            controller.initialiserAvecRotation(rotationSelectionnee);

            Stage stage = (Stage) tableRotations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de modification", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleSupprimer(ActionEvent actionEvent) {
        rotation rotationSelectionnee = tableRotations.getSelectionModel().getSelectedItem();

        if (rotationSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une rotation à supprimer.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer cette rotation ?",
                ButtonType.YES, ButtonType.NO);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    rs.supprimer(rotationSelectionnee.getId_rotation());
                    chargerDonnees();
                    showAlert("Succès", "Rotation supprimée avec succès.", Alert.AlertType.INFORMATION);
                } catch (RuntimeException e) {
                    showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    public void versAjout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutRotation.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableRotations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page d'ajout", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void versAccueil(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/acceuilterrain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableRotations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }
}