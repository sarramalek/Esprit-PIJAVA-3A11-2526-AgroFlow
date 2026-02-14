package controllers;

import entities.plante;
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
import services.PlanteService;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AffichagePlanteController implements Initializable {

    @FXML
    private TableView<plante> tablePlantes;
    @FXML
    private TableColumn<plante, String> colNom;
    @FXML
    private TableColumn<plante, String> colVariete;
    @FXML
    private TableColumn<plante, Float> colBesoinEau;
    @FXML
    private TableColumn<plante, Integer> colCycle;

    private final PlanteService ps = new PlanteService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTableau();
        chargerDonnees();
    }

    private void configurerTableau() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_p"));
        colVariete.setCellValueFactory(new PropertyValueFactory<>("variete"));
        colBesoinEau.setCellValueFactory(new PropertyValueFactory<>("besoin_eau"));
        colCycle.setCellValueFactory(new PropertyValueFactory<>("cycle_jours"));
    }

    private void chargerDonnees() {
        ObservableList<plante> liste = FXCollections.observableArrayList(ps.afficherToutes());
        tablePlantes.setItems(liste);
    }

    @FXML
    public void versModifier(ActionEvent actionEvent) {
        plante planteSelectionnee = tablePlantes.getSelectionModel().getSelectedItem();

        if (planteSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une plante à modifier.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierPlante.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur et passer la plante sélectionnée
            ModifierPlanteController controller = loader.getController();
            controller.initialiserAvecPlante(planteSelectionnee);

            Stage stage = (Stage) tablePlantes.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de modification", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleSupprimer(ActionEvent actionEvent) {
        plante planteSelectionnee = tablePlantes.getSelectionModel().getSelectedItem();

        if (planteSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une plante à supprimer.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer " + planteSelectionnee.getNom_p() + " ?",
                ButtonType.YES, ButtonType.NO);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                ps.supprimer(planteSelectionnee.getId_plante());
                chargerDonnees();
                showAlert("Succès", "Plante supprimée avec succès.", Alert.AlertType.INFORMATION);
            }
        });
    }

    @FXML
    public void versAjout(ActionEvent actionEvent) {
        // Navigation vers l'ajout
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutPlante.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tablePlantes.getScene().getWindow();
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