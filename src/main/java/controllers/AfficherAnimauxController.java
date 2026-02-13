package controllers;

import entities.animaux;
import entities.Sexe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.ServiceAnimal;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

public class AfficherAnimauxController {

    @FXML private TableView<animaux> tableAnimaux;
    @FXML private TableColumn<animaux, String> colNom;
    @FXML private TableColumn<animaux, String> colEspece;
    @FXML private TableColumn<animaux, Float> colPoids;
    @FXML private TableColumn<animaux, Date> colDate; // Nouvelle colonne
    @FXML private TableColumn<animaux, Sexe> colSexe; // Nouvelle colonne

    private ServiceAnimal service = new ServiceAnimal();

    @FXML
    public void initialize() {
        // Liaison de TOUTES les colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEspece.setCellValueFactory(new PropertyValueFactory<>("espece"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_naissance"));
        colSexe.setCellValueFactory(new PropertyValueFactory<>("sexe"));

        refreshTable();
    }

    private void refreshTable() {
        try {
            ObservableList<animaux> list = FXCollections.observableArrayList(service.afficher());
            tableAnimaux.setItems(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSupprimer(ActionEvent event) {
        animaux selectionne = tableAnimaux.getSelectionModel().getSelectedItem();
        if (selectionne != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer " + selectionne.getNom() + " ?");
            alert.setContentText("Voulez-vous vraiment supprimer cet animal ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.supprimer(selectionne.getId());
                    refreshTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            alerteSelection();
        }
    }

    @FXML
    void versModifier(ActionEvent event) {
        // 1. On récupère l'animal sélectionné dans la TableView
        animaux selectionne = tableAnimaux.getSelectionModel().getSelectedItem();

        if (selectionne != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierAnimal.fxml"));
                Parent root = loader.load();

                // 2. Accéder au contrôleur de la page de modification
                ModifierAnimalController controller = loader.getController();

                // 3. ENVOYER les données de l'animal au formulaire
                controller.chargerDonnees(selectionne);

                // 4. Afficher la nouvelle page
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Alerte si rien n'est sélectionné
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner un animal à modifier.");
            alert.show();
        }
    }


    @FXML
    void versAjout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ajoutAnimaux.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void naviguerVersExamens(ActionEvent event) {
        try {
            // Le nom du fichier doit être EXACT (attention aux majuscules)
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherExamens.fxml"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Pour garantir que la taille reste identique (1100x700)
            Scene scene = stage.getScene();
            scene.setRoot(root);

        } catch (IOException e) {
            System.err.println("Le fichier /AfficherExamens.fxml est introuvable ou contient une erreur !");
            e.printStackTrace();
        }
    }

    @FXML
    void goToExamens(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherExamens.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Dans tes contrôleurs (ou une classe Helper)
    private void changerScene(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers " + fxmlFile + " : " + e.getMessage());
        }
    }

    @FXML void naviguerAnimaux(ActionEvent event) { changerScene(event, "AfficherAnimaux.fxml"); }
    @FXML void naviguerMateriels(ActionEvent event) { changerScene(event, "AfficherMateriels.fxml"); }
    @FXML void naviguerStocks(ActionEvent event) { changerScene(event, "AfficherStocks.fxml"); }
    @FXML void naviguerTerrains(ActionEvent event) { changerScene(event, "AfficherTerrains.fxml"); }
    @FXML void naviguerEvenements(ActionEvent event) { changerScene(event, "AfficherEvenements.fxml"); }
    @FXML void naviguerUsers(ActionEvent event) { changerScene(event, "AfficherUsers.fxml"); }
    private void alerteSelection() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Veuillez sélectionner un animal dans le tableau.");
        alert.show();
    }
}