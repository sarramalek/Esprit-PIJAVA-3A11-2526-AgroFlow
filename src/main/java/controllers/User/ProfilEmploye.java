package controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User.Personne;
import services.User.PersonneService;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Contrôleur pour le profil de l'employé
 * Permet de consulter et modifier ses informations personnelles
 */
public class ProfilEmploye {

    // ══════════════════════════════════════════════════════════════
    // FXML Components
    // ══════════════════════════════════════════════════════════════

    @FXML private TextField cinField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telField;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    @FXML private DatePicker dateNaissField;
    @FXML private PasswordField ancienMdpField;
    @FXML private PasswordField nouveauMdpField;
    @FXML private PasswordField confirmMdpField;
    @FXML private Button saveBtn;
    @FXML private Button desactiverBtn;
    @FXML private Button cancelBtn;
    @FXML private Label roleLabel;

    // ══════════════════════════════════════════════════════════════
    // Instance Variables
    // ══════════════════════════════════════════════════════════════

    private Personne currentUser;
    private PersonneService personneService;
    private AcceuilEmploye parentController;

    // ══════════════════════════════════════════════════════════════
    // Initialization
    // ══════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        System.out.println("✓ ProfilEmploye Controller initialisé");

        try {
            personneService = new PersonneService();
        } catch (Exception e) {
            System.err.println("✗ Erreur initialisation PersonneService");
            e.printStackTrace();
        }

        // Le CIN n'est pas modifiable
        if (cinField != null) {
            cinField.setEditable(false);
            cinField.setStyle("-fx-background-color: #F0F0F0;");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // User Management
    // ══════════════════════════════════════════════════════════════

    public void setCurrentUser(Personne user) {
        System.out.println("\n=== setCurrentUser appelé dans ProfilEmploye ===");

        if (user == null) {
            System.err.println("✗ ERREUR: user est NULL");
            return;
        }

        this.currentUser = user;

        System.out.println("✓ Utilisateur reçu:");
        System.out.println("  - CIN: " + user.getCin());
        System.out.println("  - Nom: " + user.getNom());
        System.out.println("  - Prénom: " + user.getPrenom());

        // Remplir les champs
        loadUserData();

        System.out.println("================================================\n");
    }

    // ══════════════════════════════════════════════════════════════
    // Load Data
    // ══════════════════════════════════════════════════════════════

    private void loadUserData() {
        if (currentUser == null) {
            System.err.println("⚠️ currentUser est NULL");
            return;
        }

        try {
            // Informations de base
            if (cinField != null) {
                cinField.setText(String.valueOf(currentUser.getCin()));
            }
            if (nomField != null) {
                nomField.setText(currentUser.getNom());
            }
            if (prenomField != null) {
                prenomField.setText(currentUser.getPrenom());
            }
            if (emailField != null) {
                emailField.setText(currentUser.getEmail());
            }
            if (telField != null) {
                telField.setText(currentUser.getTel());
            }
            if (adresseField != null) {
                adresseField.setText(currentUser.getAdresse());
            }
            if (villeField != null) {
                villeField.setText(currentUser.getVille());
            }
            if (roleLabel != null) {
                roleLabel.setText("👷 EMPLOYÉ");
            }

            // Date de naissance (si le champ existe)
            if (dateNaissField != null && currentUser.getDate_naiss() != null) {
                try {
                    // Supposant format yyyy-MM-dd
                    String[] parts = currentUser.getDate_naiss().split("-");
                    if (parts.length == 3) {
                        dateNaissField.setValue(java.time.LocalDate.of(
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2])
                        ));
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Erreur parsing date: " + e.getMessage());
                }
            }

            System.out.println("✓ Données utilisateur chargées");

        } catch (Exception e) {
            System.err.println("✗ Erreur chargement données");
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Action Handlers
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleSave() {
        System.out.println("💾 Sauvegarde du profil...");

        if (currentUser == null) {
            showError("Erreur", "Session expirée");
            return;
        }

        // Validation
        if (!validateFields()) {
            return;
        }

        try {
            // Mettre à jour les données
            currentUser.setNom(nomField.getText().trim());
            currentUser.setPrenom(prenomField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setTel(telField.getText().trim());
            currentUser.setAdresse(adresseField.getText().trim());
            currentUser.setVille(villeField.getText().trim());

            // Date de naissance
            if (dateNaissField != null && dateNaissField.getValue() != null) {
                currentUser.setDate_naiss(dateNaissField.getValue().toString());
            }

            // Mot de passe (si modifié)
            if (nouveauMdpField != null && !nouveauMdpField.getText().isEmpty()) {
                if (!handlePasswordChange()) {
                    return;
                }
            }

            // Sauvegarder en base
            personneService.modifier(currentUser);

            showSuccess("Succès", "Profil mis à jour avec succès !");
            System.out.println("✓ Profil sauvegardé");

            // Fermer la fenêtre
            handleFermer();

        } catch (SQLException e) {
            System.err.println("✗ Erreur sauvegarde");
            e.printStackTrace();
            showError("Erreur", "Impossible de sauvegarder le profil: " + e.getMessage());
        }
    }

    /**
     * Valider les champs
     */
    private boolean validateFields() {
        if (nomField.getText().trim().isEmpty()) {
            showError("Validation", "Le nom est obligatoire");
            return false;
        }
        if (prenomField.getText().trim().isEmpty()) {
            showError("Validation", "Le prénom est obligatoire");
            return false;
        }
        if (emailField.getText().trim().isEmpty()) {
            showError("Validation", "L'email est obligatoire");
            return false;
        }
        if (!emailField.getText().contains("@")) {
            showError("Validation", "Email invalide");
            return false;
        }

        // Validation mot de passe si modifié
        if (nouveauMdpField != null && !nouveauMdpField.getText().isEmpty()) {
            if (ancienMdpField.getText().isEmpty()) {
                showError("Validation", "Veuillez saisir l'ancien mot de passe");
                return false;
            }
            if (!nouveauMdpField.getText().equals(confirmMdpField.getText())) {
                showError("Validation", "Les mots de passe ne correspondent pas");
                return false;
            }
            if (nouveauMdpField.getText().length() < 6) {
                showError("Validation", "Le mot de passe doit contenir au moins 6 caractères");
                return false;
            }
        }

        return true;
    }

    /**
     * Gérer le changement de mot de passe
     */
    private boolean handlePasswordChange() {
        // Vérifier l'ancien mot de passe
        if (!currentUser.getMdp().equals(ancienMdpField.getText())) {
            showError("Erreur", "Ancien mot de passe incorrect");
            return false;
        }

        // Mettre à jour
        currentUser.setMdp(nouveauMdpField.getText());
        System.out.println("✓ Mot de passe mis à jour");
        return true;
    }

    @FXML
    private void handleFermer() {
        System.out.println("🚪 Fermeture du profil...");

        // Méthode robuste pour fermer la fenêtre
        Stage stage = null;

        // Essayer plusieurs composants pour obtenir le stage
        if (cancelBtn != null && cancelBtn.getScene() != null && cancelBtn.getScene().getWindow() != null) {
            stage = (Stage) cancelBtn.getScene().getWindow();
        } else if (saveBtn != null && saveBtn.getScene() != null && saveBtn.getScene().getWindow() != null) {
            stage = (Stage) saveBtn.getScene().getWindow();
        } else if (nomField != null && nomField.getScene() != null && nomField.getScene().getWindow() != null) {
            stage = (Stage) nomField.getScene().getWindow();
        }

        if (stage != null) {
            stage.close();
            System.out.println("✓ Fenêtre fermée");
        } else {
            System.err.println("⚠️ Impossible de fermer la fenêtre (stage est NULL)");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Alert Helpers
    // ══════════════════════════════════════════════════════════════

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDesactiver() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("⚠️ Désactiver votre compte");
        confirm.setContentText(
                "Cette action est irréversible.\n\n" +
                        "Votre compte et toutes vos données seront supprimés définitivement.\n\n" +
                        "Êtes-vous sûr de vouloir continuer ?"
        );

        // Boutons personnalisés
        ButtonType btnSupprimer = new ButtonType("Oui, supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnuler   = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnSupprimer, btnAnnuler);

        confirm.showAndWait().ifPresent(response -> {
            if (response == btnSupprimer) {
                try {
                    personneService.supprimer(currentUser.getCin());
                    System.out.println("✓ Compte supprimé: " + currentUser.getCin());

                    // Fermer cette fenêtre
                    Stage stage = (Stage) desactiverBtn.getScene().getWindow();
                    stage.close();

                    // Retourner à la page de login
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                    Parent root = loader.load();
                    Stage loginStage = new Stage();
                    loginStage.setScene(new Scene(root, 900, 600));
                    loginStage.setTitle("AgroFlow - Connexion");
                    loginStage.show();

                    // Fermer le dashboard agricole
                    if (parentController != null) {
                        Stage dashStage = parentController.getStage();
                        if (dashStage != null) dashStage.close();
                    }

                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                    showError("Erreur lors de la suppression du compte","error");
                }
            }
        });
    }

}