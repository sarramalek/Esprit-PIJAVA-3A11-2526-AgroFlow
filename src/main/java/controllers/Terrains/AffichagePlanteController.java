package controllers.Terrains;

import models.Terrains.plante;
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
import services.Terrains.PlanteService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
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

    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboTri;

    private final PlanteService ps = new PlanteService();
    private ObservableList<plante> listePlantes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTableau();
        configurerRecherche();
        configurerTri();
        chargerDonnees();
    }

    private void configurerTableau() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_p"));
        colVariete.setCellValueFactory(new PropertyValueFactory<>("variete"));
        colBesoinEau.setCellValueFactory(new PropertyValueFactory<>("besoin_eau"));
        colCycle.setCellValueFactory(new PropertyValueFactory<>("cycle_jours"));
    }

    private void configurerRecherche() {
        // Recherche en temps réel
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                chargerDonnees();
            } else {
                rechercherPlantes(newValue);
            }
        });
    }

    private void configurerTri() {
        // Options de tri
        comboTri.setItems(FXCollections.observableArrayList(
                "Nom (A-Z)",
                "Nom (Z-A)",
                "Besoin en eau (croissant)",
                "Besoin en eau (décroissant)",
                "Cycle (court au long)",
                "Cycle (long au court)"
        ));

        // Action lors du changement de tri
        comboTri.setOnAction(event -> {
            String critere = comboTri.getValue();
            if (critere != null) {
                trierPlantes(critere);
            }
        });
    }

    private void chargerDonnees() {
        listePlantes = FXCollections.observableArrayList(ps.afficherToutes());
        tablePlantes.setItems(listePlantes);
    }

    private void rechercherPlantes(String motCle) {
        List<plante> resultats = ps.rechercher(motCle);
        listePlantes = FXCollections.observableArrayList(resultats);
        tablePlantes.setItems(listePlantes);
    }

    private void trierPlantes(String critere) {
        List<plante> resultats = ps.trierPar(critere);
        listePlantes = FXCollections.observableArrayList(resultats);
        tablePlantes.setItems(listePlantes);
    }

    @FXML
    public void reinitialiserRecherche(ActionEvent actionEvent) {
        txtRecherche.clear();
        comboTri.setValue(null);
        chargerDonnees();
    }

    @FXML
    public void versModifier(ActionEvent actionEvent) {
        plante planteSelectionnee = tablePlantes.getSelectionModel().getSelectedItem();

        if (planteSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une plante à modifier.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/ModifierPlante.fxml"));
            Parent root = loader.load();

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
                "⚠️ ATTENTION ⚠️\n\n" +
                        "Supprimer la plante '" + planteSelectionnee.getNom_p() + "' ?\n\n" +
                        "Cela supprimera aussi :\n" +
                        "• Toutes les rotations de cette plante\n" +
                        "• L'historique des cultures",
                ButtonType.YES, ButtonType.NO);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    ps.supprimerAvecRotations(planteSelectionnee.getId_plante());  // ← CHANGEMENT ICI
                    chargerDonnees();
                    showAlert("Succès", "Plante et ses rotations supprimées avec succès.", Alert.AlertType.INFORMATION);
                } catch (RuntimeException e) {
                    showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    public void versAjout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/ajoutplante.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tablePlantes.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void versAccueil(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/acceuilterrain.fxml"));
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