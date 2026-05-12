package controllers.Events;

import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
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
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Events.CategorieEvenement;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static controllers.User.GestionAbonnements.showInfo;

public class AfficherEvenementsUserController {
    @FXML private Button dashboardBtn,logoutBtn;

    @FXML private Label welcomeNameLabel;
    @FXML private Hyperlink aproposLink;

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    private Personne currentUser;
    //image useer
    @FXML private ImageView sidebarAvatarImageView;
    @FXML private Label     sidebarAvatarDefault;
    @FXML private Circle sidebarAvatarBg;

    @FXML private TableView<Evenement> eventsTable;
    @FXML private TableColumn<Evenement, String> titreColumn;
    @FXML private TableColumn<Evenement, String> typeColumn;
    @FXML private TableColumn<Evenement, Date> dateDebutColumn;
    @FXML private TableColumn<Evenement, Date> dateFinColumn;
    @FXML private TableColumn<Evenement, String> lieuColumn;
    @FXML private TableColumn<Evenement, String> categorieColumn;
    @FXML private TableColumn<Evenement, String> statutColumn;
    @FXML private TableColumn<Evenement, Void> actionsColumn;

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

    // ID de l'utilisateur connecté — à injecter depuis la page de connexion
    private int idUtilisateur ; // à remplacer par la session utilisateur réelle

    public void setIdUtilisateur(int id) {
        this.idUtilisateur = id;
    }

    public void setUserName(String name) {
        if (userNameLabel != null) userNameLabel.setText(name);
    }

    // ================= INITIALIZATION =================
    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
            this.idUtilisateur = this.currentUser.getCin();
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerSidebarAvatar(SessionManager.getCurrentUser());

        initColumns();
        initFilters();
        try {
            loadEvenements();
            setupReactiveSearch();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les evenements : " + e.getMessage());
            eventsTable.setItems(FXCollections.observableArrayList());
            resultsCountLabel.setText("0 evenement(s) trouve(s)");
        }
    }

    // ================= FILTRES =================
    private void initFilters() {
        filterStatut.getItems().addAll("Tous les statuts", "Planifie", "Annule", "Termine");
        filterStatut.setValue("Tous les statuts");

        try {
            int userRole = currentUser != null ? currentUser.getRole() : 3;
            filterCategorie.getItems().add("Toutes");

            categorieService.recuperer().stream()
                    .filter(cat -> {
                        String desc = cat.getDescription_categorie();
                        if (desc == null) return false;
                        String d = desc.toLowerCase();
                        return switch (userRole) {
                            case 1 -> d.contains("agricole");
                            case 2 -> d.contains("employe") || d.contains("employé");
                            default -> true;
                        };
                    })
                    .forEach(cat -> filterCategorie.getItems().add(cat.getNom_categorie()));

            filterCategorie.setValue("Toutes");
        } catch (Exception e) {
            e.printStackTrace();
            filterCategorie.getItems().setAll("Toutes");
            filterCategorie.setValue("Toutes");
        }

        filterStatut.setOnAction(e -> appliquerFiltres());
        filterCategorie.setOnAction(e -> appliquerFiltres());
        filterDateDebut.setOnAction(e -> appliquerFiltres());
        filterDateFin.setOnAction(e -> appliquerFiltres());
        filterLieu.textProperty().addListener((obs, old, nv) -> appliquerFiltres());
    }

    // ================= COLONNES =================
    private void initColumns() {
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeEvenement"));
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateDebutColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override protected void updateItem(Date d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : fmt.format(d.toLocalDate()));
            }
        });

        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        dateFinColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override protected void updateItem(Date d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : fmt.format(d.toLocalDate()));
            }
        });

        categorieColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(
                        categorieService.getNomCategorieById(cellData.getValue().getIdCategorie()));
            } catch (SQLException e) {
                return new SimpleStringProperty("Erreur");
            }
        });

        // Statut avec couleurs
        statutColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setText(null); setStyle(""); return; }
                setText(statut);
                switch (statut.toLowerCase()) {
                    case "planifie": case "planifié":
                        setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-background-radius: 4;"); break;
                    case "termine": case "terminé":
                        setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2; -fx-font-weight: bold; -fx-background-radius: 4;"); break;
                    case "annule": case "annulé":
                        setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-weight: bold; -fx-background-radius: 4;"); break;
                    default: setStyle("");
                }
            }
        });

        // Colonne Action : bouton "S'inscrire" uniquement
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button inscrireBtn = new Button("S'inscrire");

            {
                inscrireBtn.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6; -fx-font-weight: bold;");

                inscrireBtn.setOnAction(e -> {
                    Evenement evenement = getTableView().getItems().get(getIndex());
                    // Bloquer l'inscription si evenement annulé ou terminé
                    String statut = evenement.getStatut() != null ? evenement.getStatut().toLowerCase() : "";
                    if (statut.equals("annulé") || statut.equals("annule") || statut.equals("terminé") || statut.equals("termine")) {
                        showWarning("Inscription impossible",
                                "Vous ne pouvez pas vous inscrire a un evenement " + evenement.getStatut() + ".");
                        return;
                    }
                    ouvrirInscription(evenement);
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : inscrireBtn);
            }
        });
    }

    // ================= CHARGEMENT =================
    private void loadEvenements() throws SQLException {
        List<Evenement> filtered = getEvenementsForCurrentUser();
        evenements = FXCollections.observableArrayList(filtered);
        filteredData = new FilteredList<>(evenements, e -> true);
        eventsTable.setItems(filteredData);
        updateResultsCount();
    }

    private List<Evenement> getEvenementsForCurrentUser() throws SQLException {
        int userRole = currentUser != null ? currentUser.getRole() : 3;

        Set<Integer> allowedCategoryIds = categorieService.recuperer().stream()
                .filter(cat -> {
                    String desc = cat.getDescription_categorie();
                    if (desc == null) return false;
                    String d = desc.toLowerCase();
                    return switch (userRole) {
                        case 1 -> d.contains("agricole");
                        case 2 -> d.contains("employe") || d.contains("employé");
                        default -> true; // admin (role=3) sees everything
                    };
                })
                .map(CategorieEvenement::getId_categorie)
                .collect(Collectors.toSet());

        System.out.println("✓ Role: " + userRole + " | Allowed category IDs: " + allowedCategoryIds);

        return evenementService.recuperer().stream()
                .filter(ev -> allowedCategoryIds.contains(ev.getIdCategorie()))
                .collect(Collectors.toList());
    }

    private void setupReactiveSearch() {
        searchField.textProperty().addListener((obs, old, nv) -> appliquerFiltres());
    }

    // ================= FILTRES =================
    private void appliquerFiltres() {
        filteredData.setPredicate(ev ->
                matchesSearch(ev) && matchesDateDebut(ev) && matchesDateFin(ev)
                        && matchesLieu(ev) && matchesStatut(ev) && matchesCategorie(ev)
        );
        updateResultsCount();
    }

    private boolean matchesSearch(Evenement ev) {
        String t = searchField.getText();
        return t == null || t.trim().isEmpty() || ev.getTitre().toLowerCase().contains(t.toLowerCase());
    }
    private boolean matchesDateDebut(Evenement ev) {
        LocalDate f = filterDateDebut.getValue();
        return f == null || ev.getDateDebut().toLocalDate().equals(f);
    }
    private boolean matchesDateFin(Evenement ev) {
        LocalDate f = filterDateFin.getValue();
        return f == null || ev.getDateFin().toLocalDate().equals(f);
    }
    private boolean matchesLieu(Evenement ev) {
        String l = filterLieu.getText();
        return l == null || l.trim().isEmpty() || ev.getLieu().toLowerCase().contains(l.toLowerCase());
    }
    private boolean matchesStatut(Evenement ev) {
        String s = filterStatut.getValue();
        return s == null || s.equals("Tous les statuts") || ev.getStatut().equalsIgnoreCase(s);
    }
    private boolean matchesCategorie(Evenement ev) {
        String c = filterCategorie.getValue();
        if (c == null || c.equals("Toutes")) return true;
        try {
            return categorieService.getNomCategorieById(ev.getIdCategorie()).equals(c);
        } catch (SQLException e) { return true; }
    }

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
        resultsCountLabel.setText(filteredData.size() + " evenement(s) trouve(s)");
    }

    // ================= POP-UP INSCRIPTION =================
    private void ouvrirInscription(Evenement evenement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/SInscrireEvenement.fxml"));
            Parent root = loader.load();

            SInscrireEvenementController controller = loader.getController();
            controller.setEvenement(evenement);
            controller.setIdUtilisateur(idUtilisateur);

            Stage popup = new Stage();
            popup.initModality(Modality.WINDOW_MODAL);
            popup.initOwner(eventsTable.getScene().getWindow());
            popup.setResizable(false);
            popup.setTitle("S'inscrire a l'evenement");
            popup.setScene(new Scene(root));
            popup.showAndWait();

            // Pas besoin de recharger les evenements ici (lecture seule)

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }


    // ================= OUTILS =================
    @FXML
    private void handleOpenGoogleCalendar(ActionEvent event) {
        try {
            CalendarViewService calendarService = new CalendarViewService();
            Stage stage = (Stage) eventsTable.getScene().getWindow();
            calendarService.afficherCalendrier(stage);
        } catch (Exception e) {
            showError("Erreur", "Impossible d'ouvrir le calendrier : " + e.getMessage());
        }
    }

    @FXML
    private void handleOpenCarte(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/G-Evenements/CarteEvenements.fxml");
            if (fxmlUrl == null) { showError("Erreur", "CarteEvenements.fxml introuvable."); return; }
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage popup = new Stage();
            popup.setTitle("Carte des evenements");
            popup.setScene(new Scene(root, 600, 580));
            popup.initModality(Modality.WINDOW_MODAL);
            popup.initOwner(eventsTable.getScene().getWindow());
            popup.setResizable(true);
            popup.show();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la carte : " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadEvenements();
            handleResetFilters(event);
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'actualiser : " + e.getMessage());
        }
    }

    // ================= NAVIGATION =================
    @FXML
    private void goToEvenements(ActionEvent event) {
        // Deja sur cette page
    }

    @FXML
    private void goToMesParticipations(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/AfficherParticipationsUser.fxml"));
            Parent root = loader.load();
            AfficherParticipationsUserController controller = loader.getController();
            controller.setIdUtilisateur(idUtilisateur);
            Stage stage = (Stage) eventsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    // ================= ALERTS =================
    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }

    //navigation side bar agricole
    // navigation Front Office Agricole
    private void chargerSidebarAvatar(Personne user) {
        if (user == null) return;

        // Nom et rôle
        if (userNameLabel != null)
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());

        // Clip circulaire appliqué en Java (radius=35, centre=35,35 pour fitWidth/Height=70)
        if (sidebarAvatarImageView != null) {
            Circle clip = new Circle(35, 35, 35);
            sidebarAvatarImageView.setClip(clip);
        }

        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) return;

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 70, 70, false, true, true);
                Platform.runLater(() -> {
                    if (!image.isError()) {
                        sidebarAvatarImageView.setImage(image);
                        sidebarAvatarImageView.setVisible(true);
                        sidebarAvatarImageView.setManaged(true);
                        sidebarAvatarDefault.setVisible(false);
                        if (sidebarAvatarBg != null) sidebarAvatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("⚠️ Avatar sidebar : " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    @FXML
    void ouvrirTerrains(MouseEvent event) {
        chargerPage(event, "/TerrainsInterface/agricoleaffichageterrain.fxml", "Gestion des Terrains");
    }

    @FXML
    void ouvrirPlantes(MouseEvent event) {
        chargerPage(event, "/TerrainsInterface/agricoleaffichageplante.fxml", "Liste des Plantes");
    }

    @FXML
    void ouvrirRotations(MouseEvent event) {
        chargerPage(event, "/TerrainsInterface/agricoleaffichagerotation.fxml", "Gestion des Rotations");
    }

    private void chargerPage(MouseEvent event, String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));
            stage.setTitle(titre);
            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
        }
    }
    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleDashboardAgricole(MouseEvent event)    { navigateTo(event,"/UsersInterface/AcceuillAgr.fxml","Dashboard"); }
    @FXML private void handleMesTerrains(MouseEvent mouseEvent)  {         navigateTo(mouseEvent,"/TerrainsInterface/acceuilagricoleterrain.fxml","Terrains");
    }
    @FXML private void handleMesAnimaux(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml","Animaux");
    }
    @FXML private void handleMesArticles(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherArticleAgr.fxml","Articles"); }
    @FXML private void handleMesCatégories(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherCategorieAgr.fxml","Catégories "); }
    @FXML private void handleMonMateriel(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMachine.fxml","Animaux"); }
    @FXML private void handleMonProfil(MouseEvent event )    { try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
        Parent root = loader.load();
        ProfilEmploye ctrl = loader.getController();
        if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
        Stage s = new Stage();
        s.setTitle("Mon Profil"); s.setScene(new Scene(root));
        s.setResizable(true); s.initModality(Modality.APPLICATION_MODAL);
        s.centerOnScreen(); s.showAndWait();
    } catch (IOException e) { showError("Erreur"+ e.getMessage()); } }

    // ✓ CORRECT
    @FXML
    private void handleMonAbonnement(MouseEvent event) {
        System.out.println("💳 Ouverture Mon Abonnement...");
        navigateTo(event,"/UsersInterface/MesAbonnements.fxml","Mes Abonnements");
    }





    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Ajouter cette méthode getStage() pour ProfilAgricole
    public Stage getStage() {
        if (logoutBtn != null && logoutBtn.getScene() != null)
            return (Stage) logoutBtn.getScene().getWindow();
        return null;
    }

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("✓ setCurrentUser appelé pour: " + user.getNom());

            if (userNameLabel != null)
                userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            else
                System.err.println("✗ userNameLabel est NULL !");

            if (welcomeNameLabel != null)
                welcomeNameLabel.setText(user.getPrenom() + " !");
            else
                System.err.println("✗ welcomeNameLabel est NULL !");

            if (userRoleLabel != null)
                userRoleLabel.setText("🌾 AGRICULTEUR");


        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL !");
        }
    }



    /**
     * Transfère l'utilisateur courant au contrôleur cible via réflexion
     */
    private void transferUserToController(Object controller) {
        try {
            controller.getClass()
                    .getMethod("setCurrentUser", Personne.class)
                    .invoke(controller, currentUser);
            System.out.println("✓ Utilisateur transféré au contrôleur");
        } catch (NoSuchMethodException e) {
            System.out.println("ℹ Le contrôleur n'a pas de méthode setCurrentUser()");
        } catch (Exception e) {
            System.err.println("✗ Erreur lors du transfert utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gère les erreurs de navigation de manière appropriée
     */
    private void handleNavigationError(String fxmlPath, String title, IOException e) {
        e.printStackTrace();

        // Vérifier si c'est un fichier manquant ou une autre erreur
        if (e.getMessage() != null && e.getMessage().contains("Location is not set")) {
            showInfo("Module à venir",
                    "Le module \"" + title + "\" sera disponible prochainement.");
        } else if (fxmlPath.contains("MesTerrains") ||
                fxmlPath.contains("MesAnimaux") ||
                fxmlPath.contains("MesStocks") ||
                fxmlPath.contains("MonMateriel")) {
            // Modules pas encore implémentés
            showInfo("Fonctionnalité à venir",
                    "Cette fonctionnalité est en cours de développement.");
        } else {
            // Erreur réelle
            showError("Erreur de chargement\n\n" +
                    "Impossible de charger " + title + ".\n" +
                    "Détails: " + e.getMessage());
        }


    }
    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
        }
    }



    public void handleMesEvenements(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherEvenmentsUser.fxml","G-Evenements");
    }

    public void ouvrirParticipations(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherParticipationsUser.fxml","G-Participations");

    }

    public void handleLogout(ActionEvent actionEvent) {
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
                // On récupère le Stage et la Scene ACTUELLE
                Scene scene = stage.getScene();

                // SOLUTION MIRACLE : On change la racine, pas la scène !
                scene.setRoot(root);

                // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                stage.show();

                System.out.println("✓ Déconnexion réussie");

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }}
    }
}