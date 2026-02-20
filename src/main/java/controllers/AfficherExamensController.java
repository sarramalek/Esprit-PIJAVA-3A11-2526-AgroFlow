package controllers;

import java.sql.SQLException;
import java.time.LocalDate;
import entities.examens;
import entities.animaux;
import javafx.animation.FadeTransition; // IMPORT NÉCESSAIRE POUR L'ANIMATION
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import services.ServiceExamen;
import services.ServiceAnimal;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.time.LocalDate;

public class AfficherExamensController {

    @FXML private TableView<examens> tvExamens;
    @FXML private TableColumn<examens, String> colAnimal;
    @FXML private TableColumn<examens, String> colType;
    @FXML private TableColumn<examens, java.sql.Date> colDate;
    @FXML private TableColumn<examens, String> colDiagnostic;
    @FXML private TableColumn<examens, String> colTraitement;

    @FXML private Circle badgeRouge;
    @FXML private Label lblNbAlertes;
    @FXML private StackPane paneNotification;
    @FXML private Button btnAjouter;

    private ServiceExamen service = new ServiceExamen();
    private ServiceAnimal serviceAn = new ServiceAnimal();

    @FXML
    public void initialize() {
        colAnimal.setCellValueFactory(cellData -> {
            int idAnimal = cellData.getValue().getId_animal();
            try {
                animaux a = serviceAn.afficher().stream()
                        .filter(an -> an.getId() == idAnimal)
                        .findFirst()
                        .orElse(null);
                if (a != null) {
                    return new SimpleStringProperty(a.getNom());
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
        demarrerSystemeAlerte();
    }

    private void chargerDonnees() {
        try {
            tvExamens.setItems(FXCollections.observableArrayList(service.afficher()));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des examens : " + e.getMessage());
        }
    }

    // --- SYSTÈME D'ALERTES ---

    private void demarrerSystemeAlerte() {
        // Vérification immédiate au lancement
        verifierRappelsAujourdhui();

        // Puis vérification toutes les 60 secondes
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(60), event -> {
            verifierRappelsAujourdhui();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void verifierRappelsAujourdhui() {
        try {
            // On récupère la date d'aujourd'hui au format SQL (AAAA-MM-JJ)
            String today = java.sql.Date.valueOf(java.time.LocalDate.now()).toString();

            long nbAlertes = service.afficher().stream()
                    .filter(e -> e.getDate_examen() != null &&
                            e.getDate_examen().toString().equals(today)) // Comparaison textuelle simple
                    .count();

            if (nbAlertes > 0) {
                declencherAnimationCloche(nbAlertes);
            } else if (badgeRouge != null) {
                badgeRouge.setVisible(false);
                lblNbAlertes.setVisible(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // MÉTHODE AJOUTÉE : Elle manquait dans ton code précédent
    private void declencherAnimationCloche(long nb) {
        if (badgeRouge != null && lblNbAlertes != null) {
            badgeRouge.setVisible(true);
            lblNbAlertes.setVisible(true);
            lblNbAlertes.setText(String.valueOf(nb));

            FadeTransition fade = new FadeTransition(Duration.seconds(0.5), badgeRouge);
            fade.setFromValue(1.0);
            fade.setToValue(0.3);
            fade.setCycleCount(6);
            fade.setAutoReverse(true);
            fade.play();
        }
    }

    @FXML
    void ouvrirDetailsAlertes() {
        // Optionnel : afficher une petite alerte avec la liste des animaux concernés
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rappels du jour");
        alert.setHeaderText("Examens à effectuer aujourd'hui");
        alert.setContentText("Vous avez " + lblNbAlertes.getText() + " examen(s) prévu(s).");
        alert.show();
    }

    // --- NAVIGATION ET ACTIONS ---

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