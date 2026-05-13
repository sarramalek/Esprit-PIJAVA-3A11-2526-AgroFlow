package controllers.User;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.User.Employe;
import services.User.PersonneService;

import java.sql.SQLException;
import java.time.LocalDate;

public class AjoutPersonne {
    //sub menu
    @FXML private VBox gestionSubmenu, operationsSubmenu,gestionContainer;
    @FXML private Button gestionToggle, operationsToggle;
    @FXML private Button gestionBtn;

    @FXML private TextField cinField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField telField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private PersonneService personneService;
    private DashboardPersonnes dashboardController;
    private boolean ajoutReussi = false;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        try {
            personneService = new PersonneService();
            System.out.println("✓ AjouterEmployeController initialisé");
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'initialisation");
            e.printStackTrace();
            showError("Erreur de connexion à la base de données");
        }

        // Validation en temps réel pour le CIN (uniquement des chiffres)
        cinField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                cinField.setText(oldValue);
            }
        });

        // Validation pour le téléphone
        telField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                telField.setText(oldValue);
            }
        });
    }

    /**
     * Définir le contrôleur du dashboard parent
     */
    public void setDashboardController(DashboardPersonnes controller) {
        this.dashboardController = controller;
    }

    /**
     * Gérer la sauvegarde de l'employé
     */
    @FXML
    private void handleSave() {
        System.out.println("💾 Tentative d'ajout d'employé...");

        // Validation des champs
        if (!validateFields()) {
            return;
        }

        try {
            // Créer un nouvel employé
            Employe employe = new Employe();
            employe.setCin(Integer.parseInt(cinField.getText().trim()));
            employe.setNom(nomField.getText().trim());
            employe.setPrenom(prenomField.getText().trim());
            employe.setEmail(emailField.getText().trim());
            employe.setMdp(passwordField.getText());
            employe.setTel(telField.getText().trim());
            employe.setDate_naiss(dateNaissancePicker.getValue() != null
                    ? dateNaissancePicker.getValue().toString()
                    : "1990-01-01");
            employe.setAdresse(adresseField.getText().trim());
            employe.setVille(villeField.getText().trim());
            employe.setDate_creationcpt(LocalDate.now().toString());
            employe.setDate_dernierchg(LocalDate.now().toString());

            System.out.println("🔍 Données de l'employé:");
            System.out.println("  CIN: " + employe.getCin());
            System.out.println("  Nom: " + employe.getNom());
            System.out.println("  Prénom: " + employe.getPrenom());
            System.out.println("  Email: " + employe.getEmail());
            System.out.println("  Rôle: " + employe.getRole());

            // Ajouter dans la base de données
            personneService.ajouter(employe);

            System.out.println("✓ Employé ajouté avec succès");
            ajoutReussi = true;

            // Fermer la fenêtre
            closeWindow();

        } catch (NumberFormatException e) {
            showError("Le CIN doit être un nombre valide");
            System.err.println("✗ Erreur de format CIN");
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showError("Un employé avec ce CIN existe déjà");
            } else {
                showError("Erreur lors de l'ajout de l'employé");
            }
            System.err.println("✗ Erreur SQL:");
            e.printStackTrace();
        } catch (Exception e) {
            showError("Une erreur inattendue est survenue");
            System.err.println("✗ Erreur inattendue:");
            e.printStackTrace();
        }
    }

    /**
     * Gérer l'annulation
     */
    @FXML
    private void handleCancel() {
        System.out.println("❌ Ajout annulé");
        closeWindow();
    }

    /**
     * Valider tous les champs
     */
    private boolean validateFields() {
        // CIN
        if (cinField.getText().trim().isEmpty()) {
            showError("Le CIN est obligatoire");
            cinField.requestFocus();
            return false;
        }

        try {
            int cin = Integer.parseInt(cinField.getText().trim());
            if (cin <= 0) {
                showError("Le CIN doit être un nombre positif");
                cinField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Le CIN doit être un nombre valide");
            cinField.requestFocus();
            return false;
        }

        // Nom
        if (nomField.getText().trim().isEmpty()) {
            showError("Le nom est obligatoire");
            nomField.requestFocus();
            return false;
        }

        // Prénom
        if (prenomField.getText().trim().isEmpty()) {
            showError("Le prénom est obligatoire");
            prenomField.requestFocus();
            return false;
        }

        // Email
        if (emailField.getText().trim().isEmpty()) {
            showError("L'email est obligatoire");
            emailField.requestFocus();
            return false;
        }

        if (!isValidEmail(emailField.getText().trim())) {
            showError("Format d'email invalide");
            emailField.requestFocus();
            return false;
        }

        // Mot de passe
        if (passwordField.getText().isEmpty()) {
            showError("Le mot de passe est obligatoire");
            passwordField.requestFocus();
            return false;
        }

        if (passwordField.getText().length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            passwordField.requestFocus();
            return false;
        }

        // Téléphone
        if (telField.getText().trim().isEmpty()) {
            showError("Le téléphone est obligatoire");
            telField.requestFocus();
            return false;
        }

        hideError();
        return true;
    }

    /**
     * Valider le format de l'email
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Afficher un message d'erreur
     */
    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 12px;");
        errorLabel.setVisible(true);
    }

    /**
     * Masquer le message d'erreur
     */
    private void hideError() {
        errorLabel.setVisible(false);
    }

    /**
     * Fermer la fenêtre
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();

        // Rafraîchir le tableau du dashboard si l'ajout a réussi
        if (ajoutReussi && dashboardController != null) {
            try {
                dashboardController.loadEmployees();
            } catch (Exception e) {
                System.err.println("✗ Erreur lors du rafraîchissement de la liste");
                e.printStackTrace();
            }
        }
    }

    /**
     * Vérifier si l'ajout a réussi
     */
    public boolean isAjoutReussi() {
        return ajoutReussi;
    }
}