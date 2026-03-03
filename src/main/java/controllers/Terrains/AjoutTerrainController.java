package controllers.Terrains;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Terrains.terrain;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import services.Terrains.TerrainService;

import java.io.IOException;
import java.util.Optional;

public class AjoutTerrainController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtSurface;
    @FXML
    private TextField txtTypeSol;
    @FXML
    private TextField txtLocalisation;
    @FXML
    private TextField txtPH;

    private final TerrainService ts = new TerrainService();
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
    void ajouterTerrain(ActionEvent event) {
        // 1. Récupération des données
        String nom = txtNom.getText().trim();
        String surfaceStr = txtSurface.getText().trim();
        String typeSol = txtTypeSol.getText().trim();
        String localisation = txtLocalisation.getText().trim();
        String phStr = txtPH.getText().trim();

        // 2. Validation des champs vides
        if (nom.isEmpty() || surfaceStr.isEmpty() || typeSol.isEmpty() || localisation.isEmpty() || phStr.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }

        // 3. Validation : Nom ne doit pas contenir de chiffres
        if (nom.matches(".*\\d.*")) {
            showAlert("Erreur", "Le nom du terrain ne doit pas contenir de chiffres.", Alert.AlertType.ERROR);
            return;
        }

        // 4. Validation : Type de sol ne doit pas contenir de chiffres
        if (typeSol.matches(".*\\d.*")) {
            showAlert("Erreur", "Le type de sol ne doit pas contenir de chiffres.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // 5. Conversion des types numériques
            float surface = Float.parseFloat(surfaceStr);
            float ph = Float.parseFloat(phStr);

            // 6. Validation : Surface doit être positive
            if (surface <= 0) {
                showAlert("Erreur", "La surface doit être supérieure à 0.", Alert.AlertType.ERROR);
                return;
            }

            // 7. Validation : pH doit être entre 0 et 14
            if (ph < 0 || ph > 14) {
                showAlert("Erreur", "Le pH doit être compris entre 0 et 14.", Alert.AlertType.ERROR);
                return;
            }

            // 8. Création et ajout de l'objet
            terrain t = new terrain(0, nom, surface, typeSol, localisation, ph);
            ts.ajouter(t);

            // 9. Succès et réinitialisation
            showAlert("Succès", "Le terrain '" + nom + "' a été ajouté avec succès !", Alert.AlertType.INFORMATION);
            nettoyerChamps();

        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "La surface et le pH doivent être des nombres (ex: 100.5 et 6.5).", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void retourListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/AffichageTerrain.fxml"));
            Parent root = loader.load();


            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à la liste : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void nettoyerChamps() {
        txtNom.clear();
        txtSurface.clear();
        txtTypeSol.clear();
        txtLocalisation.clear();
        txtPH.clear();
    }

    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.chargerPage(event,"/UsersInterface/DahboardPersonne.fxml","Personnes - agroflow");}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.chargerPage(event,"/UsersInterface/GestionTache.fxml","taches - agroflow");}



    @FXML
    private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        this.chargerPage(event,"/UsersInterface/GestionAbonnements.fxml","abonnements - agroflow - Agroflow");}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.chargerPage(event,"/UsersInterface/GestionOffre.fxml","offres - agroflow - Agroflow");}


    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) throws IOException {
        this.chargerPage(actionEvent, "/UsersInterface/Acceuil.fxml","Acceuil - Agroflow ");

    }
    public void handleAnimals(MouseEvent mouseEvent) {
        this.chargerPage(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml","Animals - agroflow");

    }




    public void handleStocks(MouseEvent mouseEvent) {
        this.chargerPage(mouseEvent,"/StocksInterface/afficherarticle.fxml","Stocks - agroflow");
    }



    public void handleTerrains(MouseEvent mouseEvent) {
        this.chargerPage(mouseEvent,"/TerrainsInterface/acceuilterrain.fxml","Terrains - agroflow");
    }


    //
    public void handleEvents(MouseEvent mouseEvent) {
        this.chargerPage(mouseEvent,"/G-Evenements/Accueil.fxml","Evenements - agroflow");
    }


    public void handleMateriels(MouseEvent mouseEvent) {
        this.chargerPage(mouseEvent,"/MaterielsInterface/AccueilMateriel.fxml","Materiels - agroflow");
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
        }}

}