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
import java.util.Optional;

public class ModifierAnimalController {

    // 1. Déclaration des champs FXML (doivent correspondre aux fx:id du fichier .fxml)
    @FXML private Button logoutBtn,gestionBtn ;
    @FXML private VBox gestionSubmenu, gestionContainer;
    @FXML private TextField tfNom;
    @FXML private TextField tfEspece;
    @FXML private TextField tfPoids;
    @FXML private DatePicker dpDate;
    @FXML private ComboBox<Sexe> cbSexe;

    // 2. Variables internes
    private ServiceAnimal service = new ServiceAnimal();
    private int idAnimalActuel; // Stocke l'ID pour savoir quel animal modifier en SQL

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
        // Remplit le combo box au chargement de la page
        cbSexe.getItems().setAll(Sexe.values());
    }

    /**
     * MÉTHODE DE PASSAGE DE DONNÉES
     * Appelée depuis AfficherAnimauxController avant d'afficher cette page.
     */

    public void chargerDonnees(animaux a) {
        // On garde l'ID précieusement pour le "WHERE id = ?" de ta requête SQL
        this.idAnimalActuel = a.getId();

        // On pré-remplit les champs avec les données actuelles de l'animal
        tfNom.setText(a.getNom());
        tfEspece.setText(a.getEspece());
        tfPoids.setText(String.valueOf(a.getPoids()));

        // Gestion de la date (Conversion Date SQL -> LocalDate pour le DatePicker)
        if (a.getDate_naissance() != null) {
            dpDate.setValue(new java.sql.Date(a.getDate_naissance().getTime()).toLocalDate());
        }

        // Sélection du sexe dans le ComboBox
        cbSexe.setValue(a.getSexe());
    }

    @FXML
    void handleModifier(MouseEvent event) {
        if (estValide()) { // On appelle la même méthode de contrôle
            try {
                animaux a = new animaux();
                a.setId(idAnimalActuel); // L'ID que tu as récupéré via chargerDonnees
                a.setNom(tfNom.getText());
                a.setEspece(tfEspece.getText());
                a.setPoids(Float.parseFloat(tfPoids.getText()));
                a.setSexe(cbSexe.getValue());
                a.setDate_naissance(java.sql.Date.valueOf(dpDate.getValue()));

                service.modifier(a);
                retourListe(event);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void retourListe(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AnimalsInterface/AfficherAnimaux.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(message);
        alert.show();
    }
    private boolean estValide() {
        String messageErreur = "";

        // 1. Vérification des champs vides
        if (tfNom.getText().trim().isEmpty() || tfEspece.getText().trim().isEmpty() ||
                tfPoids.getText().trim().isEmpty() || dpDate.getValue() == null || cbSexe.getValue() == null) {
            messageErreur += "Tous les champs doivent être remplis.\n";
        }

        // 2. Contrôle sur le Nom et l'Espèce (Pas de chiffres)
        // On utilise une expression régulière : ^[a-zA-Z\s]+$ (uniquement lettres et espaces)
        if (!tfNom.getText().matches("^[a-zA-Z\\s]+$")) {
            messageErreur += "Le nom ne doit contenir que des lettres.\n";
        }
        if (!tfEspece.getText().matches("^[a-zA-Z\\s]+$")) {
            messageErreur += "La race/espèce ne doit contenir que des lettres.\n";
        }

        // 3. Contrôle sur le Poids (Doit être un nombre positif)
        try {
            float poids = Float.parseFloat(tfPoids.getText());
            if (poids <= 0) messageErreur += "Le poids doit être supérieur à 0.\n";
        } catch (NumberFormatException e) {
            messageErreur += "Le poids doit être un nombre valide (ex: 15.5).\n";
        }

        // 4. Contrôle sur la Date (Pas de date dans le futur)
        if (dpDate.getValue() != null && dpDate.getValue().isAfter(java.time.LocalDate.now())) {
            messageErreur += "La date de naissance ne peut pas être dans le futur.\n";
        }

        // Affichage de l'alerte si erreur
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
    // Méthode utilitaire pour simplifier la navigation
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