package controllers.Materiels;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Materiels.Machine;
import services.Materiels.MachineService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class AfficherMachinesController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    @FXML
    private TableView<Machine> tableMachines;

    @FXML
    private TableColumn<Machine, String> colMarque;

    @FXML
    private TableColumn<Machine, String> colModele;

    @FXML
    private TableColumn<Machine, String> colEtat;

    @FXML
    private TableColumn<Machine, String> colNumeroSerie;

    @FXML
    private TableColumn<Machine, LocalDate> colDateAchat;

    @FXML
    private TableColumn<Machine, String> colNom;

    private MachineService machineService = new MachineService();
    private ObservableList<Machine> machinesList = FXCollections.observableArrayList();

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
        // Configuration des colonnes
        colMarque.setCellValueFactory(new PropertyValueFactory<>("marque"));
        colModele.setCellValueFactory(new PropertyValueFactory<>("modele"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etatM"));
        colNumeroSerie.setCellValueFactory(new PropertyValueFactory<>("numeroSerie"));
        colDateAchat.setCellValueFactory(new PropertyValueFactory<>("dateAchat"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        // Chargement des données
        chargerMachines();
    }

    private void chargerMachines() {
        try {
            machinesList.clear();
            machinesList.addAll(machineService.recuperer());
            tableMachines.setItems(machinesList);
        } catch (Exception e) {
            afficherAlerte("Erreur", "Impossible de charger les machines", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void versAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjoutMachine.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tableMachines.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir la page d'ajout", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void versModifier() {
        Machine machineSelectionnee = tableMachines.getSelectionModel().getSelectedItem();

        if (machineSelectionnee == null) {
            afficherAlerte("Attention", "Veuillez sélectionner une machine à modifier", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierMachine.fxml"));
            Parent root = loader.load();

            // Passer la machine sélectionnée au contrôleur de modification
            ModifierMachineController controller = loader.getController();
            controller.setMachine(machineSelectionnee);

            Stage stage = (Stage) tableMachines.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir la page de modification", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSupprimer() {
        Machine machineSelectionnee = tableMachines.getSelectionModel().getSelectedItem();

        if (machineSelectionnee == null) {
            afficherAlerte("Attention", "Veuillez sélectionner une machine à supprimer", Alert.AlertType.WARNING);
            return;
        }

        try {
            machineService.supprimer(machineSelectionnee.getIdM());
            afficherAlerte("Succès", "Machine supprimée avec succès", Alert.AlertType.INFORMATION);
            chargerMachines(); // Rafraîchir la table
        } catch (Exception e) {
            afficherAlerte("Erreur", "Impossible de supprimer la machine", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void retourAccueil(Event event) {
        naviguerVers("/MaterielsInterface/AccueilMateriel.fxml", event);
    }

    // Navigation vers les autres pages
    @FXML
    private void naviguerAnimaux(Event event) {
        naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml",event);
    }

    @FXML
    private void naviguerMateriels(Event event) {
        naviguerVers("/MaterielsInterface/AccueilMateriel.fxml", event);
    }

    @FXML
    private void naviguerStocks(Event event) {
        naviguerVers("/StocksInterface/afficherarticle.fxml", event);
    }

    @FXML
    private void naviguerTerrains(Event event) {
        naviguerVers("/TerrainsInterface/AfficherTerrains.fxml", event);
    }

    @FXML
    private void naviguerEvenements(Event event) {
        naviguerVers("/EventsInterface/AccueilEvenement.fxml", event);
    }

    @FXML
    private void naviguerUsers(Event event) {
        naviguerVers("/UsersInterface/Acceuil.fxml", event);
    }

    private void naviguerVers(String fxmlPath , Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));

            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
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

    //naviguer vers les autres modules

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