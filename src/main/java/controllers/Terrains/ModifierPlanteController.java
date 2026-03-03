package controllers.Terrains;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Terrains.plante;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.Terrains.PlanteService;

import java.io.IOException;
import java.util.Optional;

public class ModifierPlanteController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtVariete;
    @FXML
    private TextField txtBesoinEau;
    @FXML
    private TextField txtCycle;

    private final PlanteService ps = new PlanteService();
    private plante planteSelectionnee;
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
    // Méthode pour initialiser les champs avec la plante sélectionnée
    public void initialiserAvecPlante(plante p) {
        this.planteSelectionnee = p;
        txtNom.setText(p.getNom_p());
        txtVariete.setText(p.getVariete());
        txtBesoinEau.setText(String.valueOf(p.getBesoin_eau()));
        txtCycle.setText(String.valueOf(p.getCycle_jours()));
    }

    @FXML
    public void handleModifier(ActionEvent event) {
        // 1. Récupération des données
        String nom = txtNom.getText().trim();
        String variete = txtVariete.getText().trim();
        String besoinEauStr = txtBesoinEau.getText().trim();
        String cycleStr = txtCycle.getText().trim();

        // 2. Validation des champs vides
        if (nom.isEmpty() || variete.isEmpty() || besoinEauStr.isEmpty() || cycleStr.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }

        // 3. Validation : Nom ne doit pas contenir de chiffres
        if (nom.matches(".*\\d.*")) {
            showAlert("Erreur", "Le nom ne doit pas contenir de chiffres.", Alert.AlertType.ERROR);
            return;
        }

        // 4. Validation : Variété ne doit pas contenir de chiffres
        if (variete.matches(".*\\d.*")) {
            showAlert("Erreur", "La variété ne doit pas contenir de chiffres.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // 5. Conversion des types numériques
            float besoinEau = Float.parseFloat(besoinEauStr);
            int cycle = Integer.parseInt(cycleStr);

            // 6. Mise à jour de l'objet existant
            planteSelectionnee.setNom_p(nom);
            planteSelectionnee.setVariete(variete);
            planteSelectionnee.setBesoin_eau(besoinEau);
            planteSelectionnee.setCycle_jours(cycle);

            // 7. Modification dans la base
            ps.modifier(planteSelectionnee);

            // 8. Succès et retour à la liste
            showAlert("Succès", "La plante '" + nom + "' a été modifiée avec succès !", Alert.AlertType.INFORMATION);
            retourListe(event);

        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "Le besoin en eau doit être un nombre (ex: 0.5) et le cycle un entier.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void retourListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/AffichagePlante.fxml"));
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