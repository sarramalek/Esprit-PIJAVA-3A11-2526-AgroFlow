package controllers.Materiels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Materiels.Machine;
import services.Materiels.MachineService;

import java.io.IOException;
import java.time.LocalDate;

public class AfficherMachinesController {

    @FXML
    private TableView<Machine> tableMachines;

    @FXML
    private TableColumn<Machine, String> colMarque;

    @FXML
    private TableColumn<Machine, String> colModele;

    @FXML
    private TableColumn<Machine, String> colEtat;

    @FXML
    private TableColumn<Machine, String> colNumeroSerie;

    @FXML
    private TableColumn<Machine, LocalDate> colDateAchat;

    @FXML
    private TableColumn<Machine, String> colNom;

    private MachineService machineService = new MachineService();
    private ObservableList<Machine> machinesList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configuration des colonnes
        colMarque.setCellValueFactory(new PropertyValueFactory<>("marque"));
        colModele.setCellValueFactory(new PropertyValueFactory<>("modele"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatM"));
        colNumeroSerie.setCellValueFactory(new PropertyValueFactory<>("numeroSerie"));
        colDateAchat.setCellValueFactory(new PropertyValueFactory<>("dateAchat"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        // Chargement des données
        chargerMachines();
    }

    private void chargerMachines() {
        try {
            machinesList.clear();
            machinesList.addAll(machineService.recuperer());
            tableMachines.setItems(machinesList);
        } catch (Exception e) {
            afficherAlerte("Erreur", "Impossible de charger les machines", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void versAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutMachine.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableMachines.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir la page d'ajout", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void versModifier() {
        Machine machineSelectionnee = tableMachines.getSelectionModel().getSelectedItem();

        if (machineSelectionnee == null) {
            afficherAlerte("Attention", "Veuillez sélectionner une machine à modifier", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierMachine.fxml"));
            Parent root = loader.load();

            // Passer la machine sélectionnée au contrôleur de modification
            ModifierMachineController controller = loader.getController();
            controller.setMachine(machineSelectionnee);

            Stage stage = (Stage) tableMachines.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir la page de modification", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupprimer() {
        Machine machineSelectionnee = tableMachines.getSelectionModel().getSelectedItem();

        if (machineSelectionnee == null) {
            afficherAlerte("Attention", "Veuillez sélectionner une machine à supprimer", Alert.AlertType.WARNING);
            return;
        }

        try {
            machineService.supprimer(machineSelectionnee.getIdM());
            afficherAlerte("Succès", "Machine supprimée avec succès", Alert.AlertType.INFORMATION);
            chargerMachines(); // Rafraîchir la table
        } catch (Exception e) {
            afficherAlerte("Erreur", "Impossible de supprimer la machine", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void retourAccueil() {
        naviguerVers("/MaterielsInterface/AccueilMateriel.fxml");
    }

    // Navigation vers les autres pages
    @FXML
    private void naviguerAnimaux() {
        naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml");
    }

    @FXML
    private void naviguerMateriels() {
        naviguerVers("/MaterielsInterface/AccueilMateriel.fxml");
    }

    @FXML
    private void naviguerStocks() {
        naviguerVers("/StocksInterface/afficherarticle.fxml");
    }

    @FXML
    private void naviguerTerrains() {
        naviguerVers("/TerrainsInterface/AfficherTerrains.fxml");
    }

    @FXML
    private void naviguerEvenements() {
        naviguerVers("/EventsInterface/AccueilEvenement.fxml");
    }

    @FXML
    private void naviguerUsers() {
        naviguerVers("/UsersInterface/Acceuil.fxml");
    }

    private void naviguerVers(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) tableMachines.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible de naviguer vers la page demandée", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}