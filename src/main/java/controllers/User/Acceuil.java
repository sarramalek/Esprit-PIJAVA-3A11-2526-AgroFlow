package controllers.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.User.Personne;

import java.io.IOException;
import java.util.Optional;

public class Acceuil {
    //sub menu
    @FXML private VBox gestionSubmenu, operationsSubmenu,gestionContainer;


    @FXML private Button gestionToggle, operationsToggle;
    @FXML private Button gestionBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button logoutBtn;

    private static Personne currentUser;

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
        System.out.println("✓ AccueilController initialisé");
    }

    /**
     * Définir l'utilisateur connecté
     */
    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("✓ Utilisateur défini: " + user.getNom());
        }
    }

    /**
     * Ouvrir le module Personnes
     */
    @FXML
    public static void ouvrirPersonnes(MouseEvent event) {
        System.out.println("👥 Ouverture du module Personnes...");

        try {
            FXMLLoader loader = new FXMLLoader(Acceuil.class.getResource("/UsersInterface/DahboardPersonne.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur au contrôleur
            DashboardPersonnes controller = loader.getController();
            if (currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Gestion du Personnel");

            System.out.println("✓ Module Personnes chargé");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture du module Personnes");
            e.printStackTrace();
            showError("Erreur", "Impossible de charger le module Personnes");
        }
    }

    /**
     * Ouvrir le module Tâches
     */
    public static void ouvrirTaches(MouseEvent event) {
        try {
            System.out.println("🗂️ Ouverture du module Tâches...");

            // Charger le bon fichier FXML
            FXMLLoader loader = new FXMLLoader(Acceuil.class.getResource("/UsersInterface/GestionTache.fxml"));
            Parent root = loader.load();

            // Récupérer le controller (optionnel, seulement si nécessaire)
            GestionTache controller = loader.getController();

            // Passer l'utilisateur si nécessaire
            // if (currentUser != null) {
            //     controller.setCurrentUser(currentUser);
            // }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Gestion des Tâches");
            stage.setMaximized(true);
            stage.show();

            System.out.println("✓ Module Tâches chargé");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors du chargement du module Tâches");
            e.printStackTrace();
        }
    }

    /**
     * Ouvrir le module Offres
     */
    @FXML
    public static void ouvrirOffres(MouseEvent event) {
        System.out.println("🏷️ Ouverture du module Offres...");

        try {
            FXMLLoader loader = new FXMLLoader(Acceuil.class.getResource("/UsersInterface/GestionOffre.fxml"));
            Parent root = loader.load();

            // Passer l'utilisateur au contrôleur si nécessaire
            // GestionOffresController controller = loader.getController();
            // if (currentUser != null) {
            //     controller.setCurrentUser(currentUser);
            // }

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Gestion des Offres");
            stage.setMaximized(true);

            System.out.println("✓ Module Offres chargé");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture du module Offres");
            e.printStackTrace();
            showInfo("À venir", "Le module Offres sera disponible prochainement");
        }
    }

    /**
     * Ouvrir le module Abonnements
     */
    @FXML
    public static void ouvrirAbonnements(MouseEvent event) {
        System.out.println("📋 Ouverture du module Abonnements...");

        try {
            FXMLLoader loader = new FXMLLoader(Acceuil.class.getResource("/UsersInterface/GestionAbonnements.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Gestion des Abonnements");
            stage.setMaximized(true);

            System.out.println("✓ Module Abonnements chargé");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture du module Abonnements");
            e.printStackTrace();
            showInfo("À venir", "Le module Abonnements sera disponible prochainement");
        }
    }

    /**
     * Ouvrir le module Affectations
     */
    @FXML
    public static void ouvrirAffectations(MouseEvent event) {
        System.out.println("🔄 Ouverture du module Affectations...");

        try {
            FXMLLoader loader = new FXMLLoader(Acceuil.class.getResource("/GestionAffectation.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Gestion des Affectations");
            stage.setMaximized(true);

            System.out.println("✓ Module Affectations chargé");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture du module Affectations");
            e.printStackTrace();
            showInfo("À venir", "Le module Affectations sera disponible prochainement");
        }
    }

    /**
     * Gérer la déconnexion
     */
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
                Scene scene = new Scene(root, 900, 600);
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
        this.ouvrirPersonnes(event);
    }

    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
this.ouvrirTaches(event);
    }
    @FXML private void handleAffectations(MouseEvent event) { /* Charger vue Affectations */
    this.ouvrirAffectations(event);}
    @FXML private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
    this.ouvrirAbonnements(event);}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
    this.ouvrirOffres(event);}
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

    public void handleDashboard(ActionEvent actionEvent) {
        System.out.println("Dashboard cliqué");
    }

    public void handleAnimals(MouseEvent mouseEvent) {
        this.ouvrirAnimaux(mouseEvent);

    }

    public static void ouvrirAnimaux(MouseEvent event) {
        System.out.println("🔄 Ouverture du module Animaux...");

        try {
            FXMLLoader loader = new FXMLLoader(Acceuil.class.getResource("/AnimalsInterface/AfficherAnimaux.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Gestion des Affectations");
            stage.setMaximized(true);

            System.out.println("✓ Module Affectations chargé");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture du module Animaux");
            e.printStackTrace();
            showInfo("À venir", "Le module Affectations sera disponible prochainement");
        }
    }

    public static void ouvrirStocks(MouseEvent event) {
        System.out.println("🔄 Ouverture du module stocks...");

        try {
            FXMLLoader loader = new FXMLLoader(Acceuil.class.getResource("/StocksInterface/afficherarticle.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Gestion des Affectations");
            stage.setMaximized(true);

            System.out.println("✓ Module stocks chargé");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture du module Affectations");
            e.printStackTrace();
            showInfo("À venir", "Le module Affectations sera disponible prochainement");
        }
    }
    public void handleStocks(MouseEvent mouseEvent) {
        this.ouvrirStocks(mouseEvent);
    }

    public static void ouvrirTerrains(MouseEvent event) {
        System.out.println("🔄 Ouverture du module Terrains...");

        try {
            FXMLLoader loader = new FXMLLoader(Acceuil.class.getResource("/TerrainsInterface/acceuilterrain.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Gestion des Terrains");
            stage.setMaximized(true);

            System.out.println("✓ Module stocks chargé");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture du module Terrains");
            e.printStackTrace();
            showInfo("À venir", "Le module Terrains sera disponible prochainement");
        }
    }

    public void handleTerrains(MouseEvent mouseEvent) {
        this.ouvrirTerrains(mouseEvent);
    }


   //

    public static void ouvrirMateriels(MouseEvent event) {
        System.out.println("🔄 Ouverture du module Materiels...");

        try {
            FXMLLoader loader = new FXMLLoader(Acceuil.class.getResource("/MaterielsInterface/AccueilMateriel.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Gestion des Materiels");
            stage.setMaximized(true);

            System.out.println("✓ Module stocks chargé");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture du module Materiels");
            e.printStackTrace();
            showInfo("À venir", "Le module Terrains sera disponible prochainement");
        }
    }

    public void handleMateriels(MouseEvent mouseEvent) {
        this.ouvrirMateriels(mouseEvent);
    }


}