package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Abonnements;
import models.Personne;
import services.AbonnementService;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class GestionAbonnements {
    //sub menu
    @FXML private VBox gestionSubmenu, operationsSubmenu,gestionContainer;
    @FXML private Button gestionToggle, operationsToggle;
    @FXML private Button gestionBtn;

    @FXML
    static Button dashboardBtn;
    @FXML private Button personnesBtn;
    @FXML private Button tachesBtn;
    @FXML private Button offresBtn;
    @FXML private Button abonnementsBtn;
    @FXML private Button affectationsBtn;
    @FXML private Button logoutBtn;
    @FXML private Button addAbonnementBtn;
    @FXML private Label userNameLabel;

    // Statistiques
    @FXML private Label totalAbonnementsLabel;
    @FXML private Label actifsLabel;
    @FXML private Label expiresLabel;
    @FXML private Label enAttenteLabel;

    // Recherche
    @FXML private TextField searchField;

    // Table
    @FXML private TableView<Abonnements> abonnementsTable;
    @FXML private TableColumn<Abonnements, Integer> idColumn;
    @FXML private TableColumn<Abonnements, Integer> cinColumn;
    @FXML private TableColumn<Abonnements, Integer> offreColumn;
    @FXML private TableColumn<Abonnements, String> dateInscriptionColumn;
    @FXML private TableColumn<Abonnements, String> dateExpirationColumn;
    @FXML private TableColumn<Abonnements, String> situationColumn;
    @FXML private TableColumn<Abonnements, Void> actionsColumn;

    private AbonnementService abonnementService;
    private ObservableList<Abonnements> abonnementsList;
    private ObservableList<Abonnements> allAbonnementsList;
    private static Personne currentUser;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        try {
            abonnementService = new AbonnementService();
            System.out.println("✓ GestionAbonnementsController initialisé");

            setupTable();
            loadAbonnements();
            setupSearch();
            updateStatistics();

        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'initialisation");
            e.printStackTrace();
            showError("Erreur d'initialisation", "Impossible de charger les abonnements");
        }
    }

    /**
     * Définir l'utilisateur connecté
     */
    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            System.out.println("✓ Utilisateur défini: " + user.getNom());
        }
    }

    /**
     * Configurer la table
     */
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_abonn"));
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
        offreColumn.setCellValueFactory(new PropertyValueFactory<>("id_offre"));
        dateInscriptionColumn.setCellValueFactory(new PropertyValueFactory<>("date_inscription"));
        dateExpirationColumn.setCellValueFactory(new PropertyValueFactory<>("date_expiration"));
        situationColumn.setCellValueFactory(new PropertyValueFactory<>("situation"));

        // Cell factory pour la situation avec couleurs
        situationColumn.setCellFactory(col -> new TableCell<Abonnements, String>() {
            @Override
            protected void updateItem(String situation, boolean empty) {
                super.updateItem(situation, empty);
                if (empty || situation == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(situation);
                    String color;
                    switch (situation.toLowerCase()) {
                        case "actif":
                            color = "#27AE60";
                            break;
                        case "expiré":
                        case "expire":
                            color = "#E74C3C";
                            break;
                        case "en attente":
                            color = "#F39C12";
                            break;
                        default:
                            color = "#95A5A6";
                    }
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        // Colonne Actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox hbox = new HBox(10, editBtn, deleteBtn);

            {
                hbox.setAlignment(Pos.CENTER);

                editBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-padding: 5 15; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-padding: 5 15; -fx-cursor: hand;");

                editBtn.setOnAction(event -> {
                    Abonnements abonnement = getTableView().getItems().get(getIndex());
                    handleEditAbonnement(abonnement);
                });

                deleteBtn.setOnAction(event -> {
                    Abonnements abonnement = getTableView().getItems().get(getIndex());
                    handleDeleteAbonnement(abonnement);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        abonnementsTable.setStyle("-fx-background-color: transparent;");
    }

    /**
     * Charger les abonnements
     */
    public void loadAbonnements() {
        try {
            List<Abonnements> abonnements = abonnementService.recuperer();
            allAbonnementsList = FXCollections.observableArrayList(abonnements);
            abonnementsList = FXCollections.observableArrayList(abonnements);
            abonnementsTable.setItems(abonnementsList);

            System.out.println("✓ " + abonnements.size() + " abonnements chargés");

        } catch (SQLException e) {
            System.err.println("✗ Erreur lors du chargement des abonnements");
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les abonnements");
        }
    }

    /**
     * Configurer la recherche
     */
    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    /**
     * Appliquer les filtres
     */
    private void applyFilters() {
        if (allAbonnementsList == null) return;

        String searchText = searchField.getText().toLowerCase();
        ObservableList<Abonnements> filteredList = FXCollections.observableArrayList();

        for (Abonnements a : allAbonnementsList) {
            boolean matchesSearch = searchText.isEmpty() ||
                    String.valueOf(a.getCin()).contains(searchText) ||
                    (a.getSituation() != null && a.getSituation().toLowerCase().contains(searchText));

            if (matchesSearch) {
                filteredList.add(a);
            }
        }

        abonnementsTable.setItems(filteredList);
        System.out.println("✓ " + filteredList.size() + " abonnements affichés");
    }

    /**
     * Mettre à jour les statistiques
     */
    private void updateStatistics() {
        if (allAbonnementsList == null || allAbonnementsList.isEmpty()) {
            totalAbonnementsLabel.setText("0");
            actifsLabel.setText("0");
            expiresLabel.setText("0");
            enAttenteLabel.setText("0");
            return;
        }

        int total = allAbonnementsList.size();
        int actifs = (int) allAbonnementsList.stream()
                .filter(a -> "actif".equalsIgnoreCase(a.getSituation()))
                .count();
        int expires = (int) allAbonnementsList.stream()
                .filter(a -> "expiré".equalsIgnoreCase(a.getSituation()) || "expire".equalsIgnoreCase(a.getSituation()))
                .count();
        int enAttente = (int) allAbonnementsList.stream()
                .filter(a -> "en attente".equalsIgnoreCase(a.getSituation()))
                .count();

        totalAbonnementsLabel.setText(String.valueOf(total));
        actifsLabel.setText(String.valueOf(actifs));
        expiresLabel.setText(String.valueOf(expires));
        enAttenteLabel.setText(String.valueOf(enAttente));
    }

    /**
     * Gérer l'ajout
     */
    @FXML
    private void handleAddAbonnement() {
        System.out.println("➕ Ajouter un abonnement");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutAbonnement.fxml"));
            Parent root = loader.load();

            AjoutAbonnements controller = loader.getController();
            controller.setGestionAbonnementsController(this);

            Stage stage = new Stage();
            stage.setTitle("Nouvel Abonnement");
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture du formulaire");
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    /**
     * Gérer la modification
     */
    private void handleEditAbonnement(Abonnements abonnement) {
        System.out.println("✏️ Modifier l'abonnement ID: " + abonnement.getId_abonn());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierAbonnement.fxml"));
            Parent root = loader.load();

            ModifierAbonnement controller = loader.getController();
            controller.setGestionAbonnementsController(this);
            controller.setAbonnement(abonnement);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'Abonnement");
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture du formulaire");
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    /**
     * Gérer la suppression
     */
    private void handleDeleteAbonnement(Abonnements abonnement) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'abonnement");
        alert.setContentText("Voulez-vous vraiment supprimer cet abonnement ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                abonnementService.supprimer(abonnement.getId_abonn());
                loadAbonnements();
                updateStatistics();
                showSuccess("Succès", "Abonnement supprimé avec succès");
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de supprimer l'abonnement");
            }
        }
    }

    /**
     * Navigation
     */
    @FXML
    private void handleDashboard() {
        navigateTo("/Acceuil.fxml", "AgroFlow - Accueil");
    }

    @FXML
    private void handlePersonnes() {
        navigateTo("/DashboardPersonnes.fxml", "AgroFlow - Gestion du Personnel");
    }

    @FXML
    private void handleTaches() {
        navigateTo("/GestionTache.fxml", "AgroFlow - Gestion des Tâches");
    }

    @FXML
    private void handleOffres() {
        navigateTo("/GestionOffres.fxml", "AgroFlow - Gestion des Offres");
    }



    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(GestionAbonnements.class.getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller != null && currentUser != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", Personne.class).invoke(controller, currentUser);
                } catch (Exception ignored) {}
            }

            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);

        } catch (IOException e) {
            e.printStackTrace();
            showInfo("À venir", "Ce module sera disponible prochainement");
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("AgroFlow - Connexion");

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }

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

    static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handlePersonnes(MouseEvent event )  {
        Acceuil.ouvrirPersonnes(event);
    }

    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        Acceuil.ouvrirTaches(event);
    }
    @FXML private void handleAffectations(MouseEvent event) { /* Charger vue Affectations */
        Acceuil.ouvrirAffectations(event);}
    @FXML private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        Acceuil.ouvrirAbonnements(event);}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        Acceuil.ouvrirOffres(event);}
    @FXML private void handleGestion(MouseEvent event) { /* Vue principale Gestion */
    }
    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleAddTask(ActionEvent actionEvent) {

    }

    public void handleRefresh(ActionEvent actionEvent) {

    }
}