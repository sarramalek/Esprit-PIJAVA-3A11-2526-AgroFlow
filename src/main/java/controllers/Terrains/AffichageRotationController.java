package controllers.Terrains;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Terrains.rotation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.Terrains.RotationService;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AffichageRotationController implements Initializable {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;

    @FXML
    private TableView<rotation> tableRotations;
    @FXML
    private TableColumn<rotation, String> colTerrain;
    @FXML
    private TableColumn<rotation, String> colPlante;
    @FXML
    private TableColumn<rotation, String> colVariete;
    @FXML
    private TableColumn<rotation, Date> colDateDebut;
    @FXML
    private TableColumn<rotation, Date> colDateFin;
    @FXML
    private TableColumn<rotation, String> colStatus;

    @FXML
    private TextField txtRecherche;
    @FXML
    private ComboBox<String> comboStatut;
    @FXML
    private ComboBox<String> comboTri;

    private final RotationService rs = new RotationService();
    private ObservableList<rotation> listeRotations;

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
        configurerTableau();
        configurerRecherche();
        configurerFiltreStatut();
        configurerTri();
        chargerDonnees();
    }

    private void configurerTableau() {
        colTerrain.setCellValueFactory(new PropertyValueFactory<>("nom_terrain"));
        colPlante.setCellValueFactory(new PropertyValueFactory<>("nom_plante"));
        colVariete.setCellValueFactory(new PropertyValueFactory<>("variete_plante"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut_t"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin_t"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusText"));
    }

    private void configurerRecherche() {
        // Recherche en temps réel
        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                chargerDonnees();
            } else {
                rechercherRotations(newValue);
            }
        });
    }

    private void configurerFiltreStatut() {
        // Options de filtre
        comboStatut.setItems(FXCollections.observableArrayList(
                "Tous",
                "En cours",
                "Terminée"
        ));
        comboStatut.setValue("Tous");

        // Action lors du changement de statut
        comboStatut.setOnAction(event -> {
            String statut = comboStatut.getValue();
            if (statut != null) {
                filtrerParStatut(statut);
            }
        });
    }

    private void configurerTri() {
        // Options de tri
        comboTri.setItems(FXCollections.observableArrayList(
                "Date début (récente)",
                "Date début (ancienne)",
                "Date fin (récente)",
                "Date fin (ancienne)",
                "Terrain (A-Z)",
                "Terrain (Z-A)",
                "Plante (A-Z)",
                "Statut (En cours d'abord)"
        ));

        // Action lors du changement de tri
        comboTri.setOnAction(event -> {
            String critere = comboTri.getValue();
            if (critere != null) {
                trierRotations(critere);
            }
        });
    }

    private void chargerDonnees() {
        listeRotations = FXCollections.observableArrayList(rs.afficherToutes());
        tableRotations.setItems(listeRotations);
    }

    private void rechercherRotations(String motCle) {
        List<rotation> resultats = rs.rechercher(motCle);
        listeRotations = FXCollections.observableArrayList(resultats);
        tableRotations.setItems(listeRotations);
    }

    private void filtrerParStatut(String statut) {
        if (statut.equals("Tous")) {
            chargerDonnees();
        } else {
            int statutInt = statut.equals("En cours") ? 1 : 0;
            List<rotation> resultats = rs.filtrerParStatut(statutInt);
            listeRotations = FXCollections.observableArrayList(resultats);
            tableRotations.setItems(listeRotations);
        }
    }

    private void trierRotations(String critere) {
        List<rotation> resultats = rs.trierPar(critere);
        listeRotations = FXCollections.observableArrayList(resultats);
        tableRotations.setItems(listeRotations);
    }

    @FXML
    public void reinitialiserRecherche(ActionEvent actionEvent) {
        txtRecherche.clear();
        comboStatut.setValue("Tous");
        comboTri.setValue(null);
        chargerDonnees();
    }

    @FXML
    public void versModifier(ActionEvent actionEvent) {
        rotation rotationSelectionnee = tableRotations.getSelectionModel().getSelectedItem();

        if (rotationSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une rotation à modifier.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/ModifierRotation.fxml"));
            Parent root = loader.load();

            ModifierRotationController controller = loader.getController();
            controller.initialiserAvecRotation(rotationSelectionnee);

            Stage stage = (Stage) tableRotations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page de modification", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleSupprimer(ActionEvent actionEvent) {
        rotation rotationSelectionnee = tableRotations.getSelectionModel().getSelectedItem();

        if (rotationSelectionnee == null) {
            showAlert("Attention", "Veuillez sélectionner une rotation à supprimer.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment supprimer cette rotation ?",
                ButtonType.YES, ButtonType.NO);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    rs.supprimer(rotationSelectionnee.getId_rotation());
                    chargerDonnees();
                    showAlert("Succès", "Rotation supprimée avec succès.", Alert.AlertType.INFORMATION);
                } catch (RuntimeException e) {
                    showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    public void versAjout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/AjoutRotation.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableRotations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger la page d'ajout", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void versAccueil(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/acceuilterrain.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableRotations.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String titre, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
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