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
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Events.Participation;
import models.User.Personne;
import services.Events.EvenementService;
import services.Events.ParticipationService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static controllers.User.GestionAbonnements.showInfo;

public class AfficherParticipationsUserController {
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
    @FXML private TableView<Participation>              participationsTable;
    @FXML private TableColumn<Participation, String>    evenementColumn;
    @FXML private TableColumn<Participation, LocalDate> dateInscriptionColumn;
    @FXML private TableColumn<Participation, String>    statutColumn;
    @FXML private TableColumn<Participation, Boolean>   presenceColumn;
    @FXML private TableColumn<Participation, Void>      actionsColumn;

    @FXML private TextField        searchField;
    @FXML private DatePicker       filterDate;
    @FXML private ComboBox<String> filterStatut;
    @FXML private ComboBox<String> filterPresence;
    @FXML private Label            resultsCountLabel;

    // Stats
    @FXML private Label totalLabel;
    @FXML private Label confirmeLabel;
    @FXML private Label inscritLabel;
    @FXML private Label annuleLabel;

    private final ParticipationService participationService = new ParticipationService();
    private final EvenementService     evenementService     = new EvenementService();

    private ObservableList<Participation> participations;
    private FilteredList<Participation>   filteredData;

    private int idUtilisateur ; // injecté depuis la page de connexion

    // ================= SETTERS SESSION =================
    public void setIdUtilisateur(int id) {
        id = SessionManager.getCurrentUser().getCin();
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
            this.idUtilisateur = SessionManager.getCurrentUser().getCin(); // ← this.idUtilisateur

        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerSidebarAvatar(SessionManager.getCurrentUser());
        initColumns();
        initFilters();
        try {
            loadParticipations();
            setupReactiveSearch();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les participations : " + e.getMessage());
        }
    }
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

    // ================= FILTRES =================
    private void initFilters() {
        filterStatut.getItems().addAll("Tous les statuts", "Inscrit", "Confirme", "Annule");
        filterStatut.setValue("Tous les statuts");
        filterPresence.getItems().addAll("Toutes", "Oui", "Non");
        filterPresence.setValue("Toutes");

        filterStatut.setOnAction(e  -> appliquerFiltres());
        filterPresence.setOnAction(e -> appliquerFiltres());
        filterDate.setOnAction(e    -> appliquerFiltres());
    }

    // ================= COLONNES =================
    private void initColumns() {

        evenementColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(
                        evenementService.getNomEvenementById(cellData.getValue().getId_evenement()));
            } catch (SQLException e) {
                return new SimpleStringProperty("Erreur");
            }
        });

        dateInscriptionColumn.setCellValueFactory(new PropertyValueFactory<>("date_inscription"));
        dateInscriptionColumn.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : fmt.format(d));
            }
        });

        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut_participation"));
        statutColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setText(null); setStyle(""); return; }
                setText(statut);
                switch (statut.toLowerCase()) {
                    case "inscrit":
                        setStyle("-fx-background-color: #FFF9C4; -fx-text-fill: #F57F17; -fx-font-weight: bold; -fx-background-radius: 4;"); break;
                    case "confirme": case "confirmé":
                        setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-background-radius: 4;"); break;
                    case "annule": case "annulé":
                        setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; -fx-font-weight: bold; -fx-background-radius: 4;"); break;
                    default: setStyle("");
                }
            }
        });

        presenceColumn.setCellValueFactory(new PropertyValueFactory<>("presence"));
        presenceColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean presence, boolean empty) {
                super.updateItem(presence, empty);
                if (empty || presence == null) { setText(null); setStyle(""); return; }
                setText(presence ? "Oui" : "Non");
                setStyle(presence
                        ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
                        : "-fx-text-fill: #C62828; -fx-font-weight: bold;");
            }
        });

        addActionButtons();
    }

    private void addActionButtons() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button modifierBtn = new Button("Modifier");
            private final Button annulerBtn  = new Button("Annuler");
            private final HBox   box         = new HBox(8, modifierBtn, annulerBtn);

            {
                modifierBtn.setStyle("-fx-background-color: #546E7A; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");
                annulerBtn.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5;");

                modifierBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    if (estAnnulee(p)) {
                        showWarning("Action impossible", "Vous ne pouvez pas modifier une participation annulee.");
                        return;
                    }
                    ouvrirModification(p);
                });

                annulerBtn.setOnAction(e -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    if (estAnnulee(p)) {
                        showWarning("Deja annulee", "Cette participation est deja annulee.");
                        return;
                    }

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirmer l'annulation");
                    confirm.setHeaderText(null);
                    confirm.setContentText("Voulez-vous vraiment annuler votre participation ?");
                    Optional<ButtonType> result = confirm.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            p.setStatut_participation("Annule");
                            participationService.modifier(p);
                            showSuccess("Annulation", "Votre participation a ete annulee.");
                            loadParticipations();
                            appliquerFiltres();
                        } catch (SQLException ex) {
                            showError("Erreur", "Impossible d'annuler : " + ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Participation p = getTableView().getItems().get(getIndex());
                boolean annulee = estAnnulee(p);
                modifierBtn.setDisable(annulee);
                annulerBtn.setDisable(annulee);
                setGraphic(box);
            }
        });
    }

    // ================= CHARGEMENT — filtré par id_user =================
    private void loadParticipations() throws SQLException {
        List<Participation> toutes = participationService.recuperer();

        // ✅ Filtrer uniquement les participations de l'utilisateur connecté
        List<Participation> miennes = toutes.stream()
                .filter(p -> p.getId_user() == idUtilisateur)
                .collect(Collectors.toList());

        participations = FXCollections.observableArrayList(miennes);
        filteredData   = new FilteredList<>(participations, p -> true);
        participationsTable.setItems(filteredData);
        updateStats();
        updateResultsCount();
    }

    private void setupReactiveSearch() {
        searchField.textProperty().addListener((obs, old, nv) -> appliquerFiltres());
    }

    // ================= FILTRES =================
    private void appliquerFiltres() {
        filteredData.setPredicate(p ->
                matchesSearch(p) && matchesStatut(p) && matchesPresence(p) && matchesDate(p)
        );
        updateResultsCount();
    }

    private boolean matchesSearch(Participation p) {
        String t = searchField.getText();
        if (t == null || t.trim().isEmpty()) return true;
        try {
            return evenementService.getNomEvenementById(p.getId_evenement())
                    .toLowerCase().contains(t.toLowerCase());
        } catch (SQLException e) { return true; }
    }

    private boolean matchesStatut(Participation p) {
        String s = filterStatut.getValue();
        return s == null || s.equals("Tous les statuts")
                || p.getStatut_participation().equalsIgnoreCase(s);
    }

    private boolean matchesPresence(Participation p) {
        String pres = filterPresence.getValue();
        if (pres == null || pres.equals("Toutes")) return true;
        return p.isPresence() == pres.equals("Oui");
    }

    private boolean matchesDate(Participation p) {
        LocalDate f = filterDate.getValue();
        return f == null || p.getDate_inscription().equals(f);
    }

    @FXML
    private void handleResetFilters(ActionEvent event) {
        searchField.clear();
        filterDate.setValue(null);
        filterStatut.setValue("Tous les statuts");
        filterPresence.setValue("Toutes");
        appliquerFiltres();
    }

    // ================= STATS =================
    private void updateStats() {
        if (totalLabel == null) return;

        long total    = participations.size();
        long confirme = participations.stream()
                .filter(p -> "confirme".equalsIgnoreCase(p.getStatut_participation())
                        || "confirmé".equalsIgnoreCase(p.getStatut_participation())).count();
        long inscrit  = participations.stream()
                .filter(p -> "inscrit".equalsIgnoreCase(p.getStatut_participation())).count();
        long annule   = participations.stream()
                .filter(p -> "annule".equalsIgnoreCase(p.getStatut_participation())
                        || "annulé".equalsIgnoreCase(p.getStatut_participation())).count();

        totalLabel.setText(String.valueOf(total));
        confirmeLabel.setText(String.valueOf(confirme));
        inscritLabel.setText(String.valueOf(inscrit));
        annuleLabel.setText(String.valueOf(annule));
    }

    private void updateResultsCount() {
        resultsCountLabel.setText(filteredData.size() + " participation(s) trouvee(s)");
    }

    // ================= POP-UP MODIFICATION =================
    private void ouvrirModification(Participation participation) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/G-Evenements/ModifierParticipationUser.fxml"));
            Parent root = loader.load();

            ModifierParticipationUserController controller = loader.getController();
            controller.setParticipation(participation);

            Stage popup = new Stage();
            popup.initModality(Modality.WINDOW_MODAL);
            popup.initOwner(participationsTable.getScene().getWindow());
            popup.setResizable(true);
            popup.setTitle("Modifier ma participation");
            popup.setScene(new Scene(root));
            popup.showAndWait(); // BLOQUANT — reprend ici après fermeture

            loadParticipations();
            appliquerFiltres();

        } catch (IOException | SQLException e) {
            showError("Erreur", "Impossible d'ouvrir la modification : " + e.getMessage());
        }
    }

    // ================= NAVIGATION =================
    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadParticipations();
            handleResetFilters(event);
        } catch (SQLException e) {
            showError("Erreur", "Impossible d'actualiser : " + e.getMessage());
        }
    }

    @FXML
    private void goToEvenements(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/G-Evenements/AfficherEvenementsUser.fxml"));
            Parent root = loader.load();
            AfficherEvenementsUserController controller = loader.getController();
            controller.setIdUtilisateur(idUtilisateur);
            Stage stage = (Stage) participationsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la page : " + e.getMessage());
        }
    }

    @FXML
    private void goToMesParticipations(ActionEvent event) {
        // Déjà sur cette page
    }

    // ================= UTILITAIRES =================
    private boolean estAnnulee(Participation p) {
        return "annule".equalsIgnoreCase(p.getStatut_participation())
                || "annulé".equalsIgnoreCase(p.getStatut_participation());
    }

    // ================= ALERTS =================
    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }

    public void handleMesEvenements(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherEvenementsUser.fxml","Evenements");
    }

    public void ouvrirParticipations(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherParticipationsUser.fxml","Participations");
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