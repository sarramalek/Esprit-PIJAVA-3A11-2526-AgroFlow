package controllers.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Personne;
import models.User.offres;
import services.User.OffresServicees;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GestionOffres {
    @FXML private VBox gestionSubmenu, operationsSubmenu,gestionContainer;


    @FXML private Button gestionToggle, operationsToggle;
    @FXML private Button gestionBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button personnesBtn;
    @FXML private Button tachesBtn;
    @FXML private Button offresBtn;
    @FXML private Button abonnementsBtn;
    @FXML private Button affectationsBtn;
    @FXML private Button logoutBtn;
    @FXML private Button addOffreBtn;
    @FXML private Label userNameLabel;

    // Statistiques
    @FXML private Label totalOffresLabel;
    @FXML private Label maxPrixLabel;
    @FXML private Label avgPrixLabel;

    // Recherche
    @FXML private TextField searchField;

    // Table
    @FXML private TableView<offres> offresTable;
    @FXML private TableColumn<offres, Integer> idColumn;
    @FXML private TableColumn<offres, String> nomColumn;
    @FXML private TableColumn<offres, String> descriptionColumn;
    @FXML private TableColumn<offres, Float> prixColumn;
    @FXML private TableColumn<offres, Integer> dureeColumn;
    @FXML private TableColumn<offres, Void> actionsColumn;

    private OffresServicees offresService;
    private ObservableList<offres> offresList;
    private ObservableList<offres> allOffresList;
    private Personne currentUser;

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
        try {
            offresService = new OffresServicees();
            System.out.println("✓ GestionOffresController initialisé");

            // Configurer la table
            setupTable();

            // Charger les offres
            loadOffres();

            // Configurer la recherche
            setupSearch();

            // Mettre à jour les statistiques
            updateStatistics();

        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'initialisation");
            e.printStackTrace();
            showError("Erreur d'initialisation", "Impossible de charger les offres");
        }
    }

    /**
     * Définir l'utilisateur connecté
     */
    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            System.out.println("✓ Utilisateur défini: " + user.getNom());
        }
    }

    /**
     * Configurer la table
     */
    private void setupTable() {
        // Configurer les colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id_offres"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom_offre"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree_offre"));

        // Cell factory pour le prix avec formatage
        prixColumn.setCellFactory(col -> new TableCell<offres, Float>() {  // ❌ Double → ✅ Float
            @Override
            protected void updateItem(Float prix, boolean empty) {  // ❌ Double → ✅ Float
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f DT", prix));
                    setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                }
            }
        });

        // Cell factory pour la durée


                    dureeColumn.setCellFactory(col -> new TableCell<offres, Integer>() {
                        @Override
                        protected void updateItem(Integer duree, boolean empty) {  // ✅ boolean empty
                            super.updateItem(duree, empty);
                            if (empty || duree == null) {
                                setText(null);
                            } else {
                                setText(duree + " jours");
                            }
                        }
                    });

        // Colonne Actions avec boutons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox hbox = new HBox(10, editBtn, deleteBtn);

            {
                hbox.setAlignment(Pos.CENTER);

                editBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-padding: 5 15; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-padding: 5 15; -fx-cursor: hand;");

                editBtn.setOnAction(event -> {
                    offres offre = getTableView().getItems().get(getIndex());
                    handleEditOffre(offre);
                });

                deleteBtn.setOnAction(event -> {
                    offres offre = getTableView().getItems().get(getIndex());
                    handleDeleteOffre(offre);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        offresTable.setStyle("-fx-background-color: transparent;");
    }

    /**
     * Charger les offres
     */
    public void loadOffres() {
        try {
            List<offres> offres = offresService.recuperer();
            allOffresList = FXCollections.observableArrayList(offres);
            offresList = FXCollections.observableArrayList(offres);
            offresTable.setItems(offresList);

            System.out.println("✓ " + offres.size() + " offres chargées");

        } catch (SQLException e) {
            System.err.println("✗ Erreur lors du chargement des offres");
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les offres");
        }
    }

    /**
     * Configurer la recherche
     */
    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });
    }

    /**
     * Appliquer les filtres de recherche
     */
    private void applyFilters() {
        if (allOffresList == null) return;

        String searchText = searchField.getText().toLowerCase();
        ObservableList<offres> filteredList = FXCollections.observableArrayList();

        for (offres o : allOffresList) {
            boolean matchesSearch = searchText.isEmpty() ||
                    (o.getNom_offre() != null && o.getNom_offre().toLowerCase().contains(searchText)) ||
                    (o.getDescription() != null && o.getDescription().toLowerCase().contains(searchText));

            if (matchesSearch) {
                filteredList.add(o);
            }
        }

        offresTable.setItems(filteredList);
        System.out.println("✓ " + filteredList.size() + " offres affichées après filtrage");
    }

    /**
     * Mettre à jour les statistiques
     */
    private void updateStatistics() {
        if (allOffresList == null || allOffresList.isEmpty()) {
            totalOffresLabel.setText("0");
            maxPrixLabel.setText("0 DT");
            avgPrixLabel.setText("0 DT");
            return;
        }

        int total = allOffresList.size();
        double maxPrix = allOffresList.stream()
                .mapToDouble(offres::getPrix)
                .max()
                .orElse(0.0);
        double avgPrix = allOffresList.stream()
                .mapToDouble(offres::getPrix)
                .average()
                .orElse(0.0);

        totalOffresLabel.setText(String.valueOf(total));
        maxPrixLabel.setText(String.format("%.2f DT", maxPrix));
        avgPrixLabel.setText(String.format("%.2f DT", avgPrix));
    }

    /**
     * Gérer l'ajout d'une offre
     */
    @FXML
    private void handleAddOffre() {
        System.out.println("➕ Ajouter une offre");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/AjoutOffre.fxml"));
            Parent root = loader.load();

            AjoutOffre controller = loader.getController();
            controller.setGestionOffresController(this);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Offre");
            stage.setScene(new Scene(root, 500, 550));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture du formulaire d'ajout");
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    /**
     * Gérer la modification d'une offre
     */
    private void handleEditOffre(offres offre) {
        System.out.println("✏️ Modifier l'offre: " + offre.getNom_offre());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ModifierOffre.fxml"));
            Parent root = loader.load();

            ModifierOffre controller = loader.getController();
            controller.setGestionOffresController(this);
            controller.setOffre(offre);

            Stage stage = new Stage();
            stage.setTitle("Modifier l'Offre");
            stage.setScene(new Scene(root, 500, 550));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();

            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de l'ouverture du formulaire de modification");
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire de modification");
        }
    }

    /**
     * Gérer la suppression d'une offre
     */
    private void handleDeleteOffre(offres offre) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'offre");
        alert.setContentText("Voulez-vous vraiment supprimer l'offre \"" + offre.getNom_offre() + "\" ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                offresService.supprimer(offre.getId_offres());
                loadOffres();
                updateStatistics();
                showSuccess("Succès", "Offre supprimée avec succès");
            } catch (SQLException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de supprimer l'offre");
            }
        }
    }

    /**
     * Navigation vers Dashboard
     */
    @FXML
    private void handleDashboard(MouseEvent event) {
        navigateTo(event,"/UsersInterface/Accueil.fxml", "AgroFlow - Accueil");
    }

    /**
     * Navigation vers Personnes
     */
    @FXML
    private void handlePersonnes(MouseEvent event) {
        navigateTo(event,"/UsersInterface/DahboardPersonne.fxml", "AgroFlow - Gestion du Personnel");
    }

    /**
     * Navigation vers Tâches
     */
    @FXML
    private void handleTaches(MouseEvent event) {
        navigateTo(event,"/UsersInterface/GestionTaches.fxml", "AgroFlow - Gestion des Tâches");
    }

    /**
     * Navigation vers Abonnements
     */
    @FXML
    private void handleAbonnements(MouseEvent event) {
        navigateTo(event,"/UsersInterface/GestionAbonnements.fxml", "AgroFlow - Gestion des Abonnements");
    }


    /**
     * Méthode générique de navigation
     */
    private void navigateTo(MouseEvent event,String fxmlPath, String title) {
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
    /**
     * Gérer la déconnexion
     */
    @FXML
    private void handleLogout() {
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


            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }

    /**
     * Afficher une erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher un succès
     */
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher une information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
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
}