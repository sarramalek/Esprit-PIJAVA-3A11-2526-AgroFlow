package controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import models.User.Personne;
import services.User.PersonneService;
import services.User.EmailService;
import services.User.TwoFactorAuthService;
import utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Contrôleur d'authentification avec les 3 APIs intégrées:
 * - BCrypt pour le hachage des mots de passe
 * - JavaMail pour l'envoi d'emails
 * - Google Authenticator pour la 2FA
 */
public class Authentification {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Hyperlink signupLink;

    @FXML
    private Button loginButton;

    private PersonneService personneService;
    private EmailService emailService;
    private TwoFactorAuthService twoFactorService;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    private void initialize() {
        personneService = new PersonneService();
        emailService = new EmailService();
        twoFactorService = new TwoFactorAuthService();

        hideError();

        // Listeners pour effacer l'erreur lors de la saisie
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (errorLabel.isVisible()) {
                hideError();
            }
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (errorLabel.isVisible()) {
                hideError();
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONNEXION AVEC BCRYPT + 2FA
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Gère la connexion de l'utilisateur
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Format d'email invalide");
            return;
        }

        loginButton.setDisable(true);

        try {
            // Authentification avec BCrypt
            Personne personne = personneService.authenticate(email, password);

            if (personne != null) {
                hideError();

                // Vérifier si la 2FA est activée
                if (twoFactorService.isTwoFactorEnabled(personne.getCin())) {
                    System.out.println("🔐 2FA activée pour cet utilisateur");
                    showTwoFactorDialog(personne);
                } else {
                    // Connexion directe (sans 2FA)
                    finalizeLogin(personne);
                }
            } else {
                showError("Email ou mot de passe incorrect");
            }

        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données");
            e.printStackTrace();
        } catch (Exception e) {
            showError("Erreur lors de la connexion");
            e.printStackTrace();
        } finally {
            loginButton.setDisable(false);
        }
    }

    /**
     * Finalise la connexion après vérification (avec ou sans 2FA)
     */
    private void finalizeLogin(Personne personne) {
        try {
            // Sauvegarder la session
            SessionManager.setCurrentUser(personne);

            // Afficher les informations de connexion
            System.out.println("\n========================================");
            System.out.println("✅ CONNEXION RÉUSSIE");
            System.out.println("   Utilisateur: " + personne.getPrenom() + " " + personne.getNom());
            System.out.println("   Email: " + personne.getEmail());
            System.out.println("   Rôle: " + getRoleName(personne.getRole()));
            System.out.println("========================================\n");

            // Envoyer alerte de connexion par email (optionnel)
            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            emailService.sendLoginAlert(
                    personne.getEmail(),
                    personne.getPrenom() + " " + personne.getNom(),
                    "127.0.0.1", // TODO: Récupérer la vraie IP
                    dateTime
            );

            // Redirection vers le dashboard
            redirectToDashboard(personne);

        } catch (Exception e) {
            showError("Erreur lors de la finalisation de la connexion");
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // DIALOGUE 2FA
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Affiche le dialogue pour entrer le code 2FA
     */
    private void showTwoFactorDialog(Personne personne) {
        try {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("🔐 Authentification à deux facteurs");
            dialog.setHeaderText("Entrez le code de votre application d'authentification");

            // Boutons
            ButtonType verifyButton = new ButtonType("Vérifier", ButtonBar.ButtonData.OK_DONE);
            ButtonType backupButton = new ButtonType("Code de secours", ButtonBar.ButtonData.LEFT);
            dialog.getDialogPane().getButtonTypes().addAll(verifyButton, backupButton, ButtonType.CANCEL);

            // Champ de saisie
            TextField codeField = new TextField();
            codeField.setPromptText("000000");
            codeField.setPrefWidth(200);
            codeField.setStyle("-fx-font-size: 20px; -fx-alignment: center;");

            VBox content = new VBox(15);
            content.setAlignment(Pos.CENTER);
            content.getChildren().addAll(
                    new Label("Code à 6 chiffres :"),
                    codeField,
                    new Label("Le code change toutes les 30 secondes")
            );
            dialog.getDialogPane().setContent(content);

            // Gérer les boutons
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == verifyButton || dialogButton == backupButton) {
                    return codeField.getText();
                }
                return null;
            });

            // Afficher et traiter
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(code -> {
                try {
                    // Essayer comme code 2FA normal
                    int codeInt = Integer.parseInt(code);

                    if (twoFactorService.verifyCodeForUser(personne.getCin(), codeInt)) {
                        finalizeLogin(personne);
                    } else {
                        // Essayer comme code de secours
                        if (twoFactorService.verifyAndUseBackupCode(personne.getCin(), code)) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Code de secours utilisé");
                            alert.setHeaderText("Vous avez utilisé un code de secours");
                            alert.setContentText("Il vous reste moins de codes. Pensez à en générer de nouveaux.");
                            alert.showAndWait();
                            finalizeLogin(personne);
                        } else {
                            showError("Code incorrect. Réessayez.");
                        }
                    }
                } catch (NumberFormatException e) {
                    showError("Code invalide");
                } catch (SQLException e) {
                    showError("Erreur de vérification");
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            showError("Erreur lors de la vérification 2FA");
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // MOT DE PASSE OUBLIÉ (AVEC EMAIL)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Gère le mot de passe oublié avec envoi d'email
     */
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        // Demander l'email
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Mot de passe oublié");
        emailDialog.setHeaderText("Réinitialisation de mot de passe");
        emailDialog.setContentText("Entrez votre adresse email:");

        Optional<String> emailResult = emailDialog.showAndWait();

        emailResult.ifPresent(email -> {
            try {
                Personne user = personneService.rechercherParEmail(email);

                if (user != null) {
                    // Générer et envoyer le code
                    String resetCode = personneService.generateResetCode(email);
                    String userName = user.getPrenom() + " " + user.getNom();

                    if (emailService.sendPasswordResetCode(email, userName, resetCode)) {
                        showResetCodeDialog(email);
                    } else {
                        showError("Erreur d'envoi d'email");
                    }
                } else {
                    showError("Email non trouvé");
                }
            } catch (SQLException e) {
                showError("Erreur de base de données");
                e.printStackTrace();
            }
        });
    }

    /**
     * Dialogue pour entrer le code de réinitialisation
     */
    private void showResetCodeDialog(String email) {
        Dialog<ButtonType> codeDialog = new Dialog<>();
        codeDialog.setTitle("Code de vérification");
        codeDialog.setHeaderText("Un code a été envoyé à " + email);

        TextField codeField = new TextField();
        codeField.setPromptText("000000");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nouveau mot de passe");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer mot de passe");

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Code de vérification :"),
                codeField,
                new Label("Nouveau mot de passe :"),
                newPasswordField,
                new Label("Confirmer :"),
                confirmPasswordField
        );

        codeDialog.getDialogPane().setContent(content);
        codeDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = codeDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String code = codeField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!newPassword.equals(confirmPassword)) {
                showError("Les mots de passe ne correspondent pas");
                return;
            }

            try {
                if (personneService.resetPassword(email, code, newPassword)) {
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Succès");
                    success.setHeaderText("Mot de passe réinitialisé");
                    success.setContentText("Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.");
                    success.showAndWait();
                } else {
                    showError("Code invalide ou expiré");
                }
            } catch (SQLException e) {
                showError("Erreur lors de la réinitialisation");
                e.printStackTrace();
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // INSCRIPTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Gère la création de compte
     */
    @FXML
    private void handleSignup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/SignUp.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) signupLink.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("AgroFlow - Créer un compte");
            stage.show();

        } catch (Exception e) {
            System.err.println("❌ Impossible de charger la page d'inscription");
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // REDIRECTION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Redirige vers le dashboard approprié selon le rôle
     */
    private void redirectToDashboard(Personne personne) {
        try {
            String fxmlPath;
            String title;
            int role = personne.getRole();

            switch (role) {
                case 3 -> { // Admin
                    fxmlPath = "/UsersInterface/Acceuil.fxml";
                    title = "AgroFlow - Dashboard Admin";
                }
                case 2 -> { // Employé
                    fxmlPath = "/UsersInterface/AcceuilEmp.fxml";
                    title = "AgroFlow - Dashboard Employé";
                }
                case 1 -> { // Utilisateur (Agricole)
                    fxmlPath = "/UsersInterface/AcceuillAgr.fxml";
                    title = "AgroFlow - Dashboard Utilisateur";
                }
                default -> {
                    showError("Rôle utilisateur non reconnu");
                    return;
                }
            }

            System.out.println("🚀 Navigation vers: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            System.out.println("✓ FXML chargé");

            // Transférer l'utilisateur au contrôleur
            Object controller = loader.getController();
            if (controller != null) {
                System.out.println("✓ Contrôleur: " + controller.getClass().getSimpleName());

                // Utiliser instanceof pour chaque type
                if (controller instanceof AcceuilEmploye) {
                    ((AcceuilEmploye) controller).setCurrentUser(personne);
                    System.out.println("✓ Utilisateur transféré au contrôleur");
                } else if (controller instanceof AcceuilAgricole) {
                    ((AcceuilAgricole) controller).setCurrentUser(personne);
                    System.out.println("✓ Utilisateur transféré au contrôleur");
                } else if (controller instanceof Acceuil) {
                    ((Acceuil) controller).setCurrentUser(personne);
                    System.out.println("✓ Utilisateur transféré au contrôleur");
                }
            }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

            System.out.println("✓ Navigation réussie vers le dashboard\n");

        } catch (Exception e) {
            showError("Erreur lors de la redirection");
            System.err.println("❌ Erreur de redirection vers le dashboard");
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════

    private String getRoleName(int role) {
        return switch (role) {
            case 1 -> "Utilisateur (Agricole)";
            case 2 -> "Employé";
            case 3 -> "Administrateur";
            default -> "Inconnu";
        };
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }
}