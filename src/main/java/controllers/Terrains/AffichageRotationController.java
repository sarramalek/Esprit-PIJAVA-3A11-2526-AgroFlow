package controllers.Terrains;

import models.Terrains.rotation;
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
import services.Terrains.RotationService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.List;
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

    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboStatut;
    @FXML
    private ComboBox<String> comboTri;

    private final RotationService rs = new RotationService();
    private ObservableList<rotation> listeRotations;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTableau();
        configurerRecherche();
        configurerFiltreStatut();
        configurerTri();
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

    private void configurerRecherche() {
        // Recherche en temps réel
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                chargerDonnees();
            } else {
                rechercherRotations(newValue);
            }
        });
    }

    private void configurerFiltreStatut() {
        // Options de filtre
        comboStatut.setItems(FXCollections.observableArrayList(
                "Tous",
                "En cours",
                "Terminée"
        ));
        comboStatut.setValue("Tous");

        // Action lors du changement de statut
        comboStatut.setOnAction(event -> {
            String statut = comboStatut.getValue();
            if (statut != null) {
                filtrerParStatut(statut);
            }
        });
    }

    private void configurerTri() {
        // Options de tri
        comboTri.setItems(FXCollections.observableArrayList(
                "Date début (récente)",
                "Date début (ancienne)",
                "Date fin (récente)",
                "Date fin (ancienne)",
                "Terrain (A-Z)",
                "Terrain (Z-A)",
                "Plante (A-Z)",
                "Statut (En cours d'abord)"
        ));

        // Action lors du changement de tri
        comboTri.setOnAction(event -> {
            String critere = comboTri.getValue();
            if (critere != null) {
                trierRotations(critere);
            }
        });
    }

    private void chargerDonnees() {
        listeRotations = FXCollections.observableArrayList(rs.afficherToutes());
        tableRotations.setItems(listeRotations);
    }

    private void rechercherRotations(String motCle) {
        List<rotation> resultats = rs.rechercher(motCle);
        listeRotations = FXCollections.observableArrayList(resultats);
        tableRotations.setItems(listeRotations);
    }

    private void filtrerParStatut(String statut) {
        if (statut.equals("Tous")) {
            chargerDonnees();
        } else {
            int statutInt = statut.equals("En cours") ? 1 : 0;
            List<rotation> resultats = rs.filtrerParStatut(statutInt);
            listeRotations = FXCollections.observableArrayList(resultats);
            tableRotations.setItems(listeRotations);
        }
    }

    private void trierRotations(String critere) {
        List<rotation> resultats = rs.trierPar(critere);
        listeRotations = FXCollections.observableArrayList(resultats);
        tableRotations.setItems(listeRotations);
    }

    @FXML
    public void reinitialiserRecherche(ActionEvent actionEvent) {
        txtRecherche.clear();
        comboStatut.setValue("Tous");
        comboTri.setValue(null);
        chargerDonnees();
    }

    @FXML
    public void versModifier(ActionEvent actionEvent) {
        rotation rotationSelectionnee = tableRotations.getSelectionModel().getSelectedItem();

        if (rotationSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une rotation à modifier.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/ModifierRotation.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/AjoutRotation.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/acceuilterrain.fxml"));
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