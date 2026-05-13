package controllers.Events;

import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Events.Evenement;
import models.User.Personne;
import services.Events.CategorieEvenementService;
import services.Events.EvenementService;
import services.Events.CalendarViewService;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AfficherEvenementsController {
//img user
@FXML private ImageView avatarImageView;
    @FXML private Label     avatarDefaultLabel;
    @FXML private Circle avatarBg;
    @FXML private Label     userNameLabel;
    @FXML private Label userRoleLabel;
    private Personne currentUser ;
    @FXML private Button logoutBtn, gestionBtn;
    @FXML private VBox gestionSubmenu, gestionContainer;

    @FXML private TableView<Evenement> eventsTable;
    @FXML private TableColumn<Evenement, String> titreColumn;
    @FXML private TableColumn<Evenement, String> typeColumn;
    @FXML private TableColumn<Evenement, Date> dateDebutColumn;
    @FXML private TableColumn<Evenement, Date> dateFinColumn;
    @FXML private TableColumn<Evenement, String> lieuColumn;
    @FXML private TableColumn<Evenement, String> categorieColumn;
    @FXML private TableColumn<Evenement, String> statutColumn;
    @FXML private TableColumn<Evenement, Void> actionsColumn;

    // Filtres ← issus de la v1 améliorée
    @FXML private TextField searchField;
    @FXML private DatePicker filterDateDebut;
    @FXML private DatePicker filterDateFin;
    @FXML private TextField filterLieu;
    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<String> filterCategorie;
    @FXML private Label resultsCountLabel;

    private final EvenementService evenementService = new EvenementService();
    private final CategorieEvenementService categorieService = new CategorieEvenementService();

    private ObservableList<Evenement> evenements;
    private FilteredList<Evenement> filteredData;
    private final Map<String, Integer> categoriesMap = new HashMap<>();

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerAvatarTopBar(SessionManager.getCurrentUser());

        // Mise à jour des labels
        updateUserLabels();
        // Cacher submenu par défaut
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);

        // Hover sur le bouton Gestion → Ouvre submenu
        gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());

        // Hover sur TOUT le container Gestion → Garde submenu ouvert
        gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());

        initColumns();
        initFilters();

        try {
            loadEvenements();
            setupReactiveSearch();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les événements : " + e.getMessage());
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

    // ================= INITIALISATION DES FILTRES =================
    private void initFilters() {
        filterStatut.getItems().addAll("Tous les statuts", "Planifié", "Annulé", "Terminé");
        filterStatut.setValue("Tous les statuts");

        try {
            filterCategorie.getItems().add("Toutes");
            categorieService.recuperer().forEach(cat -> {
                String nom = cat.getNom_categorie();
                filterCategorie.getItems().add(nom);
                categoriesMap.put(nom, cat.getId_categorie());
            });
            filterCategorie.setValue("Toutes");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        filterStatut.setOnAction(e -> appliquerFiltres());
        filterCategorie.setOnAction(e -> appliquerFiltres());
        filterDateDebut.setOnAction(e -> appliquerFiltres());
        filterDateFin.setOnAction(e -> appliquerFiltres());
        filterLieu.textProperty().addListener((obs, old, newVal) -> appliquerFiltres());
    }

    // ================= TABLE COLUMNS =================
    private void initColumns() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeEvenement"));
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Formatage des dates
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateDebutColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : formatter.format(date.toLocalDate()));
            }
        });

        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        dateFinColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                setText(empty || date == null ? null : formatter.format(date.toLocalDate()));
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
        statutColumn.setCellFactory(col -> new TableCell<>() {
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
                            setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-background-radius: 4;");
                            break;
                        case "terminé":
                            setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2; -fx-font-weight: bold; -fx-background-radius: 4;");
                            break;
                        case "annulé":
                            setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-weight: bold; -fx-background-radius: 4;");
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
        filteredData = new FilteredList<>(evenements, e -> true);
        eventsTable.setItems(filteredData);
        updateResultsCount();
        System.out.println("✅ " + evenements.size() + " événements chargés");
    }

    // ================= RECHERCHE RÉACTIVE =================
    private void setupReactiveSearch() {
        searchField.textProperty().addListener((obs, old, newVal) -> appliquerFiltres());
    }

    // ================= APPLIQUER TOUS LES FILTRES =================
    private void appliquerFiltres() {
        filteredData.setPredicate(evenement ->
                matchesSearchText(evenement)
                        && matchesDateDebutFilter(evenement)
                        && matchesDateFinFilter(evenement)
                        && matchesLieuFilter(evenement)
                        && matchesStatutFilter(evenement)
                        && matchesCategorieFilter(evenement)
        );
        updateResultsCount();
    }

    private boolean matchesSearchText(Evenement evenement) {
        String searchText = searchField.getText();
        if (searchText == null || searchText.trim().isEmpty()) return true;
        return evenement.getTitre().toLowerCase().contains(searchText.toLowerCase());
    }

    private boolean matchesDateDebutFilter(Evenement evenement) {
        LocalDate dateFiltre = filterDateDebut.getValue();
        if (dateFiltre == null) return true;
        return evenement.getDateDebut().toLocalDate().equals(dateFiltre);
    }

    private boolean matchesDateFinFilter(Evenement evenement) {
        LocalDate dateFiltre = filterDateFin.getValue();
        if (dateFiltre == null) return true;
        return evenement.getDateFin().toLocalDate().equals(dateFiltre);
    }

    private boolean matchesLieuFilter(Evenement evenement) {
        String lieuFiltre = filterLieu.getText();
        if (lieuFiltre == null || lieuFiltre.trim().isEmpty()) return true;
        return evenement.getLieu().toLowerCase().contains(lieuFiltre.toLowerCase());
    }

    private boolean matchesStatutFilter(Evenement evenement) {
        String statut = filterStatut.getValue();
        if (statut == null || statut.equals("Tous les statuts")) return true;
        return evenement.getStatut().equalsIgnoreCase(statut);
    }

    private boolean matchesCategorieFilter(Evenement evenement) {
        String categorie = filterCategorie.getValue();
        if (categorie == null || categorie.equals("Toutes")) return true;
        try {
            String nomCategorie = categorieService.getNomCategorieById(evenement.getIdCategorie());
            return nomCategorie.equals(categorie);
        } catch (SQLException e) {
            return true;
        }
    }

    // ================= RÉINITIALISER LES FILTRES =================
    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        filterDateDebut.setValue(null);
        filterDateFin.setValue(null);
        filterLieu.clear();
        filterStatut.setValue("Tous les statuts");
        filterCategorie.setValue("Toutes");
        appliquerFiltres();
    }

    private void updateResultsCount() {
        if (resultsCountLabel != null) {
            resultsCountLabel.setText(filteredData.size() + " événement(s) trouvé(s)");
        }
    }

    // ================= ACTION BUTTONS =================
    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn   = new Button("✏ Modifier");
            private final Button deleteBtn = new Button("🗑 Supprimer");
            private final HBox   box       = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color:#F39C12; -fx-text-fill:white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color:#E74C3C; -fx-text-fill:white; -fx-cursor: hand;");

                // ===== MODIFIER → ouvre un pop-up modal =====
                editBtn.setOnAction(e -> {
                    Evenement evenement = getTableView().getItems().get(getIndex());
                    ouvrirPopup("/G-Evenements/ModifierEvenement.fxml", evenement);
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
                            appliquerFiltres();
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

    // ================= ADD EVENEMENT → ouvre un pop-up modal =================
    @FXML
    private void handleAddEvenement(ActionEvent event) {
        ouvrirPopup("/G-Evenements/AjouterEvenement.fxml", null);
    }
    @FXML private void handleMonProfil(MouseEvent event )    { try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
        Parent root = loader.load();
        ProfilEmploye ctrl = loader.getController();
        if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
        Stage s = new Stage();
        s.setTitle("Mon Profil"); s.setScene(new Scene(root));
        s.setResizable(true); s.initModality(Modality.APPLICATION_MODAL);
        s.centerOnScreen(); s.showAndWait();
    } catch (IOException e) { showError("Erreur"+ e.getMessage(),"erreur"); } }
    // ================= REFRESH =================
    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadEvenements();
            handleResetFilters(event);
            showSuccess("Actualisation", "Liste actualisée avec succès !");
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'actualiser : " + e.getMessage());
        }
    }

    // ================= CALENDRIER =================
    @FXML
    private void handleOpenGoogleCalendar(ActionEvent event) {
        try {
            CalendarViewService calendarService = new CalendarViewService();
            Stage stage = (Stage) eventsTable.getScene().getWindow();
            calendarService.afficherCalendrier(stage);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le calendrier : " + e.getMessage());
        }
    }

    // ================= CARTE =================
    @FXML
    private void handleOpenCarte(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/G-Evenements/CarteEvenements.fxml");
            if (fxmlUrl == null) {
                showError("Erreur", "Fichier CarteEvenements.fxml introuvable.\nVérifiez qu'il est dans : resources/G-Evenements/");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("🗺️ Carte des événements — Tunisie");
            popupStage.setScene(new Scene(root, 600, 580));
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(eventsTable.getScene().getWindow());
            popupStage.setResizable(true);
            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la carte : " + e.getMessage());
        }
    }

    // ================= GÉNÉRATEUR D'IDÉES =================
    @FXML
    private void handleGenerateIdeas(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/G-Evenements/GenerateurIdees.fxml");
            if (fxmlUrl == null) fxmlUrl = getClass().getResource("GenerateurIdees.fxml");
            if (fxmlUrl == null) fxmlUrl = getClass().getClassLoader().getResource("G-Evenements/GenerateurIdees.fxml");
            if (fxmlUrl == null) fxmlUrl = getClass().getClassLoader().getResource("GenerateurIdees.fxml");

            if (fxmlUrl == null) {
                showError("Erreur - Fichier introuvable",
                        "GenerateurIdees.fxml est introuvable.\n\n"
                                + "Vérifiez que le fichier est bien dans :\n"
                                + "  src/main/resources/G-Evenements/GenerateurIdees.fxml\n\n"
                                + "Et que Maven/IntelliJ a bien copié les resources dans target.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("💡 Générateur d'idées d'événements IA");
            popupStage.setScene(new Scene(root, 680, 620));
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(eventsTable.getScene().getWindow());
            popupStage.setResizable(true);
            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le générateur d'idées : " + e.getMessage());
        }
    }

    // ================= POPUP MODAL =================
    private void ouvrirPopup(String fxml, Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource( fxml));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(eventsTable.getScene().getWindow());
            popupStage.setResizable(true);
            popupStage.setTitle(evenement == null ? "Nouvel Événement" : "Modifier l'Événement");
            popupStage.setScene(new Scene(root));

            if (evenement != null) {
                ModifierEvenementController controller = loader.getController();
                controller.setEvenement(evenement);
            }

            popupStage.showAndWait(); // BLOQUANT : on reprend ici après fermeture du pop-up

            loadEvenements();
            appliquerFiltres();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ================= NAVIGATION =================
    @FXML
    private void goToAccueil(ActionEvent event) {
        ouvrirPage(event, "/G-Evenements/Accueil.fxml");
    }

    private void ouvrirPage(Event event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // On change la racine, pas la scène → la fenêtre ne bouge pas d'un pixel
            scene.setRoot(root);

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxml);
            e.printStackTrace();
        }
    }

    private void ouvrirPageSimple(String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/G-Evenements/" + fxml));
            Stage stage = (Stage) eventsTable.getScene().getWindow();
            Scene scene = stage.getScene();

            // On change la racine, pas la scène → la fenêtre ne bouge pas d'un pixel
            scene.setRoot(root);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    // ================= SIDEBAR HANDLERS =================
    @FXML
    private void handlePersonnes(Event event) {
        ouvrirPage(event, "/UsersInterface/DahboardPersonne.fxml");
    }

    @FXML
    private void handleTaches(Event event) {
        ouvrirPage(event, "/UsersInterface/GestionTache.fxml");
    }

    @FXML
    private void handleAbonnements(Event event) {
        ouvrirPage(event, "/UsersInterface/GestionAbonnements.fxml");
    }

    @FXML
    private void handleOffres(Event event) {
        ouvrirPage(event, "/UsersInterface/GestionOffre.fxml");
    }

    public void handleDashboard(MouseEvent actionEvent) {
        ouvrirPage(actionEvent, "/UsersInterface/Acceuil.fxml");
    }

    public void handleAnimals(Event mouseEvent) {
        ouvrirPage(mouseEvent, "/AnimalsInterface/AfficherAnimaux.fxml");
    }

    public void handleStocks(Event mouseEvent) {
        ouvrirPage(mouseEvent, "/StocksInterface/afficherarticle.fxml");
    }

    public void handleTerrains(Event mouseEvent) {
        ouvrirPage(mouseEvent, "/TerrainsInterface/acceuilterrain.fxml");
    }

    public void handleEvents(Event mouseEvent) {
        ouvrirPage(mouseEvent, "/G-Evenements/Accueil.fxml");
    }

    public void handleMateriels(Event mouseEvent) {
        ouvrirPage(mouseEvent, "/MaterielsInterface/AccueilMateriel.fxml");
    }

    // ================= SUBMENU HELPERS =================
    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    // ================= LOGOUT =================
    @FXML
    private void handleLogout() {
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
                Scene scene = stage.getScene();

                // SOLUTION MIRACLE : On change la racine, pas la scène !
                scene.setRoot(root);

                // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                stage.show();


                System.out.println("✓ Déconnexion réussie");
            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
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