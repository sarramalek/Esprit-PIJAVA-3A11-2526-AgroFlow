package controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User.Admin;
import models.User.Employe;
import models.User.Personne;
import models.User.Utilisateur;
import services.User.PersonneService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la page d'inscription (SignUp)
 * Adapté pour utiliser PersonneService
 */
public class SignUp implements Initializable {

    // ═══════════════════════════════════════════════════════
    // FXML ELEMENTS
    // ═══════════════════════════════════════════════════════

    // Toggle buttons pour les rôles
    @FXML private ToggleButton adminToggle;
    @FXML private ToggleButton agricoleToggle;
    @FXML private ToggleButton employeToggle;

    // Champs du formulaire
    @FXML private TextField cinField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField telField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;

    // Boutons
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    // Label d'erreur
    @FXML private Label errorLabel;

    // Lien de connexion
    @FXML private Hyperlink signinLink;

    // ═══════════════════════════════════════════════════════
    // SERVICES
    // ═══════════════════════════════════════════════════════

    private PersonneService personneService;
    private ToggleGroup roleToggleGroup;
    private int selectedRole = 1; // 1=Utilisateur (Agricole), 2=Employé, 3=Admin

    // ═══════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        personneService = new PersonneService();

        // Configurer le ToggleGroup pour les rôles
        setupRoleToggles();

        // Ajouter des listeners de validation
        setupValidationListeners();

        // Masquer le message d'erreur au départ
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Configure les toggle buttons pour la sélection du rôle
     */
    private void setupRoleToggles() {
        roleToggleGroup = new ToggleGroup();

        adminToggle.setToggleGroup(roleToggleGroup);
        agricoleToggle.setToggleGroup(roleToggleGroup);
        employeToggle.setToggleGroup(roleToggleGroup);

        // Agricole (Utilisateur) sélectionné par défaut
        agricoleToggle.setSelected(true);
        selectedRole = 1; // Utilisateur

        // Gérer les changements de style
        roleToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                updateToggleStyles();

                // Mettre à jour le rôle sélectionné
                if (newToggle == adminToggle) {
                    selectedRole = 3; // Admin
                } else if (newToggle == agricoleToggle) {
                    selectedRole = 1; // Utilisateur (Agricole)
                } else if (newToggle == employeToggle) {
                    selectedRole = 2; // Employé
                }
            }
        });

        updateToggleStyles();
    }

    /**
     * Met à jour les styles des toggle buttons
     */
    private void updateToggleStyles() {
        // Style pour bouton non sélectionné
        String unselectedStyle = "-fx-background-color: transparent; " +
                "-fx-text-fill: #558B2F; " +
                "-fx-font-size: 13px; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: transparent;";

        // Style pour bouton sélectionné
        String selectedStyle = "-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: transparent; " +
                "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.45), 10, 0, 0, 2);";

        adminToggle.setStyle(adminToggle.isSelected() ? selectedStyle : unselectedStyle);
        agricoleToggle.setStyle(agricoleToggle.isSelected() ? selectedStyle : unselectedStyle);
        employeToggle.setStyle(employeToggle.isSelected() ? selectedStyle : unselectedStyle);
    }

    /**
     * Configure les listeners de validation en temps réel
     */
    private void setupValidationListeners() {
        // Validation CIN (8 chiffres)
        cinField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                cinField.setText(oldVal);
            }
            if (newVal.length() > 8) {
                cinField.setText(newVal.substring(0, 8));
            }
        });

        // Validation téléphone (nombre uniquement)
        telField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                telField.setText(oldVal);
            }
            if (newVal.length() > 15) {
                telField.setText(newVal.substring(0, 15));
            }
        });

        // Limiter la date de naissance (majeur uniquement)
        dateNaissancePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate maxDate = LocalDate.now().minusYears(18);
                setDisable(empty || date.isAfter(maxDate));
            }
        });
    }

    // ═══════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════

    /**
     * Valide tous les champs du formulaire
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // CIN
        if (cinField.getText() == null || cinField.getText().trim().isEmpty()) {
            errors.append("• Le CIN est obligatoire\n");
        } else if (cinField.getText().length() != 8) {
            errors.append("• Le CIN doit contenir exactement 8 chiffres\n");
        } else {
            try {
                int cin = Integer.parseInt(cinField.getText());
                if (personneService.rechercherParId(cin) != null) {
                    errors.append("• Ce CIN est déjà enregistré\n");
                }
            } catch (SQLException e) {
                errors.append("• Erreur lors de la vérification du CIN\n");
                e.printStackTrace();
            } catch (NumberFormatException e) {
                errors.append("• Le CIN doit être un nombre valide\n");
            }
        }

        // Nom
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errors.append("• Le nom est obligatoire\n");
        } else if (nomField.getText().length() < 2) {
            errors.append("• Le nom doit contenir au moins 2 caractères\n");
        }

        // Prénom
        if (prenomField.getText() == null || prenomField.getText().trim().isEmpty()) {
            errors.append("• Le prénom est obligatoire\n");
        } else if (prenomField.getText().length() < 2) {
            errors.append("• Le prénom doit contenir au moins 2 caractères\n");
        }

        // Email
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            errors.append("• L'email est obligatoire\n");
        } else if (!isValidEmail(emailField.getText())) {
            errors.append("• L'email n'est pas valide\n");
        }

        // Mot de passe
        if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
            errors.append("• Le mot de passe est obligatoire\n");
        } else if (passwordField.getText().length() < 6) {
            errors.append("• Le mot de passe doit contenir au moins 6 caractères\n");
        }

        // Téléphone
        if (telField.getText() == null || telField.getText().trim().isEmpty()) {
            errors.append("• Le téléphone est obligatoire\n");
        } else if (telField.getText().length() < 8) {
            errors.append("• Le numéro de téléphone doit contenir au moins 8 chiffres\n");
        }

        // Date de naissance (optionnelle mais si présente, doit être valide)
        if (dateNaissancePicker.getValue() != null) {
            LocalDate maxDate = LocalDate.now().minusYears(18);
            if (dateNaissancePicker.getValue().isAfter(maxDate)) {
                errors.append("• Vous devez avoir au moins 18 ans\n");
            }
        }

        if (errors.length() > 0) {
            showError(errors.toString());
            return false;
        }

        hideError();
        return true;
    }

    /**
     * Valide le format d'un email
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    // ═══════════════════════════════════════════════════════
    // EVENT HANDLERS
    // ═══════════════════════════════════════════════════════

    /**
     * Gère l'enregistrement de l'utilisateur
     */
    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            // Créer l'objet Personne approprié selon le rôle
            Personne personne;
            switch (selectedRole) {
                case 3: // Admin
                    personne = new Admin();
                    break;
                case 2: // Employé
                    personne = new Employe();
                    break;
                case 1: // Utilisateur (Agricole)
                default:
                    personne = new Utilisateur();
                    break;
            }

            // Remplir les données
            personne.setCin(Integer.parseInt(cinField.getText().trim()));
            personne.setNom(nomField.getText().trim());
            personne.setPrenom(prenomField.getText().trim());
            personne.setEmail(emailField.getText().trim());
            personne.setMdp(passwordField.getText()); // TODO: Hash le password
            personne.setTel(telField.getText().trim());

            // Date de naissance
            if (dateNaissancePicker.getValue() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                personne.setDate_naiss(dateNaissancePicker.getValue().format(formatter));
            } else {
                personne.setDate_naiss(null);
            }

            personne.setAdresse(adresseField.getText() != null ? adresseField.getText().trim() : "");
            personne.setVille(villeField.getText() != null ? villeField.getText().trim() : "");

            // Dates de création et dernier changement
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            personne.setDate_creationcpt(now.format(formatter));
            personne.setDate_dernierchg(now.format(formatter));

            // Enregistrer dans la base de données
            personneService.ajouter(personne);

            showSuccess("Inscription réussie ! Vous pouvez maintenant vous connecter.");

            // Attendre 2 secondes puis rediriger vers la page de connexion
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::redirectToLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de l'inscription : " + e.getMessage());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            showError("Erreur : Le CIN doit être un nombre valide.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur technique : " + e.getMessage());
        }
    }

    /**
     * Gère l'annulation
     */
    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer l'annulation");
        alert.setHeaderText("Annuler l'inscription ?");
        alert.setContentText("Toutes les informations saisies seront perdues.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                redirectToLogin();
            }
        });
    }

    /**
     * Gère le clic sur "Se connecter"
     */
    @FXML
    private void handleSignup() {
        redirectToLogin();
    }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════

    /**
     * Redirige vers la page de connexion
     */
    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) saveButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Connexion");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible de charger la page de connexion.");
        }
    }

    // ═══════════════════════════════════════════════════════
    // UI HELPERS
    // ═══════════════════════════════════════════════════════

    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #C62828; " +
                "-fx-font-size: 13px; " +
                "-fx-background-color: #FFEBEE; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 10 14;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Affiche un message de succès
     */
    private void showSuccess(String message) {
        errorLabel.setText("✓ " + message);
        errorLabel.setStyle("-fx-text-fill: #2E7D32; " +
                "-fx-font-size: 13px; " +
                "-fx-background-color: #E8F5E9; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 10 14;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Masque le message d'erreur
     */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Réinitialise le formulaire
     */
    private void clearForm() {
        cinField.clear();
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        telField.clear();
        dateNaissancePicker.setValue(null);
        adresseField.clear();
        villeField.clear();
        agricoleToggle.setSelected(true);
        selectedRole = 1;
        hideError();
    }
}