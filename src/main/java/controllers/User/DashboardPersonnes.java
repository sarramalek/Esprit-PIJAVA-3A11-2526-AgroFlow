package controllers.User;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import models.User.Personne;
import models.User.Employe;
import services.User.PersonneService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DashboardPersonnes {
//sub menu
    @FXML private VBox gestionSubmenu, operationsSubmenu,gestionContainer;


    @FXML private Button gestionToggle, operationsToggle;
    @FXML private Button gestionBtn;

    // Boutons de filtre
    @FXML private Button filterAllBtn;
    @FXML private Button filterAgricoleBtn;
    @FXML private Button filterEmployeBtn;
    @FXML private Button filterAdminBtn;

    // Boutons de navigation
    @FXML private Button dashboardBtn;
    @FXML private Button tachesBtn;
    @FXML private Button logoutBtn;
    @FXML private Button addEmployeeBtn;
    @FXML private Label userNameLabel;
    @FXML private TextField searchField;

    // Table et colonnes - CHANGÉ EN PERSONNE
    @FXML private TableView<Personne> employeeTable;
    @FXML private TableColumn<Personne, String> nomColumn;
    @FXML private TableColumn<Personne, String> emailColumn;
    @FXML private TableColumn<Personne, Integer> roleColumn;
    @FXML private TableColumn<Personne, String> dateColumn;
    @FXML private TableColumn<Personne, Void> actionsColumn;

    // Formulaire d'assignation de tâche
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private TextField taskDescriptionField;
    @FXML private DatePicker dueDatePicker;
    @FXML private Button assignTaskBtn;

    private PersonneService personneService;
    private ObservableList<Personne> employeeList;
    private ObservableList<Personne> allPersonsList;
    private Personne currentUser;
    private String currentFilter = "all";

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
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

        // 3. SOURIS SORT DU CONTAINER ENTIER → Ferme submenu
        gestionContainer.setOnMouseExited(e -> {
            hideGestionSubmenu();
        });
        try {
            personneService = new PersonneService();
            System.out.println("✓ DashboardController initialisé");

            // Configurer la table
            setupTable();

            // Charger les personnes
            loadEmployees();

            // Configurer la recherche
            setupSearch();

        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'initialisation du dashboard");
            e.printStackTrace();
            showError("Erreur d'initialisation", "Impossible de charger le dashboard");
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
        // Nom
        nomColumn.setCellValueFactory(cellData -> {
            Personne p = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(p.getPrenom() + " " + p.getNom());
        });

        // Email
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Rôle avec couleurs
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setCellFactory(col -> new TableCell<Personne, Integer>() {
            @Override
            protected void updateItem(Integer role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String roleText;
                    String color;
                    switch (role) {
                        case 1: // Agricole/Utilisateur
                            roleText = "🌾 Agricole";
                            color = "#27AE60";
                            break;
                        case 2: // Employé
                            roleText = "👷 Employé";
                            color = "#F39C12";
                            break;
                        case 3: // Admin
                            roleText = "👑 Admin";
                            color = "#9B59B6";
                            break;
                        default:
                            roleText = "Inconnu";
                            color = "#95A5A6";
                    }
                    setText(roleText);
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        // Date
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_creationcpt"));

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
                    Personne p = getTableView().getItems().get(getIndex());
                    handleEditEmployee(p);
                });

                deleteBtn.setOnAction(event -> {
                    Personne p = getTableView().getItems().get(getIndex());
                    handleDeleteEmployee(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        employeeTable.setStyle("-fx-background-color: transparent;");
    }

    /**
     * Charger toutes les personnes
     */
    public void loadEmployees() {
        try {
            List<Personne> personnes = personneService.recuperer();
            allPersonsList = FXCollections.observableArrayList(personnes);
            employeeList = FXCollections.observableArrayList(personnes);
            employeeTable.setItems(employeeList);

            // Remplir le ComboBox
            ObservableList<String> employeeNames = FXCollections.observableArrayList();
            for (Personne p : personnes) {
                employeeNames.add(p.getPrenom() + " " + p.getNom());
            }
            if (employeeComboBox != null) {
                employeeComboBox.setItems(employeeNames);
            }

            System.out.println("✓ " + personnes.size() + " personnes chargées");

        } catch (SQLException e) {
            System.err.println("✗ Erreur lors du chargement des personnes");
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les personnes");
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
     * Filtrer - Afficher tous
     */
    @FXML
    private void handleFilterAll() {
        System.out.println("🔍 Filtre: Tous");
        currentFilter = "all";
        applyFilters();
        updateFilterButtonStyles();
    }

    /**
     * Filtrer - Agricoles uniquement
     */
    @FXML
    private void handleFilterAgricole() {
        System.out.println("🔍 Filtre: Agricoles (rôle 1)");
        currentFilter = "agricole";
        applyFilters();
        updateFilterButtonStyles();
    }

    /**
     * Filtrer - Employés uniquement
     */
    @FXML
    private void handleFilterEmploye() {
        System.out.println("🔍 Filtre: Employés (rôle 2)");
        currentFilter = "employe";
        applyFilters();
        updateFilterButtonStyles();
    }

    /**
     * Filtrer - Admins uniquement
     */
    @FXML
    private void handleFilterAdmin() {
        System.out.println("🔍 Filtre: Admins (rôle 3)");
        currentFilter = "admin";
        applyFilters();
        updateFilterButtonStyles();
    }

    /**
     * Appliquer les filtres (recherche + rôle)
     */
    private void applyFilters() {
        if (allPersonsList == null) return;

        String searchText = searchField.getText().toLowerCase();
        ObservableList<Personne> filteredList = FXCollections.observableArrayList();

        for (Personne p : allPersonsList) {
            // Filtre par recherche
            boolean matchesSearch = searchText.isEmpty() ||
                    (p.getNom() != null && p.getNom().toLowerCase().contains(searchText)) ||
                    (p.getPrenom() != null && p.getPrenom().toLowerCase().contains(searchText)) ||
                    (p.getEmail() != null && p.getEmail().toLowerCase().contains(searchText));

            // Filtre par rôle
            boolean matchesRole = false;
            switch (currentFilter) {
                case "all":
                    matchesRole = true;
                    break;
                case "agricole":
                    matchesRole = (p.getRole() == 1);
                    break;
                case "employe":
                    matchesRole = (p.getRole() == 2);
                    break;
                case "admin":
                    matchesRole = (p.getRole() == 3);
                    break;
            }

            if (matchesSearch && matchesRole) {
                filteredList.add(p);
            }
        }

        employeeTable.setItems(filteredList);
        System.out.println("✓ " + filteredList.size() + " personnes affichées après filtrage");
    }

    /**
     * Mettre à jour le style des boutons de filtre
     */
    private void updateFilterButtonStyles() {
        String activeStyle = "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;";
        String inactiveAll = "-fx-background-color: transparent; -fx-border-color: #3498DB; -fx-border-width: 2; -fx-text-fill: #3498DB; -fx-font-size: 13px; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;";
        String inactiveAgricole = "-fx-background-color: transparent; -fx-border-color: #27AE60; -fx-border-width: 2; -fx-text-fill: #27AE60; -fx-font-size: 13px; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;";
        String inactiveEmploye = "-fx-background-color: transparent; -fx-border-color: #F39C12; -fx-border-width: 2; -fx-text-fill: #F39C12; -fx-font-size: 13px; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;";
        String inactiveAdmin = "-fx-background-color: transparent; -fx-border-color: #9B59B6; -fx-border-width: 2; -fx-text-fill: #9B59B6; -fx-font-size: 13px; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;";

        filterAllBtn.setStyle(currentFilter.equals("all") ? activeStyle : inactiveAll);
        filterAgricoleBtn.setStyle(currentFilter.equals("agricole") ? activeStyle : inactiveAgricole);
        filterEmployeBtn.setStyle(currentFilter.equals("employe") ? activeStyle : inactiveEmploye);
        filterAdminBtn.setStyle(currentFilter.equals("admin") ? activeStyle : inactiveAdmin);
    }

    /**
     * Gérer le bouton Dashboard
     */
    @FXML
    private void handleDashboard(MouseEvent event) {
      navigateTo(event, "/UsersInterface/Acceuil.fxml","Acceuil - Agroflow ");

    }

    /**
     * Gérer l'ajout d'un employé
     */
    @FXML
    private void handleAddEmployee(MouseEvent event) {
        System.out.println("➕ Ajouter un employé cliqué");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/AjoutPersonne.fxml"));
            Parent root = loader.load();

            AjoutPersonne controller = loader.getController();
            controller.setDashboardController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Employé");
            stage.setScene(new Scene(root, 550, 650));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();

            System.out.println("✓ Fenêtre d'ajout ouverte");
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture de la fenêtre d'ajout");
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    /**
     * Gérer la modification
     */
    private void handleEditEmployee(Personne personne) {
        System.out.println("✏️ Modifier: " + personne.getNom());

        if (personne.getRole() == 2 && personne instanceof Employe) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ModifierPersonne.fxml"));
                Parent root = loader.load();

                ModifierPersonne controller = loader.getController();
                controller.setDashboardController(this);
                controller.setEmploye((Employe) personne);

                Stage stage = new Stage();
                stage.setTitle("Modifier l'Employé");
                stage.setScene(new Scene(root, 550, 650));
                stage.setResizable(false);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.centerOnScreen();

                stage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible d'ouvrir le formulaire de modification");
            }
        } else {
            showInfo("Information", "La modification est disponible uniquement pour les employés");
        }
    }

    /**
     * Gérer la suppression
     */
    private void handleDeleteEmployee(Personne personne) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la personne");
        alert.setContentText("Voulez-vous vraiment supprimer " + personne.getPrenom() + " " + personne.getNom() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                personneService.supprimer(personne.getCin());
                loadEmployees();
                applyFilters();
                showSuccess("Succès", "Personne supprimée avec succès");
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de supprimer la personne");
            }
        }
    }

    /**
     * Gérer l'assignation de tâche
     */
    @FXML
    private void handleAssignTask() {
        String employee = employeeComboBox.getValue();
        String description = taskDescriptionField.getText();
        LocalDate dueDate = dueDatePicker.getValue();

        if (employee == null || description.isEmpty() || dueDate == null) {
            showError("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        System.out.println("Assigner tâche:");
        System.out.println("  Employé: " + employee);
        System.out.println("  Description: " + description);
        System.out.println("  Date: " + dueDate);

        showSuccess("Succès", "Tâche assignée à " + employee);

        employeeComboBox.setValue(null);
        taskDescriptionField.clear();
        dueDatePicker.setValue(null);
    }

    /**
     * Navigation vers gestion des tâches
     */
    @FXML
    private void handleTaches() {
        System.out.println("🗂️ Navigation vers la gestion des tâches...");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/GestionTache.fxml"));
            Parent root = loader.load();

            GestionTache controller = loader.getController();
            if (currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) dashboardBtn.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Gestion des Tâches");
            stage.setMaximized(true);


            System.out.println("✓ Navigation réussie vers Gestion des Tâches");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de la navigation vers Gestion des Tâches");
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la gestion des tâches");
        }
    }

    /**
     * Gérer la déconnexion
     */
    @FXML
    private void handleLogout() {
        System.out.println("Déconnexion...");

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
                Scene scene = new Scene(root, 1200, 700);
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
     * Afficher une erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher un succès
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher une information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML private void handleGestionToggle() {
        gestionSubmenu.setVisible(!gestionSubmenu.isVisible());
        String arrow = gestionSubmenu.isVisible() ? "▼" : "▶";
        gestionToggle.setText("⚙️  Gestion " + arrow);
    }

    @FXML private void handleOperationsToggle() {
        operationsSubmenu.setVisible(!operationsSubmenu.isVisible());
        String arrow = operationsSubmenu.isVisible() ? "▼" : "▶";
        operationsToggle.setText("🚜  Opérations " + arrow);
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
    private void navigateTo(MouseEvent event,String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
        }
    }

}
