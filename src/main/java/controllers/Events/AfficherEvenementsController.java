package controllers.Events;

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
import models.Events.Evenement;
import services.Events.CategorieEvenementService;
import services.Events.EvenementService;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Optional;

public class AfficherEvenementsController {

    @FXML
    private TableView<Evenement> eventsTable;

    @FXML
    private TableColumn<Evenement, String> titreColumn;

    @FXML
    private TableColumn<Evenement, String> typeColumn;

    @FXML
    private TableColumn<Evenement, Date> dateDebutColumn;

    @FXML
    private TableColumn<Evenement, Date> dateFinColumn;

    @FXML
    private TableColumn<Evenement, String> lieuColumn;

    @FXML
    private TableColumn<Evenement, String> categorieColumn;

    @FXML
    private TableColumn<Evenement, String> statutColumn;

    @FXML
    private TableColumn<Evenement, Void> actionsColumn;

    @FXML
    private TextField searchField;

    private final EvenementService evenementService = new EvenementService();
    private final CategorieEvenementService categorieService = new CategorieEvenementService();
    private ObservableList<Evenement> evenements;

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        initColumns();
        try {
            loadEvenements();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les événements : " + e.getMessage());
        }
    }

    // ================= TABLE COLUMNS =================
    private void initColumns() {
        // Colonnes simples
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeEvenement"));
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Formatage des dates
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateDebutColumn.setCellFactory(col -> new TableCell<Evenement, Date>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(format.format(date));
                }
            }
        });

        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        dateFinColumn.setCellFactory(col -> new TableCell<Evenement, Date>() {
            private final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(format.format(date));
                }
            }
        });

        // Colonne catégorie : afficher le nom au lieu de l'ID
        categorieColumn.setCellValueFactory(cellData -> {
            try {
                int idCategorie = cellData.getValue().getIdCategorie();
                String nomCategorie = categorieService.getNomCategorieById(idCategorie);
                return new SimpleStringProperty(nomCategorie);
            } catch (SQLException e) {
                e.printStackTrace();
                return new SimpleStringProperty("Erreur");
            }
        });

        // Styliser la colonne statut avec des couleurs
        statutColumn.setCellFactory(col -> new TableCell<Evenement, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    switch (statut.toLowerCase()) {
                        case "planifié":
                            setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");
                            break;
                        case "terminé":
                            setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2; -fx-font-weight: bold;");
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

        addActionButtons();
    }

    // ================= LOAD DATA =================
    private void loadEvenements() throws SQLException {
        evenements = FXCollections.observableArrayList(evenementService.recuperer());
        eventsTable.setItems(evenements);
        System.out.println("✅ " + evenements.size() + " événements chargés");
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
                    Evenement evenement = getTableView().getItems().get(getIndex());
                    ouvrirPage("ModifierEvenement.fxml", evenement);
                });

                // ===== SUPPRIMER =====
                deleteBtn.setOnAction(e -> {
                    Evenement evenement = getTableView().getItems().get(getIndex());

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Suppression d'événement");
                    alert.setContentText("Voulez-vous vraiment supprimer l'événement \"" + evenement.getTitre() + "\" ?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            evenementService.supprimer(evenement.getIdEvenement());
                            showSuccess("Succès", "Événement supprimé avec succès !");
                            loadEvenements();
                        } catch (SQLException ex) {
                            showError("Erreur", "Impossible de supprimer l'événement : " + ex.getMessage());
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

    // ================= ADD EVENEMENT =================
    @FXML
    private void handleAddEvenement(ActionEvent event) {
        ouvrirPage("AjouterEvenement.fxml", null);
    }

    // ================= REFRESH =================
    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadEvenements();
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

    private void ouvrirPage(String fxml, Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/" + fxml));
            Parent root = loader.load();

            if (evenement != null) {
                ModifierEvenementController controller = loader.getController();
                controller.setEvenement(evenement);
            }

            Stage stage = (Stage) eventsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    private void ouvrirPageSimple(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + fxml));
            Stage stage = (Stage) eventsTable.getScene().getWindow();
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