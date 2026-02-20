package controllers;

import entities.examens;
import entities.animaux;
import javafx.collections.FXCollections; // AJOUTÉ
import javafx.collections.ObservableList; // AJOUTÉ
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; // AJOUTÉ
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceExamen;
import services.ServiceAnimal;

import java.io.IOException;
import java.net.URL; // AJOUTÉ
import java.sql.SQLException;
import java.util.ResourceBundle; // AJOUTÉ

public class ModifierExamenController implements Initializable { // AJOUT de Initializable

    @FXML private ComboBox<animaux> cbAnimal;
    @FXML private ComboBox<String> cbType;
    @FXML private ComboBox<String> cbDiagnostic; // AJOUTÉ
    @FXML private ComboBox<String> cbTraitement; // AJOUTÉ
    @FXML private DatePicker dpDate;

    private ServiceExamen serviceEx = new ServiceExamen();
    private ServiceAnimal serviceAn = new ServiceAnimal();
    private examens examenSelectionne;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // 1. Charger les animaux
            cbAnimal.getItems().setAll(serviceAn.afficher());

            // 2. Remplir impérativement les listes de choix
            cbType.setItems(FXCollections.observableArrayList(
                    "Vaccin", "Radio", "Scanner", "Consultation"
            ));

            cbDiagnostic.setItems(FXCollections.observableArrayList(
                    "En bonne santé", "Infection", "Fracture", "Urgence"
            ));

            cbTraitement.setItems(FXCollections.observableArrayList(
                    "Repos", "Antibiotiques", "Observation", "Chirurgie"
            ));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void chargerDonnees(examens e) {
        this.examenSelectionne = e;

        // Sélection de l'animal
        for (animaux a : cbAnimal.getItems()) {
            if (a.getId() == e.getId_animal()) {
                cbAnimal.setValue(a);
                break;
            }
        }

        // Maintenant que les listes ont des items, setValue fonctionnera !
        cbType.setValue(e.getType_examen());
        cbDiagnostic.setValue(e.getDiagnostic());
        cbTraitement.setValue(e.getTraitement());

        if (e.getDate_examen() != null) {
            java.sql.Date sqlDate = new java.sql.Date(e.getDate_examen().getTime());
            dpDate.setValue(sqlDate.toLocalDate());
        }
    }

    @FXML
    void handleModifier(ActionEvent event) {
        if (estValide()) {
            try {
                examenSelectionne.setId_animal(cbAnimal.getValue().getId());
                examenSelectionne.setType_examen(cbType.getValue());

                // Mise à jour avec les ComboBox
                examenSelectionne.setDiagnostic(cbDiagnostic.getValue());
                examenSelectionne.setTraitement(cbTraitement.getValue());

                if (dpDate.getValue() != null) {
                    examenSelectionne.setDate_examen(java.sql.Date.valueOf(dpDate.getValue()));
                }

                serviceEx.modifier(examenSelectionne);
                retourListe(event);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    private boolean estValide() {
        String msg = "";
        if (cbAnimal.getValue() == null) msg += "- Animal requis.\n";
        if (cbType.getValue() == null || cbType.getValue().isEmpty()) msg += "- Type requis.\n";
        if (dpDate.getValue() == null) msg += "- Date requise.\n";

        // CORRECTION : On vérifie la ComboBox au lieu du TextArea
        if (cbDiagnostic.getValue() == null || cbDiagnostic.getValue().isEmpty()) {
            msg += "- Diagnostic requis.\n";
        }

        if (!msg.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de modification");
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