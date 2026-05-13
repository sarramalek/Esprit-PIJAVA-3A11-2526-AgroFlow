package controllers.User;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Abonnements;
import models.User.Personne;
import services.User.AbonnementService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static controllers.User.GestionAbonnements.showInfo;

public class AcceuilAgricole {
    // Ajouter ces champs
    @FXML private Label welcomeNameLabel;
    @FXML private Hyperlink aproposLink;
    @FXML private VBox gestionSubmenu, gestionContainer;
    @FXML private Button gestionBtn;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button logoutBtn;

//image useer
@FXML private ImageView sidebarAvatarImageView;
    @FXML private Label     sidebarAvatarDefault;
    @FXML private Circle sidebarAvatarBg;
    // Labels stats abonnements
    @FXML private VBox abonnementsContainer;

    private  Personne currentUser;
    private AbonnementService abonnementService;

    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerSidebarAvatar(SessionManager.getCurrentUser());

        System.out.println("✓ AcceuilAgricole Controller initialisé");

        abonnementService = new AbonnementService();

        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }

        if (gestionBtn != null && gestionContainer != null) {
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());
        }
    }

    /*public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            if (userNameLabel != null)
                userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            if (userRoleLabel != null)
                userRoleLabel.setText("Agriculteur");
            System.out.println("✓ Utilisateur Agricole défini: " + user.getNom());

            // Charger les abonnements après avoir défini l'utilisateur
            loadAbonnements();
        }
    }*/
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

    /**
     * Charger et afficher les abonnements de l'utilisateur connecté
     */
    private void loadAbonnements() {
        if (abonnementsContainer == null || currentUser == null) return;

        abonnementsContainer.getChildren().clear();

        try {
            List<Abonnements> abonnements = abonnementService.getAbonnementsByUser(currentUser.getCin());

            if (abonnements == null || abonnements.isEmpty()) {
                // Afficher message vide
                VBox emptyBox = new VBox(15);
                emptyBox.setAlignment(Pos.CENTER);
                emptyBox.setStyle("-fx-padding: 40;");

                Label icon  = new Label("💳");
                icon.setStyle("-fx-font-size: 48px;");

                Label msg1 = new Label("Aucun abonnement actif");
                msg1.setStyle("-fx-font-size: 16px; -fx-text-fill: #95A5A6;");

                Label msg2 = new Label("Souscrivez à une offre pour accéder à plus de fonctionnalités");
                msg2.setStyle("-fx-font-size: 14px; -fx-text-fill: #BDC3C7;");

                Button btnSouscrire = new Button("➕ Voir les offres");
                btnSouscrire.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                        "-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");
                btnSouscrire.setOnMouseClicked(e -> handleMonAbonnement(e));

                emptyBox.getChildren().addAll(icon, msg1, msg2, btnSouscrire);
                abonnementsContainer.getChildren().add(emptyBox);

            } else {
                // Afficher chaque abonnement sous forme de carte
                for (Abonnements ab : abonnements) {
                    abonnementsContainer.getChildren().add(createAbonnementCard(ab));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Label errLabel = new Label("⚠️ Impossible de charger les abonnements");
            errLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 14px;");
            abonnementsContainer.getChildren().add(errLabel);
        }
    }

    /**
     * Créer une carte visuelle pour un abonnement
     */
    private HBox createAbonnementCard(Abonnements ab) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #F8FFF8; -fx-background-radius: 12; " +
                "-fx-border-color: #C8E6C9; -fx-border-radius: 12; -fx-border-width: 1; " +
                "-fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");

        // Icône
        Label icon = new Label("💳");
        icon.setStyle("-fx-font-size: 36px; -fx-background-color: #E8F5E9; " +
                "-fx-padding: 12; -fx-background-radius: 12;");

        // Infos abonnement
        VBox infos = new VBox(6);
        HBox.setHgrow(infos, Priority.ALWAYS);

        // Titre avec id offre
        Label titre = new Label("Abonnement #" + ab.getId_offre());
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

        // Dates
        HBox dates = new HBox(20);
        Label dateDebut = new Label("📅 Début : " + (ab.getDate_inscription() != null
                ? ab.getDate_inscription().toString() : "N/A"));
        dateDebut.setStyle("-fx-font-size: 13px; -fx-text-fill: #7F8C8D;");

        Label dateFin = new Label("⏰ Fin : " + (ab.getDate_expiration() != null
                ? ab.getDate_expiration().toString() : "N/A"));
        dateFin.setStyle("-fx-font-size: 13px; -fx-text-fill: #7F8C8D;");
        dates.getChildren().addAll(dateDebut, dateFin);

        infos.getChildren().addAll(titre, dates);

        // Badge statut
        String situation = ab.getSituation() != null ? ab.getSituation() : "inconnu";
        Label badge = new Label();
        switch (situation.toLowerCase()) {
            case "actif", "active" -> {
                badge.setText("✅ ACTIF");
                badge.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: #2E7D32; " +
                        "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 12px;");
            }
            case "expiré", "expire", "expired" -> {
                badge.setText("❌ EXPIRÉ");
                badge.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #C62828; " +
                        "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 12px;");
            }
            default -> {
                badge.setText("⏸ " + situation.toUpperCase());
                badge.setStyle("-fx-background-color: #FFF8E1; -fx-text-fill: #F57F17; " +
                        "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 20; -fx-font-size: 12px;");
            }
        }

        card.getChildren().addAll(icon, infos, badge);
        return card;
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML
    void ouvrirTerrains(MouseEvent event) {
        navigateTo(event, "/TerrainsInterface/agricoleaffichageterrain.fxml", "Gestion des Terrains");
    }

    @FXML
    void ouvrirPlantes(MouseEvent event) {
        navigateTo(event, "/TerrainsInterface/agricoleaffichageplante.fxml", "Liste des Plantes");
    }

    @FXML
    void ouvrirRotations(MouseEvent event) {
        navigateTo(event, "/TerrainsInterface/agricoleaffichagerotation.fxml", "Gestion des Rotations");
    }
    @FXML public void handleDashboard()    { /* Déjà sur cette page */ }
    @FXML private void handleMesTerrains(MouseEvent event )  { navigateTo(event,"/TerrainsInterface/acceuilagricoleterrain.fxml","Mes Terrains"); }
    @FXML private void handleMesAnimaux(MouseEvent event)  { navigateTo(event,"/AnimalsInterface/acceuilagricoleanimaux.fxml","Animaux"); }
    @FXML private void handleMesStocks()    { System.out.println("📦 Stocks..."); }
    @FXML private void handleMonProfil()    { System.out.println("👤 Profil..."); }
    public void ouvrirMaintenance(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMaintenance.fxml","Maintenance");
    }

    public void ouvrirAchat(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageAchat.fxml","Maintenance");

    }

    public void ouvrirMachine(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMachine.fxml","Maintenance");

    }
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
                    // On récupère le Stage et la Scene ACTUELLE
                    Scene scene = stage.getScene();

                    // SOLUTION MIRACLE : On change la racine, pas la scène !
                    scene.setRoot(root);

                    // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                    stage.show();
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

            loadAbonnements();
        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL !");
        }
    }

    // Ajouter cette méthode handleAPropos()
    @FXML
    private void handleAPropos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();
            ProfilEmploye ctrl = loader.getController();
            if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
            Stage s = new Stage();
            s.setTitle("Mon Profil"); s.setScene(new Scene(root));
            s.setResizable(true); s.initModality(Modality.APPLICATION_MODAL);
            s.centerOnScreen(); s.showAndWait();
        } catch (IOException e) { showError("Erreur"+ e.getMessage()); }
    }
    private void navigateTo(Event event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Si l'événement est null ou source non Node, on utilise logoutBtn pour trouver le Stage
            Stage stage;
            if (event != null && event.getSource() instanceof Node) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) logoutBtn.getScene().getWindow();
            }
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
    @FXML
    public void handleMesArticles(MouseEvent mouseEvent) {
        navigateTo(mouseEvent, "/StocksInterface/AfficherArticleAgr.fxml", "Articles");
    }

    @FXML
    public void handleMesCatégories(MouseEvent mouseEvent) {
        navigateTo(mouseEvent, "/StocksInterface/AfficherCategorieAgr.fxml", "Catégories");
    }
    @FXML private void handleMonMateriel(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMachine.fxml","Animaux"); }
    public void handleMesEvenements(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherEvenementsUser.fxml","Evenements");
    }

    public void ouvrirParticipations(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherParticipationsUser.fxml","Participations");
    }

    public void handlemesouvriers(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/UsersInterface/GestionOuvrier.fxml","mes ouvriers");

    }
}