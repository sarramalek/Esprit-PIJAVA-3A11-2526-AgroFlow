package controllers.Animaux;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Animaux.animaux;
import models.Animaux.Sexe;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.Animaux.ServiceAnimal;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

public class AfficherAnimauxController {

    @FXML private TableView<animaux> tableAnimaux;
    @FXML private TableColumn<animaux, String> colNom;
    @FXML private TableColumn<animaux, String> colEspece;
    @FXML private TableColumn<animaux, Float> colPoids;
    @FXML private TableColumn<animaux, Date> colDate; // Nouvelle colonne
    @FXML private TableColumn<animaux, Sexe> colSexe; // Nouvelle colonne
    @FXML private Button gestionToggle, operationsToggle;
    @FXML private Button gestionBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button logoutBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;


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

        // Liaison de TOUTES les colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEspece.setCellValueFactory(new PropertyValueFactory<>("espece"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_naissance"));
        colSexe.setCellValueFactory(new PropertyValueFactory<>("sexe"));

        refreshTable();
    }

    private void refreshTable() {
        try {
            ObservableList<animaux> list = FXCollections.observableArrayList(service.afficher());
            tableAnimaux.setItems(list);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleSupprimer(ActionEvent event) {
        animaux selectionne = tableAnimaux.getSelectionModel().getSelectedItem();
        if (selectionne != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de suppression");
            alert.setHeaderText("Supprimer " + selectionne.getNom() + " ?");
            alert.setContentText("Voulez-vous vraiment supprimer cet animal ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    service.supprimer(selectionne.getId());
                    refreshTable();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            alerteSelection();
        }
    }

    @FXML
    void versModifier(ActionEvent event) {
        // 1. On récupère l'animal sélectionné dans la TableView
        animaux selectionne = tableAnimaux.getSelectionModel().getSelectedItem();

        if (selectionne != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AnimalsInterface/ModifierAnimal.fxml"));
                Parent root = loader.load();

                // 2. Accéder au contrôleur de la page de modification
                ModifierAnimalController controller = loader.getController();

                // 3. ENVOYER les données de l'animal au formulaire
                controller.chargerDonnees(selectionne);

                // 4. Afficher la nouvelle page
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Alerte si rien n'est sélectionné
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Veuillez sélectionner un animal à modifier.");
            alert.show();
        }
    }


    @FXML
    void versAjout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AnimalsInterface/ajoutAnimaux.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void naviguerVersExamens( MouseEvent event) {
        try {
            // Le nom du fichier doit être EXACT (attention aux majuscules)
            Parent root = FXMLLoader.load(getClass().getResource("/AnimalsInterface/AfficherExamens.fxml"));

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Pour garantir que la taille reste identique (1100x700)
            Scene scene = stage.getScene();
            scene.setRoot(root);

        } catch (IOException e) {
            System.err.println("Le fichier /AfficherExamens.fxml est introuvable ou contient une erreur !");
            e.printStackTrace();
        }
    }

    @FXML
    void goToExamens(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AnimalsInterface/AfficherExamens.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Dans tes contrôleurs (ou une classe Helper)
    private void changerScene( MouseEvent event, String fxmlFile) {
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


    private void alerteSelection() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Veuillez sélectionner un animal dans le tableau.");
        alert.show();
    }

    //navigation vers les autres modules
    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.changerScene(event,"/UsersInterface/DahboardPersonne.fxml");}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.changerScene(event,"/UsersInterface/GestionTache.fxml");}



    @FXML private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        this.changerScene(event,"/UsersInterface/GestionAbonnements.fxml");}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.changerScene(event,"/UsersInterface/GestionOffre.fxml");}

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

    public void handleDashboard(MouseEvent actionEvent) {
        this.changerScene(actionEvent,"/UsersInterface/Acceuil.fxml");
    }

    public void handleAnimals(MouseEvent mouseEvent) {
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