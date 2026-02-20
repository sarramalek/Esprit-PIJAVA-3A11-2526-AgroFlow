package controllers.Events;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Events.Participation;
import services.Events.EvenementService;
import services.Events.ParticipationService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AfficherParticipationsController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
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
    public void initialize(Event event) {

            // Cacher submenu par défaut
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);

            // 1. Hover sur le bouton Gestion → Ouvre submenu
            gestionBtn.setOnMouseEntered(e -> {
                showGestionSubmenu();
            });

            // 2. Hover sur TOUT le container Gestion → Garde submenu ouvert
            gestionContainer.setOnMouseEntered(e -> {
                showGestionSubmenu();
            });
        initColumns(event);
        try {
            loadParticipations();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les participations : " + e.getMessage());
        }
    }

    // ================= TABLE COLUMNS =================
    private void initColumns(Event event) {
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

        addActionButtons(event);
    }

    // ================= LOAD DATA =================
    private void loadParticipations() throws SQLException {
        participations = FXCollections.observableArrayList(participationService.recuperer());
        participationsTable.setItems(participations);
        System.out.println("✅ " + participations.size() + " participations chargées");
    }

    // ================= ACTION BUTTONS =================
    private void addActionButtons(Event event) {
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
                    ouvrirPage(event,"ModifierParticipation.fxml");
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
                            participationService.supprimer(participation.getId_participation());
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
        ouvrirPage(event,"AjouterParticipation.fxml");
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

    private void ouvrirPage(Event event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));

            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxml);
            e.printStackTrace();
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

    @FXML
    private void handlePersonnes(Event event )  {
        this.ouvrirPage(event,"/UsersInterface/DahboardPersonne.fxml");}


    @FXML private void handleTaches(Event event ) { /* Charger vue Tâches */
        this.ouvrirPage(event,"/UsersInterface/GestionTache.fxml");}



    @FXML
    private void handleAbonnements(Event event) { /* Charger vue Abonnements */
        this.ouvrirPage(event,"/UsersInterface/GestionAbonnements.fxml");}
    @FXML private void handleOffres(Event event) { /* Charger vue Offres */
        this.ouvrirPage(event,"/UsersInterface/GestionOffre.fxml");}


    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) {
        this.ouvrirPage(actionEvent,"/UsersInterface/Acceuil.fxml");

    }
    public void handleAnimals(Event mouseEvent) {
        this.ouvrirPage(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml");

    }




    public void handleStocks(Event mouseEvent) {
        this.ouvrirPage(mouseEvent,"/StocksInterface/afficherarticle.fxml");
    }



    public void handleTerrains(Event mouseEvent) {
        this.ouvrirPage(mouseEvent,"/TerrainsInterface/acceuilterrain.fxml");
    }


    //
    public void handleEvents(Event mouseEvent) {
        this.ouvrirPage(mouseEvent,"/G-Evenements/Accueil.fxml");
    }


    public void handleMateriels(Event mouseEvent) {
        this.ouvrirPage(mouseEvent,"/MaterielsInterface/AccueilMateriel.fxml");
    }
    @FXML
    private void handleLogout() {
        System.out.println("🚪 Déconnexion...");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                Scene scene = new Scene(root, 900, 600);
                stage.setScene(scene);
                stage.setTitle("AgroFlow - Connexion");
                stage.setMaximized(true);

                System.out.println("✓ Déconnexion réussie");

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }


    /**
     * Afficher une information
     */
    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}