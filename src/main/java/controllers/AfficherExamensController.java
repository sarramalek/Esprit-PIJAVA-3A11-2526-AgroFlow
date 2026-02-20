package controllers;

import java.sql.SQLException;
import entities.examens;
import entities.animaux; // Importation nécessaire
import javafx.beans.property.SimpleStringProperty; // Pour transformer l'ID en Nom
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
import services.ServiceAnimal; // Importation du service
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class AfficherExamensController {

    @FXML private TableView<examens> tvExamens;
    // CORRECTION DU TYPE : String car on veut afficher le NOM de l'animal
    @FXML private TableColumn<examens, String> colAnimal;
    @FXML private TableColumn<examens, String> colType;
    @FXML private TableColumn<examens, java.sql.Date> colDate;
    @FXML private TableColumn<examens, String> colDiagnostic;
    @FXML private TableColumn<examens, String> colTraitement;

    @FXML private Button btnAjouter;

    private ServiceExamen service = new ServiceExamen();
    // SOLUTION : Déclaration du service animal qui manquait
    private ServiceAnimal serviceAn = new ServiceAnimal();

    @FXML
    public void initialize() {
        // Liaison personnalisée pour afficher le Nom au lieu de l'ID
        colAnimal.setCellValueFactory(cellData -> {
            int idAnimal = cellData.getValue().getId_animal();
            try {
                // On récupère le nom de l'animal via son ID dans la base
                animaux a = serviceAn.afficher().stream()
                        .filter(an -> an.getId() == idAnimal)
                        .findFirst()
                        .orElse(null);

                if (a != null) {
                    return new SimpleStringProperty(a.getNom()); // Retourne le Nom
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return new SimpleStringProperty("Inconnu (" + idAnimal + ")");
        });

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
            changerScene(event, "/ModifierExamen.fxml", selection);
        } else {
            afficherAlerteSelection();
        }
    }

    @FXML
    void naviguerAjout(ActionEvent event) {
        changerScene(event, "/AjoutExamen.fxml", null);
    }

    @FXML
    void naviguerVersAnimaux(ActionEvent event) {
        changerScene(event, "/AfficherAnimaux.fxml", null);
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
            System.err.println("Erreur ouverture stats : " + e.getMessage());
        }
    }

    private void changerScene(ActionEvent event, String fxmlPath, examens examenAModifier) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Fichier introuvable : " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            if (examenAModifier != null) {
                ModifierExamenController controller = loader.getController();
                controller.chargerDonnees(examenAModifier);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers " + fxmlPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void afficherAlerteSelection() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Veuillez sélectionner un examen dans le tableau.");
        alert.show();
    }

    @FXML
    void handleDeconnexion(ActionEvent event) {
        changerScene(event, "/Login.fxml", null);
    }
}