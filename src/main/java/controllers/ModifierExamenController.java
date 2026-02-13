package controllers;

import entities.examens;
import entities.animaux;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ServiceExamen;
import services.ServiceAnimal;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class ModifierExamenController {

    @FXML private ComboBox<animaux> cbAnimal;
    @FXML private TextField tfType;
    @FXML private DatePicker dpDate;
    @FXML private TextArea taDiagnostic;
    @FXML private TextArea taTraitement;

    private ServiceExamen serviceEx = new ServiceExamen();
    private ServiceAnimal serviceAn = new ServiceAnimal();
    private examens examenSelectionne;

    public void chargerDonnees(examens e) {
        this.examenSelectionne = e;

        try {
            cbAnimal.getItems().setAll(serviceAn.afficher());
            for (animaux a : cbAnimal.getItems()) {
                if (a.getId() == e.getId_animal()) {
                    cbAnimal.setValue(a);
                    break;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        tfType.setText(e.getType_examen());
        taDiagnostic.setText(e.getDiagnostic());
        taTraitement.setText(e.getTraitement());

        // --- CORRECTION ICI : De l'entité (Date) vers le DatePicker (LocalDate) ---
        if (e.getDate_examen() != null) {
            // On s'assure de traiter la valeur comme une java.util.Date
            java.util.Date utilDate = e.getDate_examen();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            dpDate.setValue(sqlDate.toLocalDate());
        }
    }

    @FXML
    void handleModifier(ActionEvent event) {
        if (estValide()) {
            try {
                examenSelectionne.setId_animal(cbAnimal.getValue().getId());
                examenSelectionne.setType_examen(tfType.getText());
                examenSelectionne.setDiagnostic(taDiagnostic.getText());
                examenSelectionne.setTraitement(taTraitement.getText());

                // --- DEUXIÈME CONVERSION : Du DatePicker vers l'Entité ---
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
        if (tfType.getText().trim().isEmpty()) msg += "- Type d'examen requis.\n";
        if (dpDate.getValue() == null) msg += "- Date requise.\n";
        if (taDiagnostic.getText().trim().isEmpty()) msg += "- Diagnostic requis.\n";

        if (!msg.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de modification");
            alert.setHeaderText("Veuillez corriger :");
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