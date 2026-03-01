package controllers.Animaux;

import controllers.User.AcceuilAgricole;
import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
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
import models.Animaux.examens;
import models.User.Personne;
import services.Animaux.ServiceAnimal;
import services.Animaux.ServiceExamen;
import services.PdfService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static controllers.User.GestionAbonnements.showInfo;

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
    @FXML private Label welcomeNameLabel;
    @FXML private Hyperlink aproposLink;

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    private Personne currentUser;
    //image useer
    @FXML private ImageView sidebarAvatarImageView;
    @FXML private Label     sidebarAvatarDefault;
    @FXML private Circle sidebarAvatarBg;
    @FXML private TextField filterField;
    @FXML private TableColumn<animaux, String> colAvatar;

    private ServiceAnimal service = new ServiceAnimal();

    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ currentUser chargé depuis SessionManager: " + currentUser.getNom());
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }
        chargerSidebarAvatar(SessionManager.getCurrentUser());

        System.out.println("✓ AcceuilAgricole Controller initialisé");
        // Cacher submenu par défaut
        if (gestionSubmenu != null) {
            gestionSubmenu.setVisible(false);
            gestionSubmenu.setManaged(false);
        }

        // ✅
        if (gestionBtn != null) {
            gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
        }
        if (gestionContainer != null) {
            gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
            gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());
        }
        // Liaison de TOUTES les colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colEspece.setCellValueFactory(new PropertyValueFactory<>("espece"));
        colPoids.setCellValueFactory(new PropertyValueFactory<>("poids"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date_naissance"));
        colSexe.setCellValueFactory(new PropertyValueFactory<>("sexe"));

        // --- CONFIGURATION PIXABAY / IMAGE REELLE ---
        colAvatar.setCellValueFactory(new PropertyValueFactory<>("espece"));
        colAvatar.setCellFactory(column -> {
            return new TableCell<animaux, String>() {
                private final ImageView imageView = new ImageView();

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        animaux animal = getTableRow().getItem();
                        String espece = animal.getEspece().toLowerCase().trim();

                        // Traduction rapide pour l'API pour être sûr d'avoir les bonnes photos
                        String search = espece;
                        if(espece.contains("vache") || espece.contains("bovin")) search = "cow,farm";
                        if(espece.contains("poule") || espece.contains("volaille")) search = "chicken,hen";
                        if(espece.contains("chien")) search = "dog";
                        if(espece.contains("chat")) search = "cat";

                        // API Source Unsplash (recherche par mot-clé)
                        String imageUrl = "https://loremflickr.com/100/100/" + search + "/all";

                        Image img = new Image(imageUrl, 50, 50, true, true, true);
                        imageView.setImage(img);

                        // Style pour rendre l'image plus propre (optionnel)
                        imageView.setFitWidth(50);
                        imageView.setFitHeight(50);

                        setGraphic(imageView);
                    }
                }
            };
        });
        refreshTable();
    }



    private void refreshTable() {
        try {
            List<animaux> list = service.afficher();
            ObservableList<animaux> observableList = FXCollections.observableArrayList(list);
            setupSearch(observableList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void setupSearch(ObservableList<animaux> animalList) {
        FilteredList<animaux> filteredData = new FilteredList<>(animalList, p -> true);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(animal -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return animal.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        animal.getEspece().toLowerCase().contains(lowerCaseFilter);
            });
        });
        SortedList<animaux> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableAnimaux.comparatorProperty());
        tableAnimaux.setItems(sortedData);
    }

    @FXML
    void handleGenererPDF(ActionEvent event) {
        animaux selectionne = tableAnimaux.getSelectionModel().getSelectedItem();
        if (selectionne != null) {
            try {
                ServiceExamen sEx = new ServiceExamen();
                List<examens> historique = sEx.afficher().stream()
                        .filter(e -> e.getId_animal() == selectionne.getId())
                        .collect(Collectors.toList());
                new PdfService().genererCarnetSante(selectionne, historique);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Le carnet de santé de " + selectionne.getNom() + " a été généré !");
                alert.show();
            } catch (SQLException e) { e.printStackTrace(); }
        } else { alerteSelection(); }
    }

    @FXML
    void handleGenererCouples(ActionEvent event) {
        animaux selection = tableAnimaux.getSelectionModel().getSelectedItem();
        if (selection != null) {
            List<animaux> partenaires = service.trouverPartenaires(selection);
            if (partenaires.isEmpty()) {
                afficherAlerte("Aucun partenaire trouvé pour " + selection.getNom());
            } else {
                String liste = partenaires.stream()
                        .map(a -> a.getNom() + " (ID: " + a.getId() + ")")
                        .collect(Collectors.joining("\n"));
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Partenaires Potentiels");
                alert.setContentText(liste);
                alert.show();
            }
        } else { afficherAlerte("Veuillez d'abord sélectionner un animal !"); }
    }

    private void afficherAlerte(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(message);
        alert.show();
    }


        @FXML void versAjout(MouseEvent event) { changerScene(event, "/AnimalsInterface/ajoutAnimaux.fxml"); }

    @FXML
    void ouvrirStats(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/AnimalsInterface/StatsAnimaux.fxml")));
            Stage stage = new Stage();
            stage.setTitle("Statistiques - AgroFlow");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    void ouvrirSuggestions(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AnimalsInterface/SuggestionFood.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Aide à l'alimentation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
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
    void naviguerVersExamens( MouseEvent event) {
        try {
            // Le nom du fichier doit être EXACT (attention aux majuscules)
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/AnimalsInterface/AfficherExamens.fxml")));

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
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/AnimalsInterface/acceuilagricoleexamens.fxml")));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    @FXML void naviguerAnimaux(MouseEvent event) { changerScene(event, "/AnimalsInterfac/AfficherAnimaux.fxml"); }

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
    private void handleLogoutAgricole () {
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
    // navigation Front Office Agricole
    private void chargerSidebarAvatar(Personne user) {
        if (user == null) return;

        // Nom et rôle
        if (userNameLabel != null)
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());

        // Clip circulaire appliqué en Java (radius=35, centre=35,35 pour fitWidth/Height=70)
        if (sidebarAvatarImageView != null) {
            Circle clip = new Circle(35, 35, 35);
            sidebarAvatarImageView.setClip(clip);
        }

        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) return;

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 70, 70, false, true, true);
                Platform.runLater(() -> {
                    if (!image.isError()) {
                        sidebarAvatarImageView.setImage(image);
                        sidebarAvatarImageView.setVisible(true);
                        sidebarAvatarImageView.setManaged(true);
                        sidebarAvatarDefault.setVisible(false);
                        if (sidebarAvatarBg != null) sidebarAvatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("⚠️ Avatar sidebar : " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    @FXML
    void ouvrirTerrains(MouseEvent event) {
        chargerPage(event, "/TerrainsInterface/agricoleaffichageterrain.fxml", "Gestion des Terrains");
    }

    @FXML
    void ouvrirPlantes(MouseEvent event) {
        chargerPage(event, "/TerrainsInterface/agricoleaffichageplante.fxml", "Liste des Plantes");
    }

    @FXML
    void ouvrirRotations(MouseEvent event) {
        chargerPage(event, "/TerrainsInterface/agricoleaffichagerotation.fxml", "Gestion des Rotations");
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
        }
    }
    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleDashboardAgricole(MouseEvent event)    { navigateTo(event,"/UsersInterface/AcceuillAgr.fxml","Dashboard"); }
    @FXML private void handleMesTerrains(MouseEvent mouseEvent)  {         this.changerScene(mouseEvent,"/TerrainsInterface/acceuilagricoleterrain.fxml");
    }
    @FXML private void handleMesAnimaux(MouseEvent mouseEvent)   {         this.changerScene(mouseEvent,"/AnimalsInterface/acceuilagricoleanimaux.fxml");
    }
    @FXML private void handleMesArticles(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherArticleAgr.fxml","Articles"); }
    @FXML private void handleMesCatégories(MouseEvent mouseEvent)   {         navigateTo(mouseEvent,"/StocksInterface/AfficherCategorieAgr.fxml","Catégories "); }    @FXML private void handleMonMateriel(MouseEvent mouseEvent)  {         navigateTo(mouseEvent,"/MaterielsInterface/AgricoleAffichageMachine.fxml","Materiels");
        ; }
    @FXML private void handleMonProfil(MouseEvent event )    { try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
        Parent root = loader.load();
        ProfilEmploye ctrl = loader.getController();
        if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
        Stage s = new Stage();
        s.setTitle("Mon Profil"); s.setScene(new Scene(root));
        s.setResizable(true); s.initModality(Modality.APPLICATION_MODAL);
        s.centerOnScreen(); s.showAndWait();
    } catch (IOException e) { showError("Erreur"+ e.getMessage()); } }

    // ✓ CORRECT
    @FXML
    private void handleMonAbonnement(MouseEvent event) {
        System.out.println("💳 Ouverture Mon Abonnement...");
        navigateTo(event,"/UsersInterface/MesAbonnements.fxml","Mes Abonnements");
    }
    // ── Logout ────────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(AcceuilAgricole.class.getResource("/UsersInterface/login.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) logoutBtn.getScene().getWindow();
                    stage.setScene(new Scene(root, 1500, 700));
                    stage.setTitle("AgroFlow - Connexion");
                    stage.setMaximized(true);
                    System.out.println("✓ Déconnexion réussie");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showGestionSubmenu() {
        if (gestionSubmenu != null) { gestionSubmenu.setVisible(true);  gestionSubmenu.setManaged(true); }
    }
    public void hideGestionSubmenu() {
        if (gestionSubmenu != null) { gestionSubmenu.setVisible(false); gestionSubmenu.setManaged(false); }
    }
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Ajouter cette méthode getStage() pour ProfilAgricole
    public Stage getStage() {
        if (logoutBtn != null && logoutBtn.getScene() != null)
            return (Stage) logoutBtn.getScene().getWindow();
        return null;
    }

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("✓ setCurrentUser appelé pour: " + user.getNom());

            if (userNameLabel != null)
                userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            else
                System.err.println("✗ userNameLabel est NULL !");

            if (welcomeNameLabel != null)
                welcomeNameLabel.setText(user.getPrenom() + " !");
            else
                System.err.println("✗ welcomeNameLabel est NULL !");

            if (userRoleLabel != null)
                userRoleLabel.setText("🌾 AGRICULTEUR");


        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL !");
        }
    }

    // Ajouter cette méthode handleAPropos()

    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
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
     * Transfère l'utilisateur courant au contrôleur cible via réflexion
     */
    private void transferUserToController(Object controller) {
        try {
            controller.getClass()
                    .getMethod("setCurrentUser", Personne.class)
                    .invoke(controller, currentUser);
            System.out.println("✓ Utilisateur transféré au contrôleur");
        } catch (NoSuchMethodException e) {
            System.out.println("ℹ Le contrôleur n'a pas de méthode setCurrentUser()");
        } catch (Exception e) {
            System.err.println("✗ Erreur lors du transfert utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gère les erreurs de navigation de manière appropriée
     */
    private void handleNavigationError(String fxmlPath, String title, IOException e) {
        e.printStackTrace();

        // Vérifier si c'est un fichier manquant ou une autre erreur
        if (e.getMessage() != null && e.getMessage().contains("Location is not set")) {
            showInfo("Module à venir",
                    "Le module \"" + title + "\" sera disponible prochainement.");
        } else {
            // Erreur réelle
            showError("Erreur de chargement\n\n" +
                    "Impossible de charger " + title + ".\n" +
                    "Détails: " + e.getMessage());
        }
    }


    public void versAjoutAgr(MouseEvent mouseEvent) {
        changerScene(mouseEvent, "/AnimalsInterface/AjoutAnimauxAgric.fxml");
    }
    @FXML
    void versModifierAgr(ActionEvent event) {
        // 1. On récupère l'animal sélectionné dans la TableView
        animaux selectionne = tableAnimaux.getSelectionModel().getSelectedItem();

        if (selectionne != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AnimalsInterface/ModifierAnimauxAgric.fxml"));
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
}