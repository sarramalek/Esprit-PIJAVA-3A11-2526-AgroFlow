package controllers.Terrains;

import models.Terrains.terrain;
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
import services.Terrains.TerrainService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AffichageTerrainController implements Initializable {

    @FXML
    private TableView<terrain> tableTerrains;
    @FXML
    private TableColumn<terrain, String> colNom;
    @FXML
    private TableColumn<terrain, Float> colSurface;
    @FXML
    private TableColumn<terrain, String> colTypeSol;
    @FXML
    private TableColumn<terrain, String> colLocalisation;
    @FXML
    private TableColumn<terrain, Float> colPH;

    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboTri;

    private final TerrainService ts = new TerrainService();
    private ObservableList<terrain> listeTerrains;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTableau();
        configurerRecherche();
        configurerTri();
        chargerDonnees();
    }

    private void configurerTableau() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom_terrain"));
        colSurface.setCellValueFactory(new PropertyValueFactory<>("surface"));
        colTypeSol.setCellValueFactory(new PropertyValueFactory<>("type_sol"));
        colLocalisation.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        colPH.setCellValueFactory(new PropertyValueFactory<>("p_h"));
    }

    private void configurerRecherche() {
        // Recherche en temps réel
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                chargerDonnees();
            } else {
                rechercherTerrains(newValue);
            }
        });
    }

    private void configurerTri() {
        // Options de tri
        comboTri.setItems(FXCollections.observableArrayList(
                "Nom (A-Z)",
                "Nom (Z-A)",
                "Surface (croissante)",
                "Surface (décroissante)",
                "pH (acide au basique)",
                "pH (basique à acide)",
                "Type de sol (A-Z)"
        ));

        // Action lors du changement de tri
        comboTri.setOnAction(event -> {
            String critere = comboTri.getValue();
            if (critere != null) {
                trierTerrains(critere);
            }
        });
    }

    private void chargerDonnees() {
        listeTerrains = FXCollections.observableArrayList(ts.afficherTous());
        tableTerrains.setItems(listeTerrains);
    }

    private void rechercherTerrains(String motCle) {
        List<terrain> resultats = ts.rechercher(motCle);
        listeTerrains = FXCollections.observableArrayList(resultats);
        tableTerrains.setItems(listeTerrains);
    }

    private void trierTerrains(String critere) {
        List<terrain> resultats = ts.trierPar(critere);
        listeTerrains = FXCollections.observableArrayList(resultats);
        tableTerrains.setItems(listeTerrains);
    }

    @FXML
    public void reinitialiserRecherche(ActionEvent actionEvent) {
        txtRecherche.clear();
        comboTri.setValue(null);
        chargerDonnees();
    }

    @FXML
    public void versModifier(ActionEvent actionEvent) {
        terrain terrainSelectionne = tableTerrains.getSelectionModel().getSelectedItem();

        if (terrainSelectionne == null) {
            showAlert("Attention", "Veuillez sélectionner un terrain à modifier.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/ModifierTerrain.fxml"));
            Parent root = loader.load();

            ModifierTerrainController controller = loader.getController();
            controller.initialiserAvecTerrain(terrainSelectionne);

            Stage stage = (Stage) tableTerrains.getScene().getWindow();
            boolean etaitMaximise = stage.isMaximized();

            stage.setScene(new Scene(root));
            stage.setMaximized(etaitMaximise);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de modification", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleSupprimer(ActionEvent actionEvent) {
        terrain terrainSelectionne = tableTerrains.getSelectionModel().getSelectedItem();

        if (terrainSelectionne == null) {
            showAlert("Attention", "Veuillez sélectionner un terrain à supprimer.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "⚠️ ATTENTION ⚠️\n\n" +
                        "Supprimer le terrain '" + terrainSelectionne.getNom_terrain() + "' ?\n\n" +
                        "Cela supprimera aussi :\n" +
                        "• Toutes les rotations de ce terrain\n" +
                        "• L'historique des cultures\n\n" +
                        "Les plantes seront conservées.",
                ButtonType.YES, ButtonType.NO);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    ts.supprimerAvecRotations(terrainSelectionne.getId_terrain());
                    chargerDonnees();
                    showAlert("Succès", "Terrain et ses rotations supprimés avec succès.", Alert.AlertType.INFORMATION);
                } catch (RuntimeException e) {
                    showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    public void versAjout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/AjoutTerrain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableTerrains.getScene().getWindow();
            boolean etaitMaximise = stage.isMaximized();

            stage.setScene(new Scene(root));
            stage.setMaximized(etaitMaximise);
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
            Stage stage = (Stage) tableTerrains.getScene().getWindow();
            boolean etaitMaximise = stage.isMaximized();

            stage.setScene(new Scene(root));
            stage.setMaximized(etaitMaximise);
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