package controllers.Materiels;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.Optional;

public class AccueilMaterielController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;




    private void naviguerDepuisNode(String fxmlPath, Node source) {
        try {
            Parent root  = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage  stage = (Stage) source.getScene().getWindow();
            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur",
                    "Impossible de naviguer vers : " + fxmlPath + "\n" + e.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            afficherAlerte("Erreur",
                    "Erreur lors de la navigation : " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }
    /**
     * Retourne la première Stage affichée parmi les fenêtres JavaFX ouvertes.
     */
    private Stage getStageActive() {
        for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
            if (w instanceof Stage && w.isShowing()) {
                return (Stage) w;
            }
        }
        return null;
    }

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
        });}
    @FXML
    private void ouvrirMachines(MouseEvent event) {
        naviguerVers("/MaterielsInterface/AfficherMachines.fxml", event);
    }

    @FXML
    private void ouvrirMaintenances(MouseEvent event) {
        naviguerVers("/MaterielsInterface/AfficherMaintenances.fxml", event);
    }


    // Navigation vers les autres modules depuis la sidebar
    @FXML
    private void naviguerAnimaux(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/AnimalsInterface/AfficherAnimaux.fxml", event);
    }

    @FXML
    private void naviguerMateriels(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/MaterielsInterface/AccueilMateriel.fxml", event);
    }

    @FXML
    private void naviguerStocks(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/StocksInterface/afficherarticle.fxml", event);
    }

    @FXML
    private void naviguerTerrains(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/TerrainsInterface/AfficherTerrains.fxml", event);
    }

    @FXML
    private void naviguerEvenements(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/EventsInterface/AccueilEvenement.fxml", event);
    }

    @FXML
    private void naviguerUsers(javafx.event.ActionEvent event) {
        naviguerDepuisBouton("/UsersInterface/Acceuil.fxml", event);
    }

    @FXML
    private void deconnexion(javafx.event.ActionEvent event) {
        System.exit(0);
    }


    // Méthode pour naviguer depuis les cartes (MouseEvent)
    private void naviguerVers(String fxmlPath, MouseEvent event) {
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

    // Méthode pour naviguer depuis les boutons de la sidebar (ActionEvent)
    private void naviguerDepuisBouton(String fxmlPath, javafx.event.ActionEvent event) {
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
            afficherAlerte("Erreur", "Impossible de naviguer vers la page demandée: " + fxmlPath, Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de la navigation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void afficherAlerte(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.naviguerVers("/UsersInterface/DahboardPersonne.fxml",event);}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.naviguerVers("/UsersInterface/GestionTache.fxml",event);}



    @FXML
    private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        this.naviguerVers("/UsersInterface/GestionAbonnements.fxml",event);}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.naviguerVers("/UsersInterface/GestionOffre.fxml",event);}


    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) {
        this.naviguerVers("/UsersInterface/Acceuil.fxml", actionEvent);

    }
    public void handleAnimals(MouseEvent mouseEvent) {
        this.naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml",mouseEvent);

    }




    public void handleStocks(MouseEvent mouseEvent) {
        this.naviguerVers("/StocksInterface/afficherarticle.fxml",mouseEvent);
    }



    public void handleTerrains(MouseEvent mouseEvent) {
        this.naviguerVers("/TerrainsInterface/acceuilterrain.fxml",mouseEvent);
    }


    //
    public void handleEvents(MouseEvent mouseEvent) {
        this.naviguerVers("/G-Evenements/Accueil.fxml",mouseEvent);
    }


    public void handleMateriels(MouseEvent mouseEvent) {
        this.naviguerVers("/MaterielsInterface/AccueilMateriel.fxml",mouseEvent);
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
    // ================= ALERT METHODS =================
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}