package controllers.Animaux;

import models.Animaux.examens;
import models.Animaux.animaux;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User.Personne;
import services.Animaux.ServiceExamen;
import services.Animaux.ServiceAnimal;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

public class AjoutExamenController {

    @FXML private ComboBox<animaux> cbAnimal;
    @FXML private TextField tfType;
    @FXML private DatePicker dpDate;
    @FXML private TextArea taDiagnostic;
    @FXML private TextArea taTraitement;

    private ServiceExamen serviceEx = new ServiceExamen();
    private ServiceAnimal serviceAn = new ServiceAnimal();

    @FXML
    public void initialize() {
        try {
            // Chargement de la liste des animaux dans le ComboBox
            cbAnimal.getItems().setAll(serviceAn.afficher());
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
                e.setType_examen(tfType.getText());
                e.setDate_examen(java.sql.Date.valueOf(dpDate.getValue()));
                e.setDiagnostic(taDiagnostic.getText());
                e.setTraitement(taTraitement.getText());

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
        if (tfType.getText().trim().isEmpty()) msg += "- Le type d'examen est obligatoire.\n";
        if (dpDate.getValue() == null) msg += "- La date est obligatoire.\n";
        if (taDiagnostic.getText().trim().isEmpty()) msg += "- Le diagnostic est obligatoire.\n";

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
                Personne currentUser = SessionManager.getCurrentUser();
                String fxml = (currentUser != null && currentUser.getRole() == 1)
                        ? "/AnimalsInterface/acceuilagricoleexamens.fxml"
                        : "/AnimalsInterface/AfficherExamens.fxml";

                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
                // On récupère le Stage et la Scene ACTUELLE
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = stage.getScene();

                // SOLUTION MIRACLE : On change la racine, pas la scène !
                scene.setRoot(root);

                // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
