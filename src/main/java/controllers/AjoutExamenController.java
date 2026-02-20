package controllers;

import entities.examens;
import entities.animaux;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceExamen;
import services.ServiceAnimal;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AjoutExamenController implements Initializable {

    @FXML private ComboBox<animaux> cbAnimal;
    @FXML private ComboBox<String> cbType;

    // CORRECTION : Déclaration des ComboBox (doivent correspondre aux IDs dans Scene Builder)
    @FXML private ComboBox<String> cbDiagnostic; // AJOUTÉ
    @FXML private ComboBox<String> cbTraitement; // AJOUTÉ

    @FXML private DatePicker dpDate;

    private ServiceExamen serviceEx = new ServiceExamen();
    private ServiceAnimal serviceAn = new ServiceAnimal();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Chargement des animaux
            if (cbAnimal != null) {
                cbAnimal.getItems().setAll(serviceAn.afficher());
            }

            // REMÈDE : Vérifier si la ComboBox est bien liée avant de faire .setItems()
            if (cbType != null) {
                cbType.setItems(FXCollections.observableArrayList("Vaccin", "Radio", "Scanner", "Consultation"));
            }

            if (cbDiagnostic != null) {
                cbDiagnostic.setItems(FXCollections.observableArrayList("En bonne santé", "Infection", "Fracture", "Urgence"));
            } else {
                // Si ce message s'affiche, c'est que l'ID dans Scene Builder est faux ou absent !
                System.err.println("ERREUR : cbDiagnostic n'est pas lié au FXML !");
            }

            if (cbTraitement != null) {
                cbTraitement.setItems(FXCollections.observableArrayList("Repos", "Antibiotiques", "Observation", "Chirurgie"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleAjouter(ActionEvent event) {
        if (estValide()) {
            try {
                examens e = new examens();
                e.setId_animal(cbAnimal.getValue().getId());
                e.setType_examen(cbType.getValue());
                e.setDate_examen(java.sql.Date.valueOf(dpDate.getValue()));

                // Utilisation des valeurs sélectionnées dans les ComboBox
                e.setDiagnostic(cbDiagnostic.getValue());
                e.setTraitement(cbTraitement.getValue());

                serviceEx.ajouter(e);
                retourListe(event);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean estValide() {
        String msg = "";
        if (cbAnimal.getValue() == null) msg += "- Veuillez sélectionner un animal.\n";

        if (cbType.getValue() == null || cbType.getValue().isEmpty()) {
            msg += "- Le type d'examen est obligatoire.\n";
        }

        if (dpDate.getValue() == null) msg += "- La date est obligatoire.\n";

        // CORRECTION : Vérification sur la ComboBox et non le TextArea
        if (cbDiagnostic.getValue() == null || cbDiagnostic.getValue().isEmpty()) {
            msg += "- Le diagnostic est obligatoire.\n";
        }

        if (cbTraitement.getValue() == null || cbTraitement.getValue().isEmpty()) {
            msg += "- Le traitement est obligatoire.\n";
        }

        if (!msg.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(msg);
            alert.showAndWait();
            return false;
        }
        return true;
    }

    @FXML
    void retourListe(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AfficherExamens.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}