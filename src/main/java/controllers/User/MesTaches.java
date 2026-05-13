package controllers.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Personne;
import models.User.Tache;
import services.User.TacheService;
import utils.SessionManager;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la vue "Mes Tâches" des employés
 * - Recherche filtrée et triée
 * - Statistiques d'états
 * - Génération de rapport (tâche sélectionnée ou toutes)
 */
public class MesTaches {

    // ══════════════════════════════════════════════════════════════
    // FXML Components
    // ══════════════════════════════════════════════════════════════

    @FXML private VBox gestionSubmenu;
    @FXML private VBox gestionContainer;
    @FXML private Button gestionBtn;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private TextField searchField;
    @FXML private Button refreshBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button logoutBtn;

    // Filtres et tri
    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<String> filterPriorite;
    @FXML private ComboBox<String> sortCombo;

    // Statistiques
    @FXML private Label statTotal;
    @FXML private Label statEnCours;
    @FXML private Label statTerminee;
    @FXML private Label statEnAttente;
    @FXML private Label statEnRetard;

    // Boutons rapport
    @FXML private Button rapportSelectionBtn;
    @FXML private Button rapportToutBtn;

    // Table et colonnes
    @FXML private TableView<Tache> taskTable;
    @FXML private TableColumn<Tache, Integer> idColumn;
    @FXML private TableColumn<Tache, String> titleColumn;
    @FXML private TableColumn<Tache, String> descriptionColumn;
    @FXML private TableColumn<Tache, String> statutColumn;
    @FXML private TableColumn<Tache, String> prioriteColumn;
    @FXML private TableColumn<Tache, String> dateColumn;
    @FXML private TableColumn<Tache, Void> actionsColumn;

    // ══════════════════════════════════════════════════════════════
    // Instance Variables
    // ══════════════════════════════════════════════════════════════

    private TacheService tacheService;
    private ObservableList<Tache> allTachesList = FXCollections.observableArrayList();
    private Personne currentUser;
    @FXML private ImageView avatarImageView;
    @FXML private Label     avatarDefaultLabel;
    @FXML private Circle avatarBg;
    // ══════════════════════════════════════════════════════════════
    // Initialization
    // ══════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        chargerAvatarSidebar(SessionManager.getCurrentUser());

        try {
            tacheService = new TacheService();
            System.out.println("✓ MesTaches Controller initialisé");

            setupFilters();
            setupSort();
            setupSearch();

            if (taskTable != null) {
                setupTable();
            }

        } catch (Exception e) {
            System.err.println("✗ Erreur initialisation MesTaches");
            e.printStackTrace();
            showError("Erreur d'initialisation",
                    "Impossible de charger le module Mes Tâches: " + e.getMessage());
        }
    }
    private void chargerAvatarSidebar(Personne user) {
        if (user == null) return;

        String photoUrl = user.getPhotoUrl();

        if (photoUrl == null || photoUrl.isBlank()
                || photoUrl.equals("0") || photoUrl.equals("null")) {
            // Pas de photo → emoji par défaut, rien à faire
            return;
        }

        // Clip circulaire appliqué en Java (pas possible en FXML)
        Circle clip = new Circle(32, 32, 32);
        avatarImageView.setClip(clip);

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 64, 64, false, true, true);
                Platform.runLater(() -> {
                    if (!image.isError()) {
                        avatarImageView.setImage(image);
                        avatarImageView.setVisible(true);
                        avatarImageView.setManaged(true);
                        avatarDefaultLabel.setVisible(false);
                        avatarDefaultLabel.setManaged(false);
                        if (avatarBg != null) avatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("⚠️ Erreur chargement avatar : " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    // ══════════════════════════════════════════════════════════════
    // User Management
    // ══════════════════════════════════════════════════════════════

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("✓ setCurrentUser appelé pour: " + user.getNom());

            if (userNameLabel != null)
                userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            if (userRoleLabel != null)
                userRoleLabel.setText(getRoleText(user.getRole()));

            loadMyTaches();
        }
    }

    private String getRoleText(int role) {
        switch (role) {
            case 1: return "🌾 AGRICULTEUR";
            case 2: return "👷 EMPLOYÉ";
            case 3: return "👨‍💼 ADMIN";
            default: return "👤 UTILISATEUR";
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Setup Filters & Sort
    // ══════════════════════════════════════════════════════════════

    private void setupFilters() {
        if (filterStatut != null) {
            filterStatut.setItems(FXCollections.observableArrayList(
                    "Tous", "En attente", "En cours", "Terminée", "En retard"
            ));
            filterStatut.setValue("Tous");
            filterStatut.setOnAction(e -> applyFiltersAndSort());
        }

        if (filterPriorite != null) {
            filterPriorite.setItems(FXCollections.observableArrayList(
                    "Toutes", "Basse", "Moyenne", "Haute", "Urgente"
            ));
            filterPriorite.setValue("Toutes");
            filterPriorite.setOnAction(e -> applyFiltersAndSort());
        }
    }

    private void setupSort() {
        if (sortCombo != null) {
            sortCombo.setItems(FXCollections.observableArrayList(
                    "Par défaut", "Titre (A→Z)", "Titre (Z→A)",
                    "Priorité (Urgente→Basse)", "Priorité (Basse→Urgente)",
                    "Date (Récente→Ancienne)", "Date (Ancienne→Récente)"
            ));
            sortCombo.setValue("Par défaut");
            sortCombo.setOnAction(e -> applyFiltersAndSort());
        }
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, newVal) -> applyFiltersAndSort());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Filter + Sort Logic (combiné)
    // ══════════════════════════════════════════════════════════════

    private void applyFiltersAndSort() {
        if (allTachesList == null || taskTable == null) return;

        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String statut = filterStatut != null ? filterStatut.getValue() : "Tous";
        String priorite = filterPriorite != null ? filterPriorite.getValue() : "Toutes";
        String sort = sortCombo != null ? sortCombo.getValue() : "Par défaut";

        // 1. Filtrer
        List<Tache> filtered = allTachesList.stream()
                .filter(t -> {
                    // Filtre texte
                    if (!searchText.isEmpty()) {
                        boolean matchSearch =
                                (t.getNomTache() != null && t.getNomTache().toLowerCase().contains(searchText)) ||
                                        (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchText)) ||
                                        (t.getEtat() != null && t.getEtat().toLowerCase().contains(searchText)) ||
                                        (t.getPriorite() != null && t.getPriorite().toLowerCase().contains(searchText)) ||
                                        String.valueOf(t.getId()).contains(searchText);
                        if (!matchSearch) return false;
                    }
                    // Filtre statut
                    if (statut != null && !statut.equals("Tous")) {
                        if (t.getEtat() == null || !t.getEtat().equalsIgnoreCase(statut)) return false;
                    }
                    // Filtre priorité
                    if (priorite != null && !priorite.equals("Toutes")) {
                        if (t.getPriorite() == null || !t.getPriorite().equalsIgnoreCase(priorite)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 2. Trier
        if (sort != null) {
            switch (sort) {
                case "Titre (A→Z)":
                    filtered.sort(Comparator.comparing(t -> t.getNomTache() != null ? t.getNomTache() : ""));
                    break;
                case "Titre (Z→A)":
                    filtered.sort((a, b) -> {
                        String na = a.getNomTache() != null ? a.getNomTache() : "";
                        String nb = b.getNomTache() != null ? b.getNomTache() : "";
                        return nb.compareTo(na);
                    });
                    break;
                case "Priorité (Urgente→Basse)":
                    filtered.sort(Comparator.comparingInt(t -> prioriteOrdre(t.getPriorite())));
                    break;
                case "Priorité (Basse→Urgente)":
                    filtered.sort((a, b) -> prioriteOrdre(b.getPriorite()) - prioriteOrdre(a.getPriorite()));
                    break;
                case "Date (Récente→Ancienne)":
                    filtered.sort((a, b) -> {
                        String da = a.getDateEcheance() != null ? a.getDateEcheance().toString() : "";
                        String db = b.getDateEcheance() != null ? b.getDateEcheance().toString() : "";
                        return db.compareTo(da);
                    });
                    break;
                case "Date (Ancienne→Récente)":
                    filtered.sort((a, b) -> {
                        String da = a.getDateEcheance() != null ? a.getDateEcheance().toString() : "";
                        String db = b.getDateEcheance() != null ? b.getDateEcheance().toString() : "";
                        return da.compareTo(db);
                    });
                    break;
                default:
                    break;
            }
        }

        taskTable.setItems(FXCollections.observableArrayList(filtered));
    }

    /** Ordre décroissant urgence: Urgente=0, Haute=1, Moyenne=2, Basse=3 */
    private int prioriteOrdre(String p) {
        if (p == null) return 99;
        switch (p.toLowerCase()) {
            case "urgente": return 0;
            case "haute":   return 1;
            case "moyenne": return 2;
            case "basse":   return 3;
            default:        return 99;
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Statistics
    // ══════════════════════════════════════════════════════════════

    private void updateStatistics() {
        if (allTachesList == null) return;

        int total = allTachesList.size();
        long enCours   = allTachesList.stream().filter(t -> "En cours".equalsIgnoreCase(t.getEtat())).count();
        long terminee  = allTachesList.stream().filter(t -> t.getEtat() != null && t.getEtat().toLowerCase().contains("termin")).count();
        long enAttente = allTachesList.stream().filter(t -> "En attente".equalsIgnoreCase(t.getEtat())).count();
        long enRetard  = allTachesList.stream().filter(t -> "En retard".equalsIgnoreCase(t.getEtat())).count();

        if (statTotal != null)     statTotal.setText(String.valueOf(total));
        if (statEnCours != null)   statEnCours.setText(String.valueOf(enCours));
        if (statTerminee != null)  statTerminee.setText(String.valueOf(terminee));
        if (statEnAttente != null) statEnAttente.setText(String.valueOf(enAttente));
        if (statEnRetard != null)  statEnRetard.setText(String.valueOf(enRetard));
    }

    // ══════════════════════════════════════════════════════════════
    // Table Setup
    // ══════════════════════════════════════════════════════════════

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_tache"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("nom_tache"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));
        prioriteColumn.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date_echeancee"));

        // Style statut
        statutColumn.setCellFactory(col -> new TableCell<Tache, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item.toLowerCase()) {
                    case "en cours":   setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;"); break;
                    case "terminée":
                    case "terminee":   setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;"); break;
                    case "en retard":  setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;"); break;
                    case "en attente": setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold;"); break;
                    default:           setStyle("-fx-text-fill: #95A5A6; -fx-font-weight: bold;");
                }
            }
        });

        // Style priorité
        prioriteColumn.setCellFactory(col -> new TableCell<Tache, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                switch (item.toLowerCase()) {
                    case "urgente": setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;"); break;
                    case "haute":   setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;"); break;
                    case "moyenne": setStyle("-fx-text-fill: #3498DB; -fx-font-weight: bold;"); break;
                    case "basse":   setStyle("-fx-text-fill: #95A5A6; -fx-font-weight: bold;"); break;
                    default:        setStyle("-fx-text-fill: #7F8C8D; -fx-font-weight: bold;");
                }
            }
        });

        // Colonne Actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn   = new Button("👁️ Voir");
            private final Button statusBtn = new Button("📝 Statut");
            private final HBox hbox = new HBox(5, viewBtn, statusBtn);

            {
                hbox.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px;");
                statusBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-padding: 4 10; -fx-cursor: hand; -fx-font-size: 11px;");

                viewBtn.setOnAction(e -> handleViewTask(getTableView().getItems().get(getIndex())));
                statusBtn.setOnAction(e -> handleChangeStatus(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // Data Loading
    // ══════════════════════════════════════════════════════════════

    private void loadMyTaches() {
        if (currentUser == null) {
            showError("Erreur", "Session expirée. Veuillez vous reconnecter.");
            return;
        }
        if (taskTable == null) return;

        try {
            List<Tache> taches = tacheService.recupererTachesParPersonne(currentUser.getCin());
            allTachesList = FXCollections.observableArrayList(taches);
            applyFiltersAndSort();
            updateStatistics();
            System.out.println("✓ " + taches.size() + " tâches chargées");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger vos tâches: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Task Actions
    // ══════════════════════════════════════════════════════════════

    private void handleViewTask(Tache tache) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la Tâche #" + tache.getId());
        alert.setHeaderText(tache.getNomTache());
        StringBuilder content = new StringBuilder();
        content.append("Description: ").append(tache.getDescription() != null ? tache.getDescription() : "N/A").append("\n\n");
        content.append("Statut: ").append(tache.getEtat() != null ? tache.getEtat() : "N/A").append("\n");
        content.append("Priorité: ").append(tache.getPriorite() != null ? tache.getPriorite() : "N/A").append("\n");
        content.append("Date d'échéance: ").append(tache.getDateEcheance() != null ? tache.getDateEcheance() : "N/A");
        alert.setContentText(content.toString());
        alert.getDialogPane().setMinWidth(500);
        alert.showAndWait();
    }

    private void handleChangeStatus(Tache tache) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                tache.getEtat(), "En attente", "En cours", "Terminée");
        dialog.setTitle("Changer le statut");
        dialog.setHeaderText("Tâche: " + tache.getNomTache());
        dialog.setContentText("Nouveau statut:");

        dialog.showAndWait().ifPresent(nouveauStatut -> {
            try {
                tache.setEtat(nouveauStatut);
                tacheService.modifier(tache);
                loadMyTaches();
                showSuccess("Succès", "Statut mis à jour avec succès");
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de modifier le statut: " + e.getMessage());
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // Rapport Generation
    // ══════════════════════════════════════════════════════════════

    /**
     * Génère un rapport pour la tâche sélectionnée dans le tableau
     */
    @FXML
    private void handleRapportSelection() {
        if (taskTable == null) return;
        Tache selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Aucune sélection", "Veuillez sélectionner une tâche dans le tableau.");
            return;
        }
        generateRapport(Collections.singletonList(selected), "rapport_tache_" + selected.getId());
    }

    /**
     * Génère un rapport pour toutes les tâches affichées (après filtres)
     */
    @FXML
    private void handleRapportTout() {
        if (taskTable == null || taskTable.getItems().isEmpty()) {
            showInfo("Aucune tâche", "Il n'y a aucune tâche à exporter.");
            return;
        }
        generateRapport(new ArrayList<>(taskTable.getItems()), "rapport_toutes_taches");
    }

    /**
     * Génère un fichier texte/CSV du rapport et propose de le sauvegarder
     */
    private void generateRapport(List<Tache> taches, String defaultName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le rapport");
        fileChooser.setInitialFileName(defaultName + ".txt");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichier texte", "*.txt"),
                new FileChooser.ExtensionFilter("CSV", "*.csv")
        );

        Stage stage = getStage();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), "UTF-8"))) {

            boolean isCsv = file.getName().endsWith(".csv");

            if (isCsv) {
                writer.println("ID,Titre,Description,Statut,Priorité,Échéance");
                for (Tache t : taches) {
                    writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                            t.getId(),
                            safe(t.getNomTache()),
                            safe(t.getDescription()),
                            safe(t.getEtat()),
                            safe(t.getPriorite()),
                            t.getDateEcheance() != null ? t.getDateEcheance().toString() : "N/A"
                    );
                }
            } else {
                // Entête rapport
                writer.println("══════════════════════════════════════════════════════");
                writer.println("               RAPPORT DE TÂCHES - AgroFlow");
                writer.println("══════════════════════════════════════════════════════");
                if (currentUser != null) {
                    writer.println("Employé   : " + currentUser.getPrenom() + " " + currentUser.getNom());
                    writer.println("CIN       : " + currentUser.getCin());
                }
                writer.println("Date      : " + new java.util.Date());
                writer.println("Nb tâches : " + taches.size());
                writer.println("──────────────────────────────────────────────────────");
                writer.println();

                // Statistiques rapides
                long enCours   = taches.stream().filter(t -> "En cours".equalsIgnoreCase(t.getEtat())).count();
                long terminee  = taches.stream().filter(t -> t.getEtat() != null && t.getEtat().toLowerCase().contains("termin")).count();
                long enAttente = taches.stream().filter(t -> "En attente".equalsIgnoreCase(t.getEtat())).count();
                long enRetard  = taches.stream().filter(t -> "En retard".equalsIgnoreCase(t.getEtat())).count();

                writer.println("RÉSUMÉ DES STATUTS :");
                writer.printf("  ✓ Terminées : %d   |  ▶ En cours : %d   |  ⏳ En attente : %d   |  ⚠ En retard : %d%n",
                        terminee, enCours, enAttente, enRetard);
                writer.println();
                writer.println("══════════════════════════════════════════════════════");
                writer.println();

                // Détail de chaque tâche
                int i = 1;
                for (Tache t : taches) {
                    writer.println("TÂCHE #" + i++ + " (ID: " + t.getId() + ")");
                    writer.println("  Titre       : " + safe(t.getNomTache()));
                    writer.println("  Description : " + safe(t.getDescription()));
                    writer.println("  Statut      : " + safe(t.getEtat()));
                    writer.println("  Priorité    : " + safe(t.getPriorite()));
                    writer.println("  Échéance    : " + (t.getDateEcheance() != null ? t.getDateEcheance() : "N/A"));
                    writer.println("──────────────────────────────────────────────────────");
                }
            }

            showSuccess("Rapport généré", "Le rapport a été sauvegardé avec succès :\n" + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de générer le rapport : " + e.getMessage());
        }
    }

    private String safe(String s) {
        return s != null ? s.replace("\"", "\"\"") : "N/A";
    }

    // ══════════════════════════════════════════════════════════════
    // Navigation Handlers
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleRefresh() {
        if (currentUser == null) {
            showError("Erreur", "Session expirée. Veuillez vous reconnecter.");
            return;
        }
        if (searchField != null) searchField.clear();
        if (filterStatut != null) filterStatut.setValue("Tous");
        if (filterPriorite != null) filterPriorite.setValue("Toutes");
        if (sortCombo != null) sortCombo.setValue("Par défaut");
        loadMyTaches();
    }

    @FXML
    private void handleDashboard(MouseEvent event) {
        if (currentUser == null) {
            showError("Erreur de session", "Votre session a expiré.\nVeuillez vous reconnecter.");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) dashboardBtn.getScene().getWindow();
                if (stage != null) {
                    // On récupère le Stage et la Scene ACTUELLE

                    Scene scene = stage.getScene();

                    // SOLUTION MIRACLE : On change la racine, pas la scène !
                    scene.setRoot(root);

                    // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                    stage.show();
                }
            } catch (IOException e) { e.printStackTrace(); }
            return;
        }
        navigateTo(event, "/UsersInterface/AcceuilEmp.fxml", "AgroFlow - Dashboard Employé");
    }

    @FXML
    private void handleMonProfil() {
        if (currentUser == null) {
            showError("Erreur", "Session expirée. Veuillez vous reconnecter.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();
            ProfilEmploye controller = loader.getController();
            if (controller != null) controller.setCurrentUser(currentUser);
            Stage stage = new Stage();
            stage.setTitle("Mon Profil - Employé");
            stage.setScene(new Scene(root, 600, 700));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
        }
    }

    @FXML
    private void handleRapports() {
        showInfo("À venir", "Le module Rapports avancés sera disponible prochainement.");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    currentUser = null;
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                    Parent root = loader.load();
                    Stage stage = getStage();
                    if (stage != null) {
                        stage.setScene(new Scene(root, 900, 600));
                        stage.setTitle("AgroFlow - Connexion");
                        stage.setMaximized(true);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Erreur", "Impossible de se déconnecter: " + e.getMessage());
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // Navigation Utility
    // ══════════════════════════════════════════════════════════════

    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            boolean etaitMaximise = stage.isMaximized();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(etaitMaximise);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
        }
    }

    private Stage getStage() {
        if (dashboardBtn != null && dashboardBtn.getScene() != null)
            return (Stage) dashboardBtn.getScene().getWindow();
        if (logoutBtn != null && logoutBtn.getScene() != null)
            return (Stage) logoutBtn.getScene().getWindow();
        if (taskTable != null && taskTable.getScene() != null)
            return (Stage) taskTable.getScene().getWindow();
        return null;
    }

    // ══════════════════════════════════════════════════════════════
    // Alert Helpers
    // ══════════════════════════════════════════════════════════════

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showSuccess(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    public void handleMateriel(MouseEvent mouseEvent) {
        navigateTo(mouseEvent, "/MaterielsInterface/MaintenanceFront.fxml", "EmployeMaintenance");

    }
    public void handleEvenements(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherEvenementsEmp.fxml", "EvenementsAfficher");
    }


    public void ouvrirRotations(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/TerrainsInterface/EmployeRotation.fxml","Rotations Employee ");
    }

    public void handleStocks(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/StocksInterface/AfficherStockOuvrier.fxml","stocks");
    }
    }
