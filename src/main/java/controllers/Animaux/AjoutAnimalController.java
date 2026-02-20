package controllers.Animaux;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Animaux.Sexe;
import models.Animaux.animaux;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.Animaux.ServiceAnimal;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class AjoutAnimalController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;

    @FXML private TextField tfNom;
    @FXML private TextField tfEspece;
    @FXML private TextField tfPoids;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Sexe> cbSexe;

    private ServiceAnimal service = new ServiceAnimal();

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
        // Remplit le ComboBox au chargement de la page
        if (cbSexe != null) {
            cbSexe.getItems().setAll(Sexe.values());
        }
    }

    @FXML
    void handleAjouter(MouseEvent event) {

        // On utilise ta méthode de validation Popup
        if (estValide()) {
            try {
                animaux a = new animaux();
                a.setNom(tfNom.getText());
                a.setEspece(tfEspece.getText());
                a.setPoids(Float.parseFloat(tfPoids.getText()));
                a.setSexe(cbSexe.getValue());
                a.setDate_naissance(java.sql.Date.valueOf(dpDate.getValue()));

                service.ajouter(a);
                retourListe(event);
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    // TA MÉTHODE DE VALIDATION EN POPUP
    private boolean estValide() {
        String messageErreur = "";

        // 1. Vérification des champs vides
        if (tfNom.getText().trim().isEmpty() || tfEspece.getText().trim().isEmpty() ||
                tfPoids.getText().trim().isEmpty() || dpDate.getValue() == null || cbSexe.getValue() == null) {
            messageErreur += "Tous les champs doivent être remplis.\n";
        }

        // 2. Contrôle sur le Nom et l'Espèce
        if (!tfNom.getText().matches("^[a-zA-Z\\s]+$")) {
            messageErreur += "Le nom ne doit contenir que des lettres.\n";
        }
        if (!tfEspece.getText().matches("^[a-zA-Z\\s]+$")) {
            messageErreur += "La race/espèce ne doit contenir que des lettres.\n";
        }

        // 3. Contrôle sur le Poids
        try {
            float poids = Float.parseFloat(tfPoids.getText());
            if (poids <= 0) messageErreur += "Le poids doit être supérieur à 0.\n";
        } catch (NumberFormatException e) {
            messageErreur += "Le poids doit être un nombre valide (ex: 15.5).\n";
        }

        // 4. Contrôle sur la Date
        if (dpDate.getValue() != null && dpDate.getValue().isAfter(LocalDate.now())) {
            messageErreur += "La date de naissance ne peut pas être dans le futur.\n";
        }

        // SI ERREUR -> AFFICHE LA POPUP
        if (!messageErreur.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(messageErreur);
            alert.showAndWait();
            return false;
        }

        return true;
    }

    @FXML
    void retourListe(MouseEvent event) {
        changerScene(event, "AnimalsInterface/AfficherAnimaux.fxml");
    }

    private void changerScene(MouseEvent event, String fxmlFile) {
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

}