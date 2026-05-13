package controllers.User;
import javax.activation.*;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import models.User.Personne;
import models.User.Employe;
import models.User.Utilisateur;
import services.User.*;
import services.DatabaseBackupService;
import models.User.Abonnements;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import utils.SessionManager;
import services.User.SmsService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;


public class DashboardPersonnes {

    @FXML private ImageView avatarImageView;
    @FXML private Label     avatarDefaultLabel;
    @FXML private Circle avatarBg;
    @FXML private Label     userNameLabel;
    // sub menu
    @FXML private VBox gestionSubmenu, operationsSubmenu, gestionContainer;
    @FXML private Button gestionToggle, operationsToggle;
    @FXML private Button gestionBtn;

    // Boutons de filtre
    @FXML private Button filterAllBtn;
    @FXML private Button filterAgricoleBtn;
    @FXML private Button filterEmployeBtn;
    @FXML private Button filterAdminBtn;

    // Navigation
    @FXML private Button dashboardBtn;
    @FXML private Button tachesBtn;
    @FXML private Button logoutBtn;
    @FXML private Button addEmployeeBtn;
    @FXML private Label  userRoleLabel;
    //@FXML private Label  userNameLabel;
    @FXML private TextField searchField;

    @FXML private Button pdfBtn;
    @FXML private Label  selectedPersonLabel;
    private LogReportService logReportService;

    // Table
    @FXML private TableView<Personne>            employeeTable;
    @FXML private TableColumn<Personne, String>  nomColumn;
    @FXML private TableColumn<Personne, String>  emailColumn;
    @FXML private TableColumn<Personne, Integer> roleColumn;
    @FXML private TableColumn<Personne, String>  dateColumn;
    @FXML private TableColumn<Personne, Void>    actionsColumn;

    // Formulaire tâche
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private TextField        taskDescriptionField;
    @FXML private DatePicker       dueDatePicker;
    @FXML private Button           assignTaskBtn;

    private PersonneService   personneService;
    private AbonnementService abonnementService;
    private PdfReportService  pdfReportService;

    private ObservableList<Personne> employeeList;
    private ObservableList<Personne> allPersonsList;
    private Personne currentUser;           // ← champ de classe, JAMAIS redéclaré en local
    private Personne selectedPersonne;
    private String   currentFilter = "all";

    // ═══════════════════════════════════════════════════════════════════
    // INITIALISATION
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        logReportService = new LogReportService();

        // ✅ CORRECTION PRINCIPALE : récupérer le user depuis SessionManager dès initialize()
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerAvatarTopBar(SessionManager.getCurrentUser());

        // Mise à jour des labels
        updateUserLabels();

        // Sous-menu
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
        gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
        gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());

        try {
            personneService   = new PersonneService();
            abonnementService = new AbonnementService();
            pdfReportService  = new PdfReportService();

            setupTable();
            loadEmployees();
            setupSearch();

        } catch (Exception e) {
            showError("Erreur d'initialisation", "Impossible de charger le dashboard");
            e.printStackTrace();
        }
    }
    private void chargerAvatarTopBar(Personne user) {
        if (user == null) return;

        // Afficher le nom
        if (userNameLabel != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
        }

        // Charger la photo depuis l'URL Cloudinary dans un thread background
        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) {
            // Pas de photo → garder l'emoji par défaut, rien à faire
            return;
        }

        // Appliquer le clip circulaire en Java (ne fonctionne pas correctement en FXML)
        Circle clip = new Circle(24, 24, 24);
        avatarImageView.setClip(clip);

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 48, 48, false, true, true);

                Platform.runLater(() -> {
                    if (!image.isError()) {
                        avatarImageView.setImage(image);
                        avatarImageView.setVisible(true);
                        avatarImageView.setManaged(true);
                        avatarDefaultLabel.setVisible(false);
                        if (avatarBg != null) avatarBg.setVisible(false);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ═══════════════════════════════════════════════════════════════════
    // GESTION DU USER COURANT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Appelé depuis l'écran précédent pour passer le user.
     * Met aussi à jour SessionManager pour cohérence globale.
     */
    public void setCurrentUser(Personne user) {
        // ✅ CORRECTION : assigner le CHAMP de classe, pas une variable locale
        this.currentUser = user;

        if (user != null) {
            SessionManager.setCurrentUser(user); // synchroniser le SessionManager
            System.out.println("✓ setCurrentUser: " + user.getPrenom() + " " + user.getNom());
            updateUserLabels();
        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL");
        }
    }

    /**
     * Met à jour les labels nom/rôle dans la sidebar.
     */
    private void updateUserLabels() {
        if (currentUser == null) return;

        if (userNameLabel != null)
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        else
            System.err.println("✗ userNameLabel est NULL (non lié en FXML ?)");

        if (userRoleLabel != null) {
            String roleText = switch (currentUser.getRole()) {
                case 1 -> "🌾 AGRICOLE";
                case 2 -> "👷 EMPLOYÉ";
                case 3 -> "👑 ADMIN";
                default -> "Rôle inconnu";
            };
            userRoleLabel.setText(roleText);
        } else {
            System.err.println("✗ userRoleLabel est NULL (non lié en FXML ?)");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // PDF
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleExportPDF() {
        Personne selectedPerson = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedPerson == null) {
            showWarning("Aucune personne sélectionnée", "Veuillez sélectionner une personne dans le tableau.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Générer l'historique des connexions");
        confirm.setContentText("Voulez-vous générer le rapport PDF pour " +
                selectedPerson.getPrenom() + " " + selectedPerson.getNom() + " ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            generatePDF(selectedPerson);
        }
    }

    private void generatePDF(Personne personne) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");
            String defaultFileName = String.format("Historique_Connexions_%s_%s_%s.pdf",
                    personne.getNom().replace(" ", "_"),
                    personne.getPrenom().replace(" ", "_"),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            fileChooser.setInitialFileName(defaultFileName);
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF (*.pdf)", "*.pdf"));

            String userHome = System.getProperty("user.home");
            File initialDir = new File(userHome, "Documents");
            if (!initialDir.exists()) initialDir = new File(userHome, "Desktop");
            if (initialDir.exists()) fileChooser.setInitialDirectory(initialDir);

            File file = fileChooser.showSaveDialog(pdfBtn.getScene().getWindow());
            if (file != null) {
                showProgress("Génération du PDF en cours...");
                boolean success = logReportService.generateLogHistoryPDF(personne, file.getAbsolutePath());
                if (success) {
                    showSuccess("PDF généré avec succès", "Rapport enregistré à :\n" + file.getAbsolutePath());
                    Alert openAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    openAlert.setTitle("Ouvrir le fichier");
                    openAlert.setHeaderText("PDF généré avec succès");
                    openAlert.setContentText("Voulez-vous ouvrir le fichier maintenant ?");
                    Optional<ButtonType> openResult = openAlert.showAndWait();
                    if (openResult.isPresent() && openResult.get() == ButtonType.OK) openPDF(file);
                } else {
                    showError("Erreur", "Impossible de générer le PDF.");
                }
            }
        } catch (Exception e) {
            showError("Erreur PDF", e.getMessage());
            e.printStackTrace();
        }
    }

    private void openPDF(File file) {
        try {
            if (java.awt.Desktop.isDesktopSupported())
                java.awt.Desktop.getDesktop().open(file);
            else
                showWarning("Ouverture impossible", "Ouvrez manuellement :\n" + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Erreur ouverture", e.getMessage());
        }
    }

    @FXML
    private void handleEmailPDF() throws IOException {
        Personne selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("Sélection vide", "Veuillez sélectionner une personne."); return; }
        File tempFile = File.createTempFile("rapport_", ".pdf");
        logReportService.generateLogHistoryPDF(selected, tempFile.getAbsolutePath());
        EmailService.sendPDFAttachment(
                selected.getEmail(),
                "Votre historique de connexions",
                "Veuillez trouver en pièce jointe votre historique.",
                tempFile);
    }

    @FXML
    private void handleExportStats() {
        PdfReportService pdfService = new PdfReportService();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport");
        fileChooser.setInitialFileName("rapport_stats_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(pdfBtn.getScene().getWindow());
        if (file != null) {
            try {
                pdfService.generateStatistiquesPdf(file.getAbsolutePath());
                showSuccess("Rapport exporté !");
            } catch (Exception e) {
                showAlertSimple("Erreur : " + e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TABLE
    // ═══════════════════════════════════════════════════════════════════

    private void setupTable() {
        nomColumn.setCellValueFactory(cellData -> {
            Personne p = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(p.getPrenom() + " " + p.getNom());
        });
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setCellFactory(col -> new TableCell<Personne, Integer>() {
            @Override
            protected void updateItem(Integer role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) { setText(null); setStyle(""); return; }
                String txt; String color;
                switch (role) {
                    case 1: txt = "🌾 Agricole"; color = "#27AE60"; break;
                    case 2: txt = "👷 Employé";  color = "#F39C12"; break;
                    case 3: txt = "👑 Admin";    color = "#9B59B6"; break;
                    default: txt = "Inconnu";    color = "#95A5A6";
                }
                setText(txt);
                setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold;");
            }
        });
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_creationcpt"));
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn   = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox   hbox      = new HBox(8, editBtn, deleteBtn);
            {
                hbox.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color:#3498DB;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:5 12;-fx-cursor:hand;");
                deleteBtn.setStyle("-fx-background-color:#E74C3C;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:5 12;-fx-cursor:hand;");
                editBtn.setOnAction(e   -> handleEditEmployee(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteEmployee(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
        employeeTable.setRowFactory(tv -> {
            TableRow<Personne> row = new TableRow<>();
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                row.setStyle(isSelected ? "-fx-background-color: #e8f5e9;" : "");
            });
            return row;
        });
        employeeTable.setStyle("-fx-background-color:transparent;");
    }

    public void loadEmployees() {
        try {
            List<Personne> personnes = personneService.recuperer();
            allPersonsList = FXCollections.observableArrayList(personnes);
            employeeList   = FXCollections.observableArrayList(personnes);
            employeeTable.setItems(employeeList);
            ObservableList<String> names = FXCollections.observableArrayList();
            personnes.forEach(p -> names.add(p.getPrenom() + " " + p.getNom()));
            if (employeeComboBox != null) employeeComboBox.setItems(names);
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les personnes");
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // RECHERCHE & FILTRES
    // ═══════════════════════════════════════════════════════════════════

    private void setupSearch() {
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
    }

    @FXML private void handleFilterAll()      { currentFilter = "all";     applyFilters(); updateFilterButtonStyles(); }
    @FXML private void handleFilterAgricole() { currentFilter = "agricole"; applyFilters(); updateFilterButtonStyles(); }
    @FXML private void handleFilterEmploye()  { currentFilter = "employe";  applyFilters(); updateFilterButtonStyles(); }
    @FXML private void handleFilterAdmin()    { currentFilter = "admin";    applyFilters(); updateFilterButtonStyles(); }

    private void applyFilters() {
        if (allPersonsList == null) return;
        String search = searchField.getText().toLowerCase();
        ObservableList<Personne> filtered = FXCollections.observableArrayList();
        for (Personne p : allPersonsList) {
            boolean matchSearch = search.isEmpty()
                    || (p.getNom()    != null && p.getNom().toLowerCase().contains(search))
                    || (p.getPrenom() != null && p.getPrenom().toLowerCase().contains(search))
                    || (p.getEmail()  != null && p.getEmail().toLowerCase().contains(search));
            boolean matchRole = switch (currentFilter) {
                case "agricole" -> p.getRole() == 1;
                case "employe"  -> p.getRole() == 2;
                case "admin"    -> p.getRole() == 3;
                default         -> true;
            };
            if (matchSearch && matchRole) filtered.add(p);
        }
        employeeTable.setItems(filtered);
    }

    private void updateFilterButtonStyles() {
        String active     = "-fx-background-color:#3498DB;-fx-text-fill:white;-fx-font-size:13px;-fx-padding:8 15;-fx-background-radius:5;-fx-cursor:hand;";
        String inAll      = "-fx-background-color:transparent;-fx-border-color:#3498DB;-fx-border-width:2;-fx-text-fill:#3498DB;-fx-font-size:13px;-fx-padding:8 15;-fx-background-radius:5;-fx-cursor:hand;";
        String inAgricole = "-fx-background-color:transparent;-fx-border-color:#27AE60;-fx-border-width:2;-fx-text-fill:#27AE60;-fx-font-size:13px;-fx-padding:8 15;-fx-background-radius:5;-fx-cursor:hand;";
        String inEmploye  = "-fx-background-color:transparent;-fx-border-color:#F39C12;-fx-border-width:2;-fx-text-fill:#F39C12;-fx-font-size:13px;-fx-padding:8 15;-fx-background-radius:5;-fx-cursor:hand;";
        String inAdmin    = "-fx-background-color:transparent;-fx-border-color:#9B59B6;-fx-border-width:2;-fx-text-fill:#9B59B6;-fx-font-size:13px;-fx-padding:8 15;-fx-background-radius:5;-fx-cursor:hand;";
        filterAllBtn.setStyle(currentFilter.equals("all")      ? active : inAll);
        filterAgricoleBtn.setStyle(currentFilter.equals("agricole") ? active : inAgricole);
        filterEmployeBtn.setStyle(currentFilter.equals("employe")   ? active : inEmploye);
        filterAdminBtn.setStyle(currentFilter.equals("admin")       ? active : inAdmin);
    }

    // ═══════════════════════════════════════════════════════════════════
    // CRUD PERSONNES
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleAddEmployee() {
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
            stage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    private void handleEditEmployee(Personne personne) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ModifierPersonne.fxml"));
            Parent root = loader.load();
            ModifierPersonne controller = loader.getController();
            controller.setDashboardController(this);
            controller.setEmploye(personne);
            Stage stage = new Stage();
            stage.setTitle("Modifier l'Employé");
            stage.setScene(new Scene(root, 550, 650));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire de modification");
        }
    }

    private void handleDeleteEmployee(Personne personne) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la personne");
        alert.setContentText("Voulez-vous vraiment supprimer "
                + personne.getPrenom() + " " + personne.getNom() + " ?");
        alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                personneService.supprimer(personne.getCin());
                loadEmployees();
                applyFilters();
                showSuccess("Succès", "Personne supprimée avec succès");
            } catch (SQLException e) {
                showError("Erreur", "Impossible de supprimer la personne");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════
    // NAVIGATION  — utilise ActionEvent pour compatibilité FXML boutons
    // ═══════════════════════════════════════════════════════════════════

    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Transmettre currentUser au nouveau contrôleur s'il le supporte
            Object ctrl = loader.getController();
            try {
                ctrl.getClass().getMethod("setCurrentUser", Personne.class)
                        .invoke(ctrl, this.currentUser);
            } catch (NoSuchMethodException ignored) {
                // Le contrôleur destination n'a pas de setCurrentUser, rien à faire
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            showError("Erreur navigation", "Impossible de charger : " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML private void handleDashboard(MouseEvent e)    { navigateTo(e, "/UsersInterface/Acceuil.fxml",             "Accueil - AgroFlow"); }
    @FXML private void handlePersonnes(MouseEvent e)    { navigateTo(e, "/UsersInterface/Acceuil.fxml",             "Personnes - AgroFlow"); }
    @FXML private void handleTaches(MouseEvent e)       { navigateTo(e, "/UsersInterface/GestionTache.fxml",        "Tâches - AgroFlow"); }
    @FXML private void handleAbonnements(MouseEvent e)  { navigateTo(e, "/UsersInterface/GestionAbonnements.fxml",  "Abonnements - AgroFlow"); }
    @FXML private void handleOffres(MouseEvent e)       { navigateTo(e, "/UsersInterface/GestionOffre.fxml",        "Offres - AgroFlow"); }
    @FXML private void handleAnimals(MouseEvent e)      { navigateTo(e, "/AnimalsInterface/AfficherAnimaux.fxml",   "Animaux - AgroFlow"); }
    @FXML private void handleStocks(MouseEvent e)       { navigateTo(e, "/StocksInterface/afficherarticle.fxml",    "Stocks - AgroFlow"); }
    @FXML private void handleTerrains(MouseEvent e)     { navigateTo(e, "/TerrainsInterface/acceuilterrain.fxml",   "Terrains - AgroFlow"); }
    @FXML private void handleEvents(MouseEvent e)       { navigateTo(e, "/G-Evenements/Accueil.fxml",               "Événements - AgroFlow"); }
    @FXML private void handleMateriels(MouseEvent e)    { navigateTo(e, "/MaterielsInterface/AccueilMateriel.fxml", "Matériels - AgroFlow"); }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");
        alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            SessionManager.setCurrentUser(null); // ✅ vider la session
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                // On récupère le Stage et la Scene ACTUELLE
                Scene scene = stage.getScene();

                // SOLUTION MIRACLE : On change la racine, pas la scène !
                scene.setRoot(root);

                // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                stage.show();
            } catch (IOException e) {
                showError("Erreur", "Impossible de retourner à la connexion");
            }
        });
    }

    @FXML
    private void openDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/StatsDashboard.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Statistiques AgroFlow");
        stage.setScene(new Scene(root));
        stage.show();
    }

    // ═══════════════════════════════════════════════════════════════════
    // SOUS-MENUS
    // ═══════════════════════════════════════════════════════════════════

    @FXML private void handleGestionToggle() {
        gestionSubmenu.setVisible(!gestionSubmenu.isVisible());
        gestionToggle.setText(gestionSubmenu.isVisible() ? "⚙️  Gestion ▼" : "⚙️  Gestion ▶");
    }
    @FXML private void handleOperationsToggle() {
        operationsSubmenu.setVisible(!operationsSubmenu.isVisible());
        operationsToggle.setText(operationsSubmenu.isVisible() ? "🚜  Opérations ▼" : "🚜  Opérations ▶");
    }
    @FXML private void handleGestion() { /* Vue principale Gestion */ }
    private void showGestionSubmenu() { gestionSubmenu.setVisible(true);  gestionSubmenu.setManaged(true);  }
    private void hideGestionSubmenu() { gestionSubmenu.setVisible(false); gestionSubmenu.setManaged(false); }

    // ═══════════════════════════════════════════════════════════════════
    // TÂCHE
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleAssignTask() {
        String employee    = employeeComboBox.getValue();
        String description = taskDescriptionField.getText();
        LocalDate dueDate  = dueDatePicker.getValue();
        if (employee == null || description.isEmpty() || dueDate == null) {
            showError("Erreur", "Veuillez remplir tous les champs");
            return;
        }
        showSuccess("Succès", "Tâche assignée à " + employee);
        employeeComboBox.setValue(null);
        taskDescriptionField.clear();
        dueDatePicker.setValue(null);
    }

    public void handleAddTask(ActionEvent e) {}
    public void handleRefresh(ActionEvent e) { loadEmployees(); }

    // ═══════════════════════════════════════════════════════════════════
    // DATABASE BACKUP
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleBackup(MouseEvent event) {
        // Vérification des droits (Seul l'admin peut faire une sauvegarde)
        if (currentUser == null || currentUser.getRole() != 3) {
            showError("Accès refusé", "Seuls les administrateurs peuvent effectuer une sauvegarde de la base de données.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la sauvegarde de la base de données");
        
        // Nom de fichier par défaut avec horodatage
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName("agro_backup_" + timestamp + ".sql");
        
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers SQL (*.sql)", "*.sql"));

        // Dossier par défaut : Documents ou Bureau
        String userHome = System.getProperty("user.home");
        File initialDir = new File(userHome, "Documents");
        if (!initialDir.exists()) initialDir = new File(userHome, "Desktop");
        fileChooser.setInitialDirectory(initialDir);

        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            showProgress("Sauvegarde de la base de données en cours...");
            
            DatabaseBackupService backupService = new DatabaseBackupService();
            
            // On lance la sauvegarde dans un thread séparé pour ne pas bloquer l'UI
            new Thread(() -> {
                boolean success = backupService.backup(file.getAbsolutePath());
                
                Platform.runLater(() -> {
                    if (success) {
                        showSuccess("Sauvegarde terminée", "La base de données a été sauvegardée avec succès dans :\n" + file.getAbsolutePath());
                    } else {
                        showError("Erreur de sauvegarde", "Une erreur est survenue lors de la sauvegarde. Vérifiez que MySQL est bien lancé.");
                    }
                });
            }).start();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 2FA
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleNavigateToSettings2FA(MouseEvent event) {
        try {
            // ✅ Utiliser this.currentUser (déjà chargé) plutôt que SessionManager ici
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Session expirée.");
                return;
            }
            String telephone = currentUser.getTel();

            if (telephone == null || telephone.isEmpty()) {
                navigateToSettings2FA(event);
                return;
            }

            String otp = String.format("%06d", (int)(Math.random() * 999999));
            SessionManager.setTempOtp(otp);
            SmsService smsService = new SmsService();
            boolean sent = smsService.sendOtpCode(telephone, otp);

            if (!sent) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'envoyer le SMS.");
                return;
            }

            String masked = telephone.length() > 4
                    ? telephone.substring(0, telephone.length() - 4).replaceAll("\\d", "*")
                    + telephone.substring(telephone.length() - 4)
                    : telephone;

            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("🔐 Vérification SMS");
            dialog.setHeaderText("Code envoyé au : " + masked);

            ButtonType verifyBtn = new ButtonType("Vérifier", ButtonBar.ButtonData.OK_DONE);
            ButtonType resendBtn = new ButtonType("Renvoyer", ButtonBar.ButtonData.LEFT);
            dialog.getDialogPane().getButtonTypes().addAll(verifyBtn, resendBtn, ButtonType.CANCEL);

            TextField codeField = new TextField();
            codeField.setPromptText("000000");
            codeField.setMaxWidth(200);
            codeField.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 24px;" +
                    "-fx-alignment: center; -fx-pref-height: 52px;" +
                    "-fx-border-color: #52B788; -fx-border-radius: 8;" +
                    "-fx-background-radius: 8; -fx-border-width: 2;");
            codeField.textProperty().addListener((obs, o, n) -> {
                if (!n.matches("\\d*")) codeField.setText(n.replaceAll("[^\\d]", ""));
                if (n.length() > 6)     codeField.setText(n.substring(0, 6));
            });

            VBox content = new VBox(14);
            content.setAlignment(javafx.geometry.Pos.CENTER);
            content.getChildren().addAll(
                    new Label("📱 Entrez le code reçu par SMS :"),
                    codeField,
                    new Label("⏱  Valable 5 minutes"));
            dialog.getDialogPane().setContent(content);

            final String[] currentOtp = { otp };
            dialog.setResultConverter(btn -> {
                if (btn == resendBtn) {
                    String newOtp = String.format("%06d", (int)(Math.random() * 999999));
                    currentOtp[0] = newOtp;
                    SessionManager.setTempOtp(newOtp);
                    boolean reSent = smsService.sendOtpCode(telephone, newOtp);
                    Alert info = new Alert(reSent ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                    info.setTitle(reSent ? "SMS renvoyé" : "Erreur");
                    info.setHeaderText(null);
                    info.setContentText(reSent ? "Nouveau code envoyé au " + masked : "Échec envoi SMS.");
                    info.showAndWait();
                    return null;
                }
                if (btn == verifyBtn) return codeField.getText();
                return null;
            });

            dialog.showAndWait().ifPresent(code -> {
                if (code.equals(currentOtp[0])) {
                    SessionManager.clearTempOtp();
                    navigateToSettings2FA(event);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Code incorrect", "Code SMS invalide. Accès refusé.");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToSettings2FA(MouseEvent event) {
        Settings2FA settings = new Settings2FA();
        settings.launch();
    }

    // ═══════════════════════════════════════════════════════════════════
    // MON PROFIL
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleMonProfil() {
        System.out.println("👤 Ouverture Mon Profil...");

        // ✅ Toujours relire depuis SessionManager en cas de doute
        if (currentUser == null) {
            currentUser = SessionManager.getCurrentUser();
        }

        if (currentUser == null) {
            showError("Erreur", "Session expirée. Veuillez vous reconnecter.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();
            ProfilEmploye controller = loader.getController();
            if (controller != null) {
                controller.setCurrentUser(currentUser);
                System.out.println("✓ Utilisateur passé au profil: " + currentUser.getNom());
            }
            Stage stage = new Stage();
            stage.setTitle("Mon Profil");
            stage.setScene(new Scene(root, 800, 700));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ALERTES
    // ═══════════════════════════════════════════════════════════════════

    private void showError(String title, String msg)   { alert(Alert.AlertType.ERROR,       title, msg); }
    private void showSuccess(String title, String msg) { alert(Alert.AlertType.INFORMATION, title, msg); }
    private void showInfo(String title, String msg)    { alert(Alert.AlertType.INFORMATION, title, msg); }
    private void showWarning(String title, String msg) { alert(Alert.AlertType.WARNING,     title, msg); }
    private void showSuccess(String msg)               { alert(Alert.AlertType.INFORMATION, "Succès", msg); }
    private void showAlertSimple(String msg)           { alert(Alert.AlertType.ERROR,       "Erreur", msg); }

    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        alert(type, title, content);
    }

    private void showProgress(String message) {
        Alert progress = new Alert(Alert.AlertType.INFORMATION);
        progress.setTitle("En cours...");
        progress.setHeaderText(null);
        progress.setContentText(message);
        progress.show();
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(progress::close);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}