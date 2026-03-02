package controllers.Animaux;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;

import controllers.User.AcceuilAgricole;
import controllers.User.ProfilEmploye;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.util.Duration;
import models.Animaux.animaux;
import models.Animaux.examens;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import models.User.Personne;
import services.Animaux.ServiceAnimal;
import services.Animaux.ServiceExamen;
import utils.SessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class AfficherExamensController {

    @FXML private TableView<examens> tvExamens;
    @FXML private TableColumn<examens, String> colAnimal;
    @FXML private TableColumn<examens, String> colType;
    @FXML private TableColumn<examens, java.sql.Date> colDate;
    @FXML private TableColumn<examens, String> colDiagnostic;
    @FXML private TableColumn<examens, String> colTraitement;
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    @FXML private Button btnAjouter;
    private List<animaux> animauxList = new ArrayList<>();

    @FXML private Button dashboardBtn;

    @FXML private Label welcomeNameLabel;
    @FXML private Hyperlink aproposLink;

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    private Personne currentUser;
    //image useer
    @FXML private ImageView sidebarAvatarImageView;
    @FXML private Label     sidebarAvatarDefault;
    @FXML private Circle sidebarAvatarBg;

    @FXML private TableColumn<examens, String> colTraduction;
    @FXML private TableColumn<examens, String> colConseils;

    @FXML private Circle badgeRouge;
    @FXML private Label lblNbAlertes;
    @FXML private TextField filterType;
    @FXML private DatePicker filterDate;

    private ObservableList<examens> masterData = FXCollections.observableArrayList();
    private FilteredList<examens> filteredData;
    private ServiceExamen service = new ServiceExamen();
    private ServiceAnimal serviceAn = new ServiceAnimal();

    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerSidebarAvatar(SessionManager.getCurrentUser());
        // Cacher submenu par défaut
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }

        // ✅
        if (gestionBtn != null) {
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        }
        if (gestionContainer != null) {
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());
        }

        configurerColonnes();
        chargerDonnees();

        filteredData = new FilteredList<>(masterData, p -> true);
        filterType.textProperty().addListener((obs, old, nv) -> appliquerFiltres());
        filterDate.valueProperty().addListener((obs, old, nv) -> appliquerFiltres());
        tvExamens.setItems(filteredData);

        demarrerSystemeAlerte();
    }

    private void chargerAnimaux() {
        try {
            animauxList = serviceAn.afficher();
        } catch (SQLException e) {
            animauxList = new ArrayList<>();
        }
    }

    private void configurerColonnes() {
        colAnimal.setCellValueFactory(cellData -> {
            int id = cellData.getValue().getId_animal();

            animaux target = new animaux();
            target.setId(id);

            return new SimpleStringProperty(
                    animauxList.stream()
                            .filter(a -> a.equals(target))
                            .findFirst()
                            .map(animaux::getNom)
                            .orElse("Inconnu")
            );
        });

        colType.setCellValueFactory(new PropertyValueFactory<>("type_examen"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_examen"));
        colDiagnostic.setCellValueFactory(new PropertyValueFactory<>("diagnostic"));
        colTraitement.setCellValueFactory(new PropertyValueFactory<>("traitement"));

        // TRADUCTION via API MyMemory
        colTraduction.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    String diag = getTableRow().getItem().getDiagnostic();
                    if (diag == null || diag.isEmpty()) {
                        setText("-");
                    } else {
                        Task<String> task = new Task<>() {
                            @Override protected String call() throws Exception {
                                String query = diag.replace(" ", "%20");
                                URL url = new URL("https://api.mymemory.translated.net/get?q=" + query + "&langpair=fr|en");
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                try (Scanner s = new Scanner(conn.getInputStream())) {
                                    String resp = s.useDelimiter("\\A").next();
                                    return resp.split("\"translatedText\":\"")[1].split("\"")[0];
                                }
                            }
                        };
                        task.setOnSucceeded(e -> setText("🇬🇧 " + task.getValue()));
                        new Thread(task).start();
                    }
                }
            }
        });

        // CONSEILS (Logique locale)
        colConseils.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String diag = getTableRow().getItem().getDiagnostic().toLowerCase();
                    if (diag.contains("infection")) {
                        setText("⚠️ Isoler l'animal");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (diag.contains("urgence") || diag.contains("fracture")) {
                        setText("🚨 Rappel Vétérinaire");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setText("✅ Suivi normal");
                        setStyle("-fx-text-fill: #27ae60;");
                    }
                }
            }
        });
    }


//---------------
@FXML
void ouvrirStats(ActionEvent event) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Statistiques");
    alert.setHeaderText("Analyse des examens");
    alert.setContentText("La fonctionnalité des statistiques sera bientôt disponible !");
    alert.show();
}

    @FXML
    void afficherConseilsSante(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AnimalsInterface/FicheSanteView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Fiches de Santé - API Externe");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement de FicheSanteView.fxml : " + e.getMessage());
        }
    }

    @FXML void ouvrirDetailsAlertes() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Rappels du jour");
        alert.setHeaderText("Examens prévus aujourd'hui");
        long nb = masterData.stream()
                .filter(e -> e.getDate_examen() != null && ((java.sql.Date) e.getDate_examen()).toLocalDate().equals(LocalDate.now()))
                .count();
        alert.setContentText("Vous avez " + nb + " examen(s) à traiter.");
        alert.show();
    }

    @FXML void traduireDiagnostics(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText("La colonne Traduction utilise l'API MyMemory.");
        alert.show();
    }
    private void appliquerFiltres() {
        filteredData.setPredicate(ex -> {
            boolean typeMatch = filterType.getText() == null || filterType.getText().isEmpty() ||
                    ex.getType_examen().toLowerCase().contains(filterType.getText().toLowerCase());
            boolean dateMatch = filterDate.getValue() == null ||
                    ((java.sql.Date) ex.getDate_examen()).toLocalDate().equals(filterDate.getValue());
            return typeMatch && dateMatch;
        });
    }

    private void demarrerSystemeAlerte() {
        verifierRappelsAujourdhui();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> verifierRappelsAujourdhui()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void verifierRappelsAujourdhui() {
        LocalDate today = LocalDate.now();
        long nb = masterData.stream()
                .filter(e -> e.getDate_examen() != null && ((java.sql.Date) e.getDate_examen()).toLocalDate().equals(today))
                .count();

        if (nb > 0) {
            if(badgeRouge != null) badgeRouge.setVisible(true);
            if(lblNbAlertes != null) {
                lblNbAlertes.setVisible(true);
                lblNbAlertes.setText(String.valueOf(nb));
            }
        } else {
            if(badgeRouge != null) badgeRouge.setVisible(false);
            if(lblNbAlertes != null) lblNbAlertes.setVisible(false);
        }
    }


    private void chargerDonnees() {
        try { masterData.setAll(service.afficher()); } catch (Exception e) { e.printStackTrace(); }

    }

    @FXML
    void handleSupprimer(ActionEvent event) {
        examens selection = tvExamens.getSelectionModel().getSelectedItem();
        if (selection != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Suppression");
            alert.setContentText("Voulez-vous vraiment supprimer cet examen ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.supprimer(selection.getId());
                    chargerDonnees();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            afficherAlerteSelection();
        }
    }

    @FXML
    void handleModifier(ActionEvent event) {
        examens selection = tvExamens.getSelectionModel().getSelectedItem();
        if (selection != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AnimalsInterface/ModifierExamen.fxml"));
                Parent root = loader.load();

                // Transmission de l'objet au contrôleur de modification
                ModifierExamenController controller = loader.getController();
                controller.chargerDonnees(selection);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            afficherAlerteSelection();
        }
    }

    @FXML
    void naviguerAjout(MouseEvent event) {
        changerScene(event, "/AnimalsInterface/AjoutExamen.fxml");
    }

    @FXML
    void naviguerVersAnimaux(MouseEvent event) {
        changerScene(event, "/AnimalsInterface/AfficherAnimaux.fxml");
    }
    @FXML void reinitialiserFiltres() { filterType.clear(); filterDate.setValue(null); }

    // Méthode utilitaire pour simplifier la navigation
    private void changerScene( MouseEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));

            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlFile);
            e.printStackTrace();
        }
    }

    private void afficherAlerteSelection() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Veuillez sélectionner un examen dans le tableau.");
        alert.show();
    }
    @FXML
    void handleDeconnexion(ActionEvent event) {
        try {
            // Remplacez "/Login.fxml" par le nom exact de votre page de connexion
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/UsersInterface/login.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Erreur lors de la déconnexion : " + e.getMessage());
        }
    }


    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.changerScene(event,"/UsersInterface/DahboardPersonne.fxml");}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.changerScene(event,"/UsersInterface/GestionTache.fxml");}



    @FXML private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        this.changerScene(event,"/UsersInterface/GestionAbonnements.fxml");}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.changerScene(event,"/UsersInterface/GestionOffre.fxml");}





    public void handleDashboard(MouseEvent actionEvent) {
        this.changerScene(actionEvent,"/UsersInterface/Acceuil.fxml");
    }
    public void handleAnimals( MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml");

    }




    public void handleStocks(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/StocksInterface/afficherarticle.fxml");
    }



    public void handleTerrains(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/TerrainsInterface/acceuilterrain.fxml");
    }


    //
    public void handleEvents(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/G-Evenements/Accueil.fxml");
    }


    public void handleMateriels(MouseEvent mouseEvent) {
        this.changerScene(mouseEvent,"/MaterielsInterface/AccueilMateriel.fxml");
    }

    /**
     * Afficher une erreur
     */
    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
                    if (!image.isError() && sidebarAvatarImageView != null) {
                        sidebarAvatarImageView.setImage(image);
                        sidebarAvatarImageView.setVisible(true);
                        sidebarAvatarImageView.setManaged(true);
                        if (sidebarAvatarDefault != null) sidebarAvatarDefault.setVisible(false);
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
    @FXML private void handleMesTerrains(MouseEvent mouseEvent)  {         this.changerScene(mouseEvent,"/TerrainsInterface/acceuilagricoleterrain.fxml");
    }
    @FXML private void handleMesAnimaux(MouseEvent mouseEvent)   {         this.changerScene(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml");
    }
    @FXML private void handleMesStocks()    { System.out.println("📦 Stocks..."); }
    @FXML private void handleMonMateriel(MouseEvent mouseEvent)  {         navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMachine.fxml","Materiels");
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
    } catch (IOException e) { showError("Erreur"+ e.getMessage()); }  }

    // ✓ CORRECT
    @FXML
    private void handleMonAbonnement(MouseEvent event) {
        System.out.println("💳 Ouverture Mon Abonnement...");
        navigateTo(event,"/UsersInterface/MesAbonnements.fxml","Mes Abonnements");
    }
    // ── Logout ────────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(AcceuilAgricole.class.getResource("/UsersInterface/login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) logoutBtn.getScene().getWindow();
                    stage.setScene(new Scene(root, 1500, 700));
                    stage.setTitle("AgroFlow - Connexion");
                    stage.setMaximized(true);
                    System.out.println("✓ Déconnexion réussie");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showGestionSubmenu() {
        if (gestionSubmenu != null) { gestionSubmenu.setVisible(true);  gestionSubmenu.setManaged(true); }
    }
    public void hideGestionSubmenu() {
        if (gestionSubmenu != null) { gestionSubmenu.setVisible(false); gestionSubmenu.setManaged(false); }
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
    @FXML private void handleMesArticles(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherArticleAgr.fxml","Articles"); }
    @FXML private void handleMesCatégories(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherCategorieAgr.fxml","Catégories "); }
    // Ajouter cette méthode handleAPropos()
    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
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


    public void handleModifierAgr(ActionEvent actionEvent) {
        examens selection = tvExamens.getSelectionModel().getSelectedItem();
        if (selection != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AnimalsInterface/ModifierExamenAgr.fxml"));
                Parent root = loader.load();

                // Transmission de l'objet au contrôleur de modification
                ModifierExamenController controller = loader.getController();
                controller.chargerDonnees(selection);

                Stage stage = (Stage) ((Node)actionEvent.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            afficherAlerteSelection();
        }
    }
}