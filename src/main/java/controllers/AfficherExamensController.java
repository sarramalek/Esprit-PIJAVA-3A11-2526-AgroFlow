package controllers;

import java.sql.SQLException;
import entities.examens;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import services.ServiceExamen;
import java.io.IOException;
import java.util.Optional;

public class AfficherExamensController {

    @FXML private TableView<examens> tvExamens;
    @FXML private TableColumn<examens, Integer> colAnimal;
    @FXML private TableColumn<examens, String> colType;
    @FXML private TableColumn<examens, java.sql.Date> colDate;
    @FXML private TableColumn<examens, String> colDiagnostic;
    @FXML private TableColumn<examens, String> colTraitement;

    @FXML private Button btnAjouter;

    private ServiceExamen service = new ServiceExamen();

    @FXML
    public void initialize() {
        // Liaison des colonnes
        colAnimal.setCellValueFactory(new PropertyValueFactory<>("id_animal"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type_examen"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_examen"));
        colDiagnostic.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));

        if (colTraitement != null) {
            colTraitement.setCellValueFactory(new PropertyValueFactory<>("traitement"));
        }

        if (btnAjouter != null) {
            btnAjouter.setCursor(Cursor.HAND);
        }

        chargerDonnees();
    }

    private void chargerDonnees() {
        try {
            tvExamens.setItems(FXCollections.observableArrayList(service.afficher()));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des examens : " + e.getMessage());
        }
    }

    @FXML
    void handleSupprimer(ActionEvent event) {
        examens selection = tvExamens.getSelectionModel().getSelectedItem();
        if (selection != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Suppression");
            alert.setContentText("Voulez-vous vraiment supprimer cet examen ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.supprimer(selection.getId());
                    chargerDonnees();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            afficherAlerteSelection();
        }
    }

    @FXML
    void handleModifier(ActionEvent event) {
        examens selection = tvExamens.getSelectionModel().getSelectedItem();
        if (selection != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierExamen.fxml"));
                Parent root = loader.load();

                // Transmission de l'objet au contrôleur de modification
                ModifierExamenController controller = loader.getController();
                controller.chargerDonnees(selection);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            afficherAlerteSelection();
        }
    }

    @FXML
    void naviguerAjout(ActionEvent event) {
        changerScene(event, "/AjoutExamen.fxml");
    }

    @FXML
    void naviguerVersAnimaux(ActionEvent event) {
        changerScene(event, "/AfficherAnimaux.fxml");
    }
    @FXML
    void ouvrirStats(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/StatsExamens.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Statistiques Examens - AgroFlow");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode utilitaire pour simplifier la navigation
    private void changerScene(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Erreur de navigation : " + e.getMessage());
        }
    }

    private void afficherAlerteSelection() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Veuillez sélectionner un examen dans le tableau.");
        alert.show();
    }
    @FXML
    void handleDeconnexion(ActionEvent event) {
        try {
            // Remplacez "/Login.fxml" par le nom exact de votre page de connexion
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Erreur lors de la déconnexion : " + e.getMessage());
        }
    }
}