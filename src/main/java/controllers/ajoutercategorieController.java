package controllers;

import entities.Categorie;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.CategorieService;
import java.io.IOException;
import java.sql.SQLException;

public class ajoutercategorieController {

    // --- ÉLÉMENTS INTERFACE (liés au fichier FXML via fx:id) ---
    @FXML private TextField tfNom; // Champ de saisie pour le nom
    @FXML private TextArea taDescription; // Champ de saisie pour la description
    @FXML private Label lblTitre, msgNom, msgDescription; // Titre dynamique et messages d'erreur/succès

    // --- SERVICES ET VARIABLES D'ÉTAT ---
    private final CategorieService catService = new CategorieService(); // Service pour interagir avec la DB
    private boolean isModification = false; // Drapeau pour savoir si on AJOUTE ou si on MODIFIE
    private int idCategorieActuel; // Stocke l'ID en cas de modification

    /**
     * initialize() : S'exécute automatiquement après le chargement du FXML.
     * C'est ici qu'on prépare le comportement de la fenêtre.
     */
    @FXML
    public void initialize() {
        // Si c'est un nouvel ajout (pas une modif), on affiche les alertes rouges dès le début
        if (!isModification) {
            afficherFeedback(msgNom, "⚠️ Veuillez remplir le nom (min 3 car.)", true);
            afficherFeedback(msgDescription, "⚠️ Veuillez remplir la description (min 5 car.)", true);
        }

        // On active les "Listeners" (écouteurs) pour surveiller ce que l'utilisateur tape
        ajouterEcouteurs();
    }

    /**
     * afficherFeedback : Gère le texte et la couleur des labels de validation.
     * @param estErreur : Si vrai -> rouge, si faux -> vert.
     */
    private void afficherFeedback(Label label, String texte, boolean estErreur) {
        label.setText(texte);
        // Utilisation du CSS en ligne pour changer la couleur dynamiquement
        label.setStyle(estErreur ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
    }

    /**
     * ajouterEcouteurs : Surveille chaque frappe au clavier dans les champs.
     */
    private void ajouterEcouteurs() {
        // Validation du Nom pendant que l'utilisateur tape
        tfNom.textProperty().addListener((obs, old, newValue) -> {
            String val = newValue.trim();
            if (val.isEmpty()) {
                afficherFeedback(msgNom, "⚠️ Le nom est obligatoire", true);
            } else if (val.length() < 3) {
                afficherFeedback(msgNom, "⚠️ Trop court (min 3 car.)", true);
            } else {
                afficherFeedback(msgNom, "✅ Nom valide", false); // Devient vert
            }
        });

        // Validation de la Description pendant que l'utilisateur tape
        taDescription.textProperty().addListener((obs, old, newValue) -> {
            String val = newValue.trim();
            if (val.isEmpty()) {
                afficherFeedback(msgDescription, "⚠️ La description est obligatoire", true);
            } else if (val.length() < 5) {
                afficherFeedback(msgDescription, "⚠️ Trop courte (min 5 car.)", true);
            } else {
                afficherFeedback(msgDescription, "✅ Description valide", false); // Devient vert
            }
        });
    }

    /**
     * preparerModification : Appelée depuis la liste des catégories pour
     * passer ce contrôleur en mode "Mise à jour".
     */
    public void preparerModification(Categorie c) {
        isModification = true; // On change l'état
        lblTitre.setText("Modifier la Catégorie"); // On change le titre de la fenêtre
        idCategorieActuel = c.getId(); // On garde l'ID pour savoir quelle ligne modifier en DB
        tfNom.setText(c.getNom()); // On remplit le champ avec le nom actuel
        taDescription.setText(c.getDescription()); // On remplit avec la description actuelle

        // On valide immédiatement les données chargées pour afficher les labels en vert
        if (c.getNom().length() >= 3) afficherFeedback(msgNom, "✅ Nom valide", false);
        if (c.getDescription().length() >= 5) afficherFeedback(msgDescription, "✅ Description valide", false);
    }

    /**
     * validerAjout : Action déclenchée par le bouton de validation.
     */
    @FXML
    void validerAjout(ActionEvent event) {
        String nom = tfNom.getText().trim();
        String desc = taDescription.getText().trim();

        // 1. Double sécurité : On vérifie les longueurs avant de toucher à la DB
        if (nom.length() < 3 || desc.length() < 5) {
            afficherAlerte(Alert.AlertType.WARNING, "Format invalide", "Veuillez respecter les contraintes.");
            return;
        }

        try {
            // 2. Vérification de l'unicité du nom (Pour éviter les doublons)
            if (catService.existeDeja(nom) && !isModification) {
                afficherFeedback(msgNom, "❌ Ce nom de catégorie existe déjà !", true);
                return;
            }

            // 3. Création de l'objet Categorie (ID=0 si ajout, ID réel si modif)
            Categorie c = new Categorie(isModification ? idCategorieActuel : 0, nom, desc);

            // 4. Appel de la méthode correspondante du Service
            if (isModification) {
                catService.modifier(c);
            } else {
                catService.ajouter(c);
            }

            // 5. Retour automatique à la liste
            retourListe(event);

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            afficherAlerte(Alert.AlertType.ERROR, "Erreur Système", "Problème d'accès à la base de données.");
        }
    }

    /**
     * retourListe : Change de scène pour revenir à l'affichage de la table.
     */
    @FXML
    void retourListe(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/affichercategorie.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    // Fonction utilitaire pour afficher des Pop-up JavaFX
    private void afficherAlerte(Alert.AlertType type, String titre, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}