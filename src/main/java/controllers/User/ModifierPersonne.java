package controllers.User;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User.Employe;
import models.User.Personne;
import services.User.PersonneService;


import java.sql.SQLException;
import java.time.LocalDate;

public class ModifierPersonne {
    private Personne personneActuelle;
    private ProfilAgricole profilAgricoleController;

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
    @FXML private Label titleLabel;

    private PersonneService personneService;
    private DashboardPersonnes dashboardController;
    private Employe employeActuel;
    private boolean modificationReussie = false;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        try {
            personneService = new PersonneService();
            System.out.println("✓ ModifierEmployeController initialisé");
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'initialisation");
            e.printStackTrace();
            showError("Erreur de connexion à la base de données");
        }

        // Le CIN ne peut pas être modifié (clé primaire)
        cinField.setEditable(false);
        cinField.setStyle("-fx-background-color: #f0f0f0;");

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
     * Charger les données de l'employé à modifier
     */
    public void setEmploye( Personne employe) {


        System.out.println("📝 Chargement des données de l'employé:");
        System.out.println("  CIN: " + employe.getCin());
        System.out.println("  Nom: " + employe.getNom());

        // Remplir les champs avec les données existantes
        cinField.setText(String.valueOf(employe.getCin()));
        nomField.setText(employe.getNom());
        prenomField.setText(employe.getPrenom());
        emailField.setText(employe.getEmail());
        passwordField.setText(employe.getMdp());
        telField.setText(employe.getTel());
        adresseField.setText(employe.getAdresse() != null ? employe.getAdresse() : "");
        villeField.setText(employe.getVille() != null ? employe.getVille() : "");

        // Date de naissance
        if (employe.getDate_naiss() != null && !employe.getDate_naiss().isEmpty()) {
            try {
                dateNaissancePicker.setValue(LocalDate.parse(employe.getDate_naiss()));
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors du parsing de la date: " + employe.getDate_naiss());
            }
        }

        // Mettre à jour le titre
        if (titleLabel != null) {
            titleLabel.setText("Modifier l'employé - " + employe.getPrenom() + " " + employe.getNom());
        }
    }

    /**
     * Gérer la sauvegarde des modifications
     */
    @FXML
    private void handleSave() {
        if (!validateFields()) return;

        try {
            // Utiliser personneActuelle si disponible, sinon employeActuel
            Personne cible = personneActuelle != null ? personneActuelle : employeActuel;

            cible.setNom(nomField.getText().trim());
            cible.setPrenom(prenomField.getText().trim());
            cible.setEmail(emailField.getText().trim());
            cible.setMdp(passwordField.getText());
            cible.setTel(telField.getText().trim());
            cible.setDate_naiss(dateNaissancePicker.getValue() != null
                    ? dateNaissancePicker.getValue().toString()
                    : cible.getDate_naiss());
            cible.setAdresse(adresseField.getText().trim());
            cible.setVille(villeField.getText().trim());
            cible.setDate_dernierchg(LocalDate.now().toString());

            personneService.modifier(cible);
            System.out.println("✓ Profil modifié avec succès");
            modificationReussie = true;

            // ── Notifier ProfilAgricole si ouvert depuis le dashboard agricole ──
            if (profilAgricoleController != null) {
                profilAgricoleController.refreshUser(cible);
            }

            closeWindow();

        } catch (SQLException e) {
            showError("Erreur lors de la modification");
            e.printStackTrace();
        }
    }
     /* Gérer l'annulation
     */
    @FXML
    private void handleCancel() {
        System.out.println("❌ Modification annulée");
        closeWindow();
    }

    /**
     * Valider tous les champs
     */
    private boolean validateFields() {
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

        // Rafraîchir le tableau du dashboard si la modification a réussi
        if (modificationReussie && dashboardController != null) {
            try {
                dashboardController.loadEmployees();
            } catch (Exception e) {
                System.err.println("✗ Erreur lors du rafraîchissement de la liste");
                e.printStackTrace();
            }
        }
    }

    /**
     * Vérifier si la modification a réussi
     */
    public boolean isModificationReussie() {
        return modificationReussie;
    }
    public void setPersonne(Personne personne) {
        this.personneActuelle = personne;

        // Compatibilité avec l'ancien code Employe
        if (personne instanceof Employe) {
            this.employeActuel = (Employe) personne;
        }

        // Remplir les champs
        cinField.setText(String.valueOf(personne.getCin()));
        nomField.setText(personne.getNom() != null ? personne.getNom() : "");
        prenomField.setText(personne.getPrenom() != null ? personne.getPrenom() : "");
        emailField.setText(personne.getEmail() != null ? personne.getEmail() : "");
        passwordField.setText(personne.getMdp() != null ? personne.getMdp() : "");
        telField.setText(personne.getTel() != null ? personne.getTel() : "");
        adresseField.setText(personne.getAdresse() != null ? personne.getAdresse() : "");
        villeField.setText(personne.getVille() != null ? personne.getVille() : "");

        if (personne.getDate_naiss() != null && !personne.getDate_naiss().isEmpty()) {
            try {
                dateNaissancePicker.setValue(LocalDate.parse(personne.getDate_naiss()));
            } catch (Exception e) {
                System.err.println("⚠️ Erreur parsing date: " + personne.getDate_naiss());
            }
        }

        if (titleLabel != null) {
            titleLabel.setText("Modifier le profil - " + personne.getPrenom() + " " + personne.getNom());
        }
    }

    public void setProfilAgricoleController(ProfilAgricole controller) {
        this.profilAgricoleController = controller;
    }
}