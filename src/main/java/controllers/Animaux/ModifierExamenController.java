package controllers.Animaux;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import models.Animaux.examens;
import models.Animaux.animaux;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User.Personne;
import services.Animaux.ServiceExamen;
import services.Animaux.ServiceAnimal;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ModifierExamenController {

    @FXML private ComboBox<animaux> cbAnimal;
    @FXML private TextField tfType;
    @FXML private DatePicker dpDate;
    @FXML private TextArea taDiagnostic;
    @FXML private TextArea taTraitement;
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    private ServiceExamen serviceEx = new ServiceExamen();
    private ServiceAnimal serviceAn = new ServiceAnimal();
    private examens examenSelectionne;
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
        if (gestionBtn != null) {
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        }
        if (gestionContainer != null) {
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());
        }}
    public void chargerDonnees(examens e) {
        this.examenSelectionne = e;

        try {
            cbAnimal.getItems().setAll(serviceAn.afficher());
            for (animaux a : cbAnimal.getItems()) {
                if (a.getId() == e.getId_animal()) {
                    cbAnimal.setValue(a);
                    break;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        tfType.setText(e.getType_examen());
        taDiagnostic.setText(e.getDiagnostic());
        taTraitement.setText(e.getTraitement());

        // --- CORRECTION ICI : De l'entité (Date) vers le DatePicker (LocalDate) ---
        if (e.getDate_examen() != null) {
            // On s'assure de traiter la valeur comme une java.util.Date
            java.util.Date utilDate = e.getDate_examen();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            dpDate.setValue(sqlDate.toLocalDate());
        }
    }

    @FXML
    void handleModifier(MouseEvent event) {
        if (estValide()) {
            try {
                examenSelectionne.setId_animal(cbAnimal.getValue().getId());
                examenSelectionne.setType_examen(tfType.getText());
                examenSelectionne.setDiagnostic(taDiagnostic.getText());
                examenSelectionne.setTraitement(taTraitement.getText());

                // --- DEUXIÈME CONVERSION : Du DatePicker vers l'Entité ---
                if (dpDate.getValue() != null) {
                    examenSelectionne.setDate_examen(java.sql.Date.valueOf(dpDate.getValue()));
                }

                serviceEx.modifier(examenSelectionne);
                retourListe(event);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean estValide() {
        String msg = "";
        if (cbAnimal.getValue() == null) msg += "- Animal requis.\n";
        if (tfType.getText().trim().isEmpty()) msg += "- Type d'examen requis.\n";
        if (dpDate.getValue() == null) msg += "- Date requise.\n";
        if (taDiagnostic.getText().trim().isEmpty()) msg += "- Diagnostic requis.\n";

        if (!msg.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de modification");
            alert.setHeaderText("Veuillez corriger :");
            alert.setContentText(msg);
            alert.showAndWait();
            return false;
        }
        return true;
    }

    @FXML
    void retourListe(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AnimalsInterface/AfficherExamens.fxml"));
            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //naviguer vers les autres modules :
    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.changerScene(event,"/UsersInterface/DahboardPersonne.fxml");}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.changerScene(event,"/UsersInterface/GestionTache.fxml");}



    @FXML private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        this.changerScene(event,"/UsersInterface/GestionAbonnements.fxml");}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.changerScene(event,"/UsersInterface/GestionOffre.fxml");}



    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

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
    @FXML
    private void handleLogout() {
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
            }
        }
    }
    // Méthode utilitaire pour simplifier la navigation
    private void changerScene(MouseEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlFile);
            e.printStackTrace();
        }
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
    @FXML private void handleMesTerrains(MouseEvent mouseEvent)  {         this.changerScene(mouseEvent,"/TerrainsInterface/acceuilagricoleterrain.fxml");
    }
    @FXML private void handleMesAnimaux(MouseEvent mouseEvent)   {         this.changerScene(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml");
    }
    @FXML private void handleMesStocks()    { System.out.println("📦 Stocks..."); }
    @FXML private void handleMonMateriel()  { System.out.println("🚜 Matériel..."); }
    @FXML private void handleMonProfil(MouseEvent event )    {navigateTo(event,"/UsersInterface/ProfilEmplye.fxml","Mon Profil");  }

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

    // Ajouter cette méthode handleAPropos()
    @FXML
    private void handleAPropos(MouseEvent event) {
        navigateTo(event,"/UsersInterface/ProfilAgricole.fxml","ddd");
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


    public void handleModifierAgr(MouseEvent mouseEvent) {
        if (estValide()) {
            try {
                examenSelectionne.setId_animal(cbAnimal.getValue().getId());
                examenSelectionne.setType_examen(tfType.getText());
                examenSelectionne.setDiagnostic(taDiagnostic.getText());
                examenSelectionne.setTraitement(taTraitement.getText());

                // --- DEUXIÈME CONVERSION : Du DatePicker vers l'Entité ---
                if (dpDate.getValue() != null) {
                    examenSelectionne.setDate_examen(java.sql.Date.valueOf(dpDate.getValue()));
                }

                serviceEx.modifier(examenSelectionne);
                retourListeAgr(mouseEvent);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void retourListeAgr(MouseEvent mouseEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AnimalsInterface/acceuilagricoleexamens.fxml"));
            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML private void handleMesArticles(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherArticleAgr.fxml","Articles"); }
    @FXML private void handleMesCatégories(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherCategorieAgr.fxml","Catégories "); }
}