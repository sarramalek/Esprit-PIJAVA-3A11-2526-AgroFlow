package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.Participation;
import services.EvenementService;
import services.ParticipationService;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AfficherParticipationsController {

    @FXML
    private TableView<Participation> participationsTable;

    @FXML
    private TableColumn<Participation, String> evenementColumn;

    @FXML
    private TableColumn<Participation, LocalDate> dateInscriptionColumn;

    @FXML
    private TableColumn<Participation, String> statutColumn;

    @FXML
    private TableColumn<Participation, Boolean> presenceColumn;

    @FXML
    private TableColumn<Participation, Void> actionsColumn;

    @FXML
    private TextField searchField;

    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService evenementService = new EvenementService();
    private ObservableList<Participation> participations;

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        initColumns();
        try {
            loadParticipations();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les participations : " + e.getMessage());
        }
    }

    // ================= TABLE COLUMNS =================
    private void initColumns() {
        // Colonne Événement : afficher le titre au lieu de l'ID
        evenementColumn.setCellValueFactory(cellData -> {
            try {
                int idEvenement = cellData.getValue().getId_evenement();
                String titre = evenementService.getNomEvenementById(idEvenement);
                return new SimpleStringProperty(titre);
            } catch (SQLException e) {
                e.printStackTrace();
                return new SimpleStringProperty("Erreur");
            }
        });

        // Colonne Date d'inscription
        dateInscriptionColumn.setCellValueFactory(new PropertyValueFactory<>("date_inscription"));
        dateInscriptionColumn.setCellFactory(col -> new TableCell<Participation, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Colonne Statut avec couleurs
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut_participation"));
        statutColumn.setCellFactory(col -> new TableCell<Participation, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    switch (statut.toLowerCase()) {
                        case "inscrit":
                            setStyle("-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-font-weight: bold;");
                            break;
                        case "confirmé":
                            setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                            break;
                        case "annulé":
                            setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Colonne Présence
        presenceColumn.setCellValueFactory(new PropertyValueFactory<>("presence"));
        presenceColumn.setCellFactory(col -> new TableCell<Participation, Boolean>() {
            @Override
            protected void updateItem(Boolean presence, boolean empty) {
                super.updateItem(presence, empty);
                if (empty || presence == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (presence) {
                        setText("✓ Oui");
                        setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                    } else {
                        setText("✗ Non");
                        setStyle("-fx-text-fill: #C62828; -fx-font-weight: bold;");
                    }
                }
            }
        });

        addActionButtons();
    }

    // ================= LOAD DATA =================
    private void loadParticipations() throws SQLException {
        participations = FXCollections.observableArrayList(participationService.recuperer());
        participationsTable.setItems(participations);
        System.out.println("✅ " + participations.size() + " participations chargées");
    }

    // ================= ACTION BUTTONS =================
    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn = new Button("✏ Modifier");
            private final Button deleteBtn = new Button("🗑 Supprimer");
            private final HBox box = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color:#F39C12; -fx-text-fill:white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color:#E74C3C; -fx-text-fill:white; -fx-cursor: hand;");

                // ===== MODIFIER =====
                editBtn.setOnAction(e -> {
                    Participation participation = getTableView().getItems().get(getIndex());
                    ouvrirPage("ModifierParticipation.fxml", participation);
                });

                // ===== SUPPRIMER =====
                deleteBtn.setOnAction(e -> {
                    Participation participation = getTableView().getItems().get(getIndex());

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Suppression de participation");
                    alert.setContentText("Voulez-vous vraiment supprimer cette participation ?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            participationService.supprimer(participation);
                            showSuccess("Succès", "Participation supprimée avec succès !");
                            loadParticipations();
                        } catch (SQLException ex) {
                            showError("Erreur", "Impossible de supprimer la participation : " + ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ================= ADD PARTICIPATION =================
    @FXML
    private void handleAddParticipation(ActionEvent event) {
        ouvrirPage("AjouterParticipation.fxml", null);
    }

    // ================= REFRESH =================
    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadParticipations();
            showSuccess("Actualisation", "Liste actualisée avec succès !");
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'actualiser : " + e.getMessage());
        }
    }

    // ================= NAVIGATION =================
    @FXML
    private void goToAccueil(ActionEvent event) {
        ouvrirPageSimple("Accueil.fxml");
    }

    private void ouvrirPage(String fxml, Participation participation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/" + fxml));
            Parent root = loader.load();

            if (participation != null) {
                ModifierParticipationController controller = loader.getController();
                controller.setParticipation(participation);
            }

            Stage stage = (Stage) participationsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    private void ouvrirPageSimple(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + fxml));
            Stage stage = (Stage) participationsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    // ================= ALERT METHODS =================
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}