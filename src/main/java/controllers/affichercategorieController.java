package controllers;

// Importations des composants JavaFX et des classes métiers
import entities.Categorie;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import services.CategorieService;
import java.io.IOException;
import java.sql.SQLException;

public class affichercategorieController {

    // --- ÉLÉMENTS INTERFACE (FX:ID) ---
    @FXML private TableView<Categorie> tableCategories; // Le conteneur principal du tableau
    @FXML private TableColumn<Categorie, String> colNom, colDescription; // Les deux colonnes de données

    // Instance du service pour communiquer avec la base de données MySQL
    private final CategorieService catService = new CategorieService();

    /**
     * initialize() : Méthode lancée automatiquement dès que la vue FXML est chargée.
     */
    @FXML
    public void initialize() {
        // 1. Liaison des colonnes : On dit à JavaFX d'aller chercher "nom" et "description"
        // via les getters (getNom, getDescription) de ta classe entité Categorie.
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // 2. Stylisation des lignes (RowFactory) :
        // On définit l'apparence visuelle de chaque ligne du tableau.
        tableCategories.setRowFactory(tv -> new TableRow<Categorie>() {
            @Override
            protected void updateItem(Categorie item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Couleur beige (#fdfae7) et bordure de séparation en bas pour le design
                    setStyle("-fx-background-color: #fdfae7; -fx-border-color: #dcdde1; -fx-border-width: 0 0 1 0;");
                }
            }
        });

        // Texte par défaut si le tableau est vide
        tableCategories.setPlaceholder(new Label("Aucune catégorie enregistrée"));

        // Chargement initial des données depuis MySQL
        chargerDonnees();
    }

    /**
     * chargerDonnees() : Récupère la liste des catégories via le service
     * et l'injecte dans le tableau JavaFX.
     */
    private void chargerDonnees() {
        try {
            // On transforme la List de Java en ObservableList pour que JavaFX puisse l'afficher
            tableCategories.setItems(FXCollections.observableArrayList(catService.recuperer()));
        } catch (SQLException e) {
            e.printStackTrace(); // Affiche l'erreur en console si la DB ne répond pas
        }
    }

    // --- NAVIGATION BARRE LATÉRALE ---
    // Ces méthodes permettent de naviguer entre les différentes pages de ton application

    @FXML void allerVersArticles(ActionEvent event) throws IOException {
        changerScene(event, "/afficherarticle.fxml");
    }

    @FXML void allerVersCategories(ActionEvent event) {
        // On ne fait rien car l'utilisateur est déjà sur cette page
    }

    @FXML void deconnexion(ActionEvent event) throws IOException {
        changerScene(event, "/login.fxml");
    }

    /**
     * changerScene() : Méthode utilitaire pour éviter de répéter le code de changement de fenêtre.
     */
    private void changerScene(ActionEvent event, String fxml) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    // --- ACTIONS DE GESTION (CRUD) ---

    /**
     * ouvrirFormulaireAjout() : Redirige vers la page de création d'une catégorie.
     */
    @FXML void ouvrirFormulaireAjout(ActionEvent event) throws IOException {
        changerScene(event, "/ajoutercategorie.fxml");
    }

    /**
     * modifierCategorie() : Gère le passage en mode modification.
     */
    @FXML void modifierCategorie(ActionEvent event) throws IOException {
        // 1. On récupère la catégorie que l'utilisateur a sélectionné dans le tableau
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();

        if (selected != null) {
            // 2. On charge manuellement le fichier FXML du formulaire
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajoutercategorie.fxml"));
            Parent root = loader.load();

            // 3. On récupère le contrôleur de la page de destination
            ajoutercategorieController controller = loader.getController();

            // 4. On appelle la méthode "preparerModification" pour remplir les champs de texte avec les infos
            controller.preparerModification(selected);

            // 5. On affiche la nouvelle scène
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } else {
            // Optionnel : Alerter l'utilisateur qu'il doit sélectionner une ligne
            System.out.println("Veuillez sélectionner une catégorie à modifier.");
        }
    }

    /**
     * supprimerCategorie() : Supprime la catégorie sélectionnée après vérification.
     */
    @FXML void supprimerCategorie(ActionEvent event) {
        // Récupération de l'élément sélectionné
        Categorie selected = tableCategories.getSelectionModel().getSelectedItem();

        if (selected != null) {
            try {
                // Appel au service pour exécuter la requête DELETE
                catService.supprimer(selected.getId());
                // On rafraîchit le tableau immédiatement
                chargerDonnees();
            } catch (SQLException e) {
                // TRÈS IMPORTANT : Gestion de l'intégrité référentielle.
                // Si la catégorie est liée à des articles en base de données,
                // MySQL empêchera la suppression (Erreur de clé étrangère).
                new Alert(Alert.AlertType.ERROR, "Erreur : Cette catégorie est liée à des articles !").show();
            }
        }
    }
}