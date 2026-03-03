package controllers.Terrains;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Terrains.rotation;
import models.Terrains.terrain;
import models.Terrains.plante;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.Terrains.RotationService;
import services.Terrains.TerrainService;
import services.Terrains.PlanteService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AjoutRotationController implements Initializable {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;

    @FXML
    private ComboBox<terrain> comboTerrain;
    @FXML
    private ComboBox<plante> comboPlante;
    @FXML
    private DatePicker dateDebut;
    @FXML
    private DatePicker dateFin;
    @FXML
    private ComboBox<String> comboStatus;

    private final RotationService rs = new RotationService();
    private final TerrainService ts = new TerrainService();
    private final PlanteService ps = new PlanteService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

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
        // Charger les terrains
        List<terrain> terrains = ts.afficherTous();
        comboTerrain.setItems(FXCollections.observableArrayList(terrains));
        comboTerrain.setCellFactory(param -> new ListCell<terrain>() {
            @Override
            protected void updateItem(terrain item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_terrain());
            }
        });
        comboTerrain.setButtonCell(new ListCell<terrain>() {
            @Override
            protected void updateItem(terrain item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_terrain());
            }
        });

        // Charger les plantes
        List<plante> plantes = ps.afficherToutes();
        comboPlante.setItems(FXCollections.observableArrayList(plantes));
        comboPlante.setCellFactory(param -> new ListCell<plante>() {
            @Override
            protected void updateItem(plante item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_p() + " (" + item.getVariete() + ")");
            }
        });
        comboPlante.setButtonCell(new ListCell<plante>() {
            @Override
            protected void updateItem(plante item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom_p() + " (" + item.getVariete() + ")");
            }
        });

        // Charger les statuts
        comboStatus.setItems(FXCollections.observableArrayList("En cours", "Terminée"));
        comboStatus.setValue("En cours");
    }

    @FXML
    void ajouterRotation(ActionEvent event) {
        // 1. Validation des champs
        if (comboTerrain.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un terrain.", Alert.AlertType.ERROR);
            return;
        }

        if (comboPlante.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une plante.", Alert.AlertType.ERROR);
            return;
        }

        if (dateDebut.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une date de début.", Alert.AlertType.ERROR);
            return;
        }

        if (dateFin.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner une date de fin.", Alert.AlertType.ERROR);
            return;
        }

        // 2. Validation : Date fin doit être après date début
        if (dateFin.getValue().isBefore(dateDebut.getValue())) {
            showAlert("Erreur", "La date de fin doit être après la date de début.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // 3. Récupération des données
            int idTerrain = comboTerrain.getValue().getId_terrain();
            int idPlante = comboPlante.getValue().getId_plante();
            Date sqlDateDebut = Date.valueOf(dateDebut.getValue());
            Date sqlDateFin = Date.valueOf(dateFin.getValue());
            int status = comboStatus.getValue().equals("En cours") ? 1 : 0;

            // 4. Création et ajout
            rotation r = new rotation(0, idTerrain, idPlante, sqlDateDebut, sqlDateFin, status);
            rs.ajouter(r);

            // 5. Succès
            showAlert("Succès", "Rotation ajoutée avec succès !", Alert.AlertType.INFORMATION);
            nettoyerChamps();

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void retourListe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/AffichageRotation.fxml"));
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
        comboTerrain.setValue(null);
        comboPlante.setValue(null);
        dateDebut.setValue(null);
        dateFin.setValue(null);
        comboStatus.setValue("En cours");
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