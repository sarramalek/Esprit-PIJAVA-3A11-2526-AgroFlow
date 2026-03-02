package controllers.Stocks;

import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.event.Event;
import com.itextpdf.layout.Document; // C'est celle-ci qu'il faut instancier
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import models.Stocks.Categorie;
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
import models.User.Personne;
import services.Stocks.CategorieService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class affichercategorieController {

    // ══════════════════════════════════════════════════════
    //  FXML — Tableau & Colonnes
    // ══════════════════════════════════════════════════════
    @FXML private TableView<Categorie>            tableCategories;
    @FXML private TableColumn<Categorie, String>  colNom;
    @FXML private TableColumn<Categorie, String>  colDescription;
    @FXML private TableColumn<Categorie, String>  colNomEn;   // optionnel
    @FXML private TableColumn<Categorie, String>  colNomAr;   // optionnel
    @FXML private TableColumn<Categorie, String>  colImage;   // optionnel — miniature Cloudinary

    // ══════════════════════════════════════════════════════
    //  FXML — Sidebar & Navigation
    // ══════════════════════════════════════════════════════
    @FXML private Button logoutBtn;
    @FXML private Button gestionBtn;
    @FXML private VBox   gestionSubmenu;
    @FXML private VBox   gestionContainer;

    // ══════════════════════════════════════════════════════
    //  Service
    // ══════════════════════════════════════════════════════
    private final CategorieService catService = new CategorieService();

    // ══════════════════════════════════════════════════════
    //  INITIALISATION
    // ══════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerSidebarAvatar(SessionManager.getCurrentUser());
        // Sidebar submenu caché par défaut
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
        if (gestionBtn != null)
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        if (gestionContainer != null)
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());

        // ── Colonnes textuelles ───────────────────────────
        // Configuration des colonnes texte
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // IMPORTANT : Ces noms doivent correspondre aux getters du modèle (ex: getNomEn)
        if (colNomEn != null) colNomEn.setCellValueFactory(new PropertyValueFactory<>("nomEn"));
        if (colNomAr != null) colNomAr.setCellValueFactory(new PropertyValueFactory<>("nomAr"));

        // Gestion de l'image Cloudinary
        if (colImage != null) {
            colImage.setCellValueFactory(new PropertyValueFactory<>("imageUrl"));
            colImage.setCellFactory(param -> new TableCell<>() {
                private final ImageView imageView = new ImageView();

                @Override
                protected void updateItem(String url, boolean empty) {
                    super.updateItem(url, empty);
                    if (empty || url == null || url.isBlank()) {
                        setGraphic(null);
                    } else {
                        // Chargement asynchrone pour ne pas figer l'UI
                        Image img = new Image(url, 50, 50, true, true, true);
                        imageView.setImage(img);
                        setGraphic(imageView);
                    }
                }
            });
            chargerDonnees();
        }




        // ── Style des lignes ──────────────────────────────
        tableCategories.setRowFactory(tv -> new TableRow<Categorie>() {
            @Override
            protected void updateItem(Categorie item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setStyle("-fx-background-color: #fdfae7;"
                            + "-fx-border-color: #dcdde1;"
                            + "-fx-border-width: 0 0 1 0;");
                }
            }
        });

        tableCategories.setPlaceholder(new Label("Aucune catégorie enregistrée"));
        chargerDonnees();
    }

    // ══════════════════════════════════════════════════════
    //  CHARGEMENT DES DONNÉES
    // ══════════════════════════════════════════════════════
    private void chargerDonnees() {
        try {
            tableCategories.setItems(FXCollections.observableArrayList(catService.recuperer()));
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════
    //  CRUD
    // ══════════════════════════════════════════════════════
    @FXML
    void ouvrirFormulaireAjout(ActionEvent event) throws IOException {
        changerScene(event, "/StocksInterface/ajoutercategorie.fxml");
    }

    @FXML
    void modifierCategorie(ActionEvent event) throws IOException {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected != null) {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/StocksInterface/ajoutercategorie.fxml"));
            Parent root = loader.load();
            ajoutercategorieController controller = loader.getController();
            controller.preparerModification(selected);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        }
    }

    @FXML
    void supprimerCategorie(ActionEvent event) {
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                catService.supprimer(selected.getId());
                chargerDonnees();
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR,
                        "Erreur : Cette catégorie est liée à des articles !").show();
            }
        }
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION INTERNE (Stocks)
    // ══════════════════════════════════════════════════════
    @FXML void allerVersArticles(ActionEvent event) throws IOException {
        changerScene(event, "/StocksInterface/afficherarticle.fxml");
    }

    @FXML void allerVersCategories(ActionEvent event) { /* Déjà sur cette page */ }

    @FXML void deconnexion(ActionEvent event) throws IOException {
        changerScene(event, "/UsersInterface/login.fxml");
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION SIDEBAR (autres modules)
    // ══════════════════════════════════════════════════════
    @FXML public void handleDashboard(MouseEvent event) throws IOException {
        changerScene(event, "/UsersInterface/Acceuil.fxml");
    }
    @FXML public void handleAnimals(MouseEvent event) throws IOException {
        changerScene(event, "/AnimalsInterface/AfficherAnimaux.fxml");
    }
    @FXML public void handleStocks(MouseEvent event) throws IOException {
        changerScene(event, "/StocksInterface/afficherarticle.fxml");
    }
    @FXML public void handleTerrains(MouseEvent event) throws IOException {
        changerScene(event, "/TerrainsInterface/acceuilterrain.fxml");
    }
    @FXML public void handleEvents(MouseEvent event) throws IOException {
        changerScene(event, "/G-Evenements/Accueil.fxml");
    }
    @FXML public void handleMateriels(MouseEvent event) throws IOException {
        changerScene(event, "/MaterielsInterface/AccueilMateriel.fxml");
    }
    @FXML private void handlePersonnes(MouseEvent event) throws IOException {
        changerScene(event, "/UsersInterface/DahboardPersonne.fxml");
    }
    @FXML private void handleTaches(MouseEvent event) throws IOException {
        changerScene(event, "/UsersInterface/GestionTache.fxml");
    }
    @FXML private void handleAbonnements(MouseEvent event) throws IOException {
        changerScene(event, "/UsersInterface/GestionAbonnements.fxml");
    }
    @FXML private void handleOffres(MouseEvent event) throws IOException {
        changerScene(event, "/UsersInterface/GestionOffre.fxml");
    }

    // ══════════════════════════════════════════════════════
    //  DÉCONNEXION
    // ══════════════════════════════════════════════════════
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                // On récupère le Stage et la Scene ACTUELLE
                Scene scene = stage.getScene();

                // SOLUTION MIRACLE : On change la racine, pas la scène !
                scene.setRoot(root);

                // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }

    // ══════════════════════════════════════════════════════
    //  SIDEBAR SUBMENU
    // ══════════════════════════════════════════════════════
    private void showGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(true);
            gestionSubmenu.setManaged(true);
        }
    }

    private void hideGestionSubmenu() {
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }
    }

    // ══════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════
    private void changerScene(Event event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxml);
            e.printStackTrace();
        }
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // navigation Front Office Agricole
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
    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleDashboardAgricole(MouseEvent event)    { navigateTo(event,"/UsersInterface/AcceuillAgr.fxml","Dashboard"); }
    @FXML private void handleMesTerrains(MouseEvent mouseEvent)  {         navigateTo(mouseEvent,"/TerrainsInterface/acceuilagricoleterrain.fxml","Terrains");
    }
    @FXML private void handleMesAnimaux(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/AnimalsInterface/afficheragricoleanimaux.fxml","Animaux");
    }
    @FXML private void handleMesArticles(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherArticleAgr.fxml","Articles"); }
    @FXML private void handleMesCatégories(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherCategorieAgr.fxml","Catégories "); }


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


    @FXML private void handleMonMateriel(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMachine.fxml","Animaux"); }
}

