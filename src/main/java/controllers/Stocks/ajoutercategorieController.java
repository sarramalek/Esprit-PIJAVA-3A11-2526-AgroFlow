package controllers.Stocks;

import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.Stocks.Categorie;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.Stocks.CategorieService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ajoutercategorieController {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer ;
    @FXML private TextField tfNom;
    @FXML private TextArea taDescription;
    @FXML private Label lblTitre, msgNom, msgDescription;

    private final CategorieService catService = new CategorieService();
    private boolean isModification = false;
    private int idCategorieActuel;

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
        // Validation immédiate au démarrage pour guider l'utilisateur
        if (!isModification) {
            afficherFeedback(msgNom, "⚠️ Veuillez remplir le nom (min 3 car.)", true);
            afficherFeedback(msgDescription, "⚠️ Veuillez remplir la description (min 5 car.)", true);
        }

        ajouterEcouteurs();
    }

    private void afficherFeedback(Label label, String texte, boolean estErreur) {
        label.setText(texte);
        label.setStyle(estErreur ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    private void ajouterEcouteurs() {
        // Validation du Nom (min 3)
        tfNom.textProperty().addListener((obs, old, newValue) -> {
            String val = newValue.trim();
            if (val.isEmpty()) {
                afficherFeedback(msgNom, "⚠️ Le nom est obligatoire", true);
            } else if (val.length() < 3) {
                afficherFeedback(msgNom, "⚠️ Trop court (min 3 car.)", true);
            } else {
                afficherFeedback(msgNom, "✅ Nom valide", false);
            }
        });

        // Validation de la Description (min 5)
        taDescription.textProperty().addListener((obs, old, newValue) -> {
            String val = newValue.trim();
            if (val.isEmpty()) {
                afficherFeedback(msgDescription, "⚠️ La description est obligatoire", true);
            } else if (val.length() < 5) {
                afficherFeedback(msgDescription, "⚠️ Trop courte (min 5 car.)", true);
            } else {
                afficherFeedback(msgDescription, "✅ Description valide", false);
            }
        });
    }

    public void preparerModification(Categorie c) {
        isModification = true;
        lblTitre.setText("Modifier la Catégorie");
        idCategorieActuel = c.getId();
        tfNom.setText(c.getNom());
        taDescription.setText(c.getDescription());

        // Validation instantanée des données chargées
        if (c.getNom().length() >= 3) afficherFeedback(msgNom, "✅ Nom valide", false);
        if (c.getDescription().length() >= 5) afficherFeedback(msgDescription, "✅ Description valide", false);
    }

    @FXML
    void validerAjout(ActionEvent event) {
        String nom = tfNom.getText().trim();
        String desc = taDescription.getText().trim();

        // 1. Validation des longueurs minimales
        if (nom.length() < 3 || desc.length() < 5) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Format invalide");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez respecter les contraintes :\n- Nom : 3 caractères\n- Description : 5 caractères");
            alert.show();
            return;
        }

        try {
            // 2. Vérification de l'unicité (uniquement pour un nouvel ajout ou si le nom a changé en modification)
            // Note: On suppose que idCategorieActuel est 0 pour un nouvel ajout
            if (catService.existeDeja(nom) && !isModification) {
                afficherFeedback(msgNom, "❌ Ce nom de catégorie existe déjà !", true);
                return;
            }

            // 3. Procéder à l'enregistrement
            Categorie c = new Categorie(isModification ? idCategorieActuel : 0, nom, desc);

            if (isModification) {
                catService.modifier(c);
            } else {
                catService.ajouter(c);
            }

            retourListe(event);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur Système", "Une erreur est survenue lors de l'accès à la base de données.");
        }
    }

    @FXML
    void retourListe(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/StocksInterface/affichercategorie.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    //navigation vers les autres modules
    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.changerScene(event,"/UsersInterface/DahboardPersonne.fxml");}


    @FXML private void handleTaches(Event event ) { /* Charger vue Tâches */
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

    public void handleDashboard(MouseEvent actionEvent) throws IOException {
        this.changerScene(actionEvent, "/UsersInterface/Acceuil.fxml");

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
    private void changerScene(Event event, String fxmlFile) {
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

}