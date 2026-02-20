package controllers.User;

import javafx.event.ActionEvent;
import javafx.event.Event;
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
        this.navigateTo(event,"/UsersInterface/DahboardPersonne.fxml","Personnes - Agroflow");}


    @FXML private void handleTaches(Event event ) { /* Charger vue Tâches */
        this.navigateTo(event,"/UsersInterface/GestionTache.fxml","Taches - Agroflow");}



    @FXML private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
    this.navigateTo(event,"/UsersInterface/GestionAbonnements.fxml","Abonnement - Agroflow");}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.navigateTo(event,"/UsersInterface/GestionOffre.fxml","Offres - Agroflow");}

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
        this.navigateTo(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml","Gestion Animaux - AgroFlow ");

    }




    public void handleStocks(MouseEvent mouseEvent) {
        this.navigateTo(mouseEvent,"/StocksInterface/afficherarticle.fxml","Gestion Stocks - Agroflow ");
    }



    public void handleTerrains(MouseEvent mouseEvent) {
        this.navigateTo(mouseEvent,"/TerrainsInterface/acceuilterrain.fxml","gestion Terrains - AgroFlow ");
    }


   //
   public void handleEvents(MouseEvent mouseEvent) {
       this.navigateTo(mouseEvent,"/G-Evenements/Accueil.fxml","gestion Evenements - AgroFlow ");
   }


    public void handleMateriels(MouseEvent mouseEvent) {
        this.navigateTo(mouseEvent,"/MaterielsInterface/AccueilMateriel.fxml","gestion Materiels - AgroFlow ");
    }
    private void navigateTo(Event event, String fxmlPath, String title) {
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

}