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
import services.User.LoginHistoryService;
import services.User.SmsService;
import utils.SessionManager;
import javafx.scene.input.KeyCode;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class Authentification {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink signupLink;
    @FXML private Button loginButton;

    private PersonneService personneService;
    private EmailService emailService;
    private TwoFactorAuthService twoFactorService;
    private LoginHistoryService loginHistoryService;
    private SmsService smsService;

    private static final String DEFAULT_IP = "127.0.0.1";

    @FXML
    private void initialize() {
        personneService    = new PersonneService();
        emailService       = new EmailService();
        twoFactorService   = new TwoFactorAuthService();
        loginHistoryService = new LoginHistoryService();
        smsService         = new SmsService();

        hideError();

        emailField.textProperty().addListener((obs, o, n) -> { if (errorLabel.isVisible()) hideError(); });
        passwordField.textProperty().addListener((obs, o, n) -> { if (errorLabel.isVisible()) hideError(); });
        passwordField.setOnKeyPressed(e -> { if (e.getCode() == KeyCode.ENTER) loginButton.fire(); });
    }

    // ═══════════════════════════════════════════════════════════════════
    // CONNEXION
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleLogin(ActionEvent event) {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }
        if (!isValidEmail(email)) {
            showError("Format d'email invalide");
            loginHistoryService.saveFailedLogin(email, DEFAULT_IP, "Format d'email invalide");
            return;
        }

        loginButton.setDisable(true);

        try {
            Personne personne = personneService.authenticate(email, password);

            if (personne != null) {
                hideError();

                if (twoFactorService.isTwoFactorEnabled(personne.getCin())) {
                    String telephone = personne.getTel();

                    if (telephone == null || telephone.isEmpty()) {
                        // Pas de numéro → fallback Google Authenticator
                        System.out.println("⚠️ Pas de numéro, fallback Google Authenticator");
                        showTwoFactorDialog(personne);
                    } else {
                        // Générer et envoyer OTP par SMS
                        String otp = String.format("%06d", (int)(Math.random() * 999999));
                        SessionManager.setTempOtp(otp);

                        boolean sent = smsService.sendOtpCode(telephone, otp);
                        if (sent) {
                            showSmsOtpDialog(personne, otp);
                        } else {
                            showError("Impossible d'envoyer le SMS. Vérifiez votre numéro.");
                            loginHistoryService.saveFailedLogin(email, DEFAULT_IP, "Échec envoi SMS OTP");
                        }
                    }
                } else {
                    // Pas de 2FA → connexion directe
                    loginHistoryService.saveSuccessfulLogin(personne.getCin(), personne.getEmail(), DEFAULT_IP);
                    finalizeLogin(personne);
                }

            } else {
                showError("Email ou mot de passe incorrect");
                loginHistoryService.saveFailedLogin(email, DEFAULT_IP, "Email ou mot de passe incorrect");
            }

        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données");
            loginHistoryService.saveFailedLogin(email, DEFAULT_IP, "Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("Erreur lors de la connexion");
            loginHistoryService.saveFailedLogin(email, DEFAULT_IP, "Erreur: " + e.getMessage());
            e.printStackTrace();
        } finally {
            loginButton.setDisable(false);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // DIALOGUE OTP PAR SMS
    // ═══════════════════════════════════════════════════════════════════

    private void showSmsOtpDialog(Personne personne, String expectedOtp) {
        try {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("🔐 Vérification par SMS");

            // Masquer partiellement le numéro
            String tel = personne.getTel();
            String maskedTel = tel.length() > 4
                    ? tel.substring(0, tel.length() - 4).replaceAll("\\d", "*") + tel.substring(tel.length() - 4)
                    : tel;
            dialog.setHeaderText("Un code a été envoyé au : " + maskedTel);

            ButtonType verifyBtn = new ButtonType("Vérifier",  ButtonBar.ButtonData.OK_DONE);
            ButtonType resendBtn = new ButtonType("Renvoyer",  ButtonBar.ButtonData.LEFT);
            dialog.getDialogPane().getButtonTypes().addAll(verifyBtn, resendBtn, ButtonType.CANCEL);

            // Champ code
            TextField codeField = new TextField();
            codeField.setPromptText("000000");
            codeField.setMaxWidth(200);
            codeField.setStyle(
                    "-fx-font-family: 'Courier New';" +
                            "-fx-font-size: 26px;" +
                            "-fx-alignment: center;" +
                            "-fx-pref-height: 56px;" +
                            "-fx-background-color: #F8FBF6;" +
                            "-fx-border-color: #52B788;" +
                            "-fx-border-radius: 8;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-width: 2;"
            );

            // Autoriser uniquement 6 chiffres
            codeField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) codeField.setText(newVal.replaceAll("[^\\d]", ""));
                if (newVal.length() > 6)     codeField.setText(newVal.substring(0, 6));
            });

            VBox content = new VBox(16);
            content.setAlignment(Pos.CENTER);
            content.setPrefWidth(320);
            content.getChildren().addAll(
                    new Label("📱 Entrez le code reçu par SMS :"),
                    codeField,
                    new Label("⏱  Valable 5 minutes — ne le partagez pas")
            );
            dialog.getDialogPane().setContent(content);

            // Tableau pour permettre la mise à jour dans le lambda
            final String[] currentOtp = { expectedOtp };

            dialog.setResultConverter(btn -> {
                if (btn == resendBtn) {
                    // Générer et renvoyer un nouveau code
                    String newOtp = String.format("%06d", (int)(Math.random() * 999999));
                    currentOtp[0] = newOtp;
                    SessionManager.setTempOtp(newOtp);

                    boolean sent = smsService.sendOtpCode(personne.getTel(), newOtp);
                    Alert info = new Alert(sent ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                    info.setTitle(sent ? "SMS renvoyé" : "Erreur");
                    info.setHeaderText(null);
                    info.setContentText(sent
                            ? "Un nouveau code a été envoyé au " + maskedTel
                            : "Échec de l'envoi du SMS. Réessayez.");
                    info.showAndWait();
                    return null; // Garder le dialogue ouvert
                }
                if (btn == verifyBtn) return codeField.getText();
                return null;
            });

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(code -> {
                if (code.equals(currentOtp[0])) {
                    // ✅ Code correct
                    loginHistoryService.saveSuccessfulLoginWith2FA(
                            personne.getCin(), personne.getEmail(), DEFAULT_IP);
                    SessionManager.clearTempOtp();
                    finalizeLogin(personne);
                } else {
                    // ❌ Code incorrect
                    showError("Code SMS incorrect. Réessayez.");
                    loginHistoryService.saveLoginHistory(
                            personne.getCin(), personne.getEmail(),
                            DEFAULT_IP, null, false, true, "Code SMS incorrect");
                }
            });

            // Annulation
            if (!result.isPresent()) {
                loginHistoryService.saveLoginHistory(
                        personne.getCin(), personne.getEmail(),
                        DEFAULT_IP, null, false, true, "Dialogue SMS annulé");
            }

        } catch (Exception e) {
            showError("Erreur lors de la vérification SMS");
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // DIALOGUE GOOGLE AUTHENTICATOR (fallback si pas de téléphone)
    // ═══════════════════════════════════════════════════════════════════

    private void showTwoFactorDialog(Personne personne) {
        try {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("🔐 Authentification à deux facteurs");
            dialog.setHeaderText("Entrez le code de votre application d'authentification");

            ButtonType verifyButton = new ButtonType("Vérifier",         ButtonBar.ButtonData.OK_DONE);
            ButtonType backupButton = new ButtonType("Code de secours",  ButtonBar.ButtonData.LEFT);
            dialog.getDialogPane().getButtonTypes().addAll(verifyButton, backupButton, ButtonType.CANCEL);

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

            dialog.setResultConverter(btn -> {
                if (btn == verifyButton || btn == backupButton) return codeField.getText();
                return null;
            });

            Optional<String> result = dialog.showAndWait();

            result.ifPresent(code -> {
                try {
                    int codeInt = Integer.parseInt(code);
                    if (twoFactorService.verifyCodeForUser(personne.getCin(), codeInt)) {
                        loginHistoryService.saveSuccessfulLoginWith2FA(
                                personne.getCin(), personne.getEmail(), DEFAULT_IP);
                        finalizeLogin(personne);
                    } else {
                        if (twoFactorService.verifyAndUseBackupCode(personne.getCin(), code)) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Code de secours utilisé");
                            alert.setContentText("Pensez à générer de nouveaux codes de secours.");
                            alert.showAndWait();
                            loginHistoryService.saveSuccessfulLoginWith2FA(
                                    personne.getCin(), personne.getEmail(), DEFAULT_IP);
                            finalizeLogin(personne);
                        } else {
                            showError("Code incorrect. Réessayez.");
                            loginHistoryService.saveLoginHistory(
                                    personne.getCin(), personne.getEmail(),
                                    DEFAULT_IP, null, false, true, "Code 2FA incorrect");
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
    // FINALISATION
    // ═══════════════════════════════════════════════════════════════════

    private void finalizeLogin(Personne personne) {
        try {
            SessionManager.setCurrentUser(personne);

            System.out.println("\n========================================");
            System.out.println("✅ CONNEXION RÉUSSIE");
            System.out.println("   Utilisateur: " + personne.getPrenom() + " " + personne.getNom());
            System.out.println("   Email: " + personne.getEmail());
            System.out.println("   Rôle: " + getRoleName(personne.getRole()));
            System.out.println("========================================\n");

            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            emailService.sendLoginAlert(
                    personne.getEmail(),
                    personne.getPrenom() + " " + personne.getNom(),
                    DEFAULT_IP, dateTime);

            redirectToDashboard(personne);

        } catch (Exception e) {
            showError("Erreur lors de la finalisation de la connexion");
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // MOT DE PASSE OUBLIÉ
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        TextInputDialog emailDialog = new TextInputDialog();
        emailDialog.setTitle("Mot de passe oublié");
        emailDialog.setHeaderText("Réinitialisation de mot de passe");
        emailDialog.setContentText("Entrez votre adresse email:");

        emailDialog.showAndWait().ifPresent(email -> {
            try {
                Personne user = personneService.rechercherParEmail(email);
                if (user != null) {
                    String resetCode = personneService.generateResetCode(email);
                    if (emailService.sendPasswordResetCode(email, user.getPrenom() + " " + user.getNom(), resetCode)) {
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

    private void showResetCodeDialog(String email) {
        Dialog<ButtonType> codeDialog = new Dialog<>();
        codeDialog.setTitle("Code de vérification");
        codeDialog.setHeaderText("Un code a été envoyé à " + email);

        TextField     codeField           = new TextField();      codeField.setPromptText("000000");
        PasswordField newPasswordField    = new PasswordField();  newPasswordField.setPromptText("Nouveau mot de passe");
        PasswordField confirmPasswordField= new PasswordField();  confirmPasswordField.setPromptText("Confirmer mot de passe");

        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Code de vérification :"), codeField,
                new Label("Nouveau mot de passe :"), newPasswordField,
                new Label("Confirmer :"),            confirmPasswordField
        );
        codeDialog.getDialogPane().setContent(content);
        codeDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        codeDialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                    showError("Les mots de passe ne correspondent pas");
                    return;
                }
                try {
                    if (personneService.resetPassword(email, codeField.getText(), newPasswordField.getText())) {
                        Alert s = new Alert(Alert.AlertType.INFORMATION);
                        s.setTitle("Succès");
                        s.setContentText("Vous pouvez maintenant vous connecter avec votre nouveau mot de passe.");
                        s.showAndWait();
                    } else {
                        showError("Code invalide ou expiré");
                    }
                } catch (SQLException e) {
                    showError("Erreur lors de la réinitialisation");
                    e.printStackTrace();
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════
    // INSCRIPTION
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    private void handleSignup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/SignUp.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) signupLink.getScene().getWindow();
            stage.setScene(new Scene(root));
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

    private void redirectToDashboard(Personne personne) {
        try {
            String fxmlPath, title;
            switch (personne.getRole()) {
                case 3 -> { fxmlPath = "/UsersInterface/Acceuil.fxml";      title = "AgroFlow - Dashboard Admin"; }
                case 2 -> { fxmlPath = "/UsersInterface/AcceuilEmp.fxml";   title = "AgroFlow - Dashboard Employé"; }
                case 1 -> { fxmlPath = "/UsersInterface/AcceuillAgr.fxml";  title = "AgroFlow - Dashboard Utilisateur"; }
                default -> { showError("Rôle utilisateur non reconnu"); return; }
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if      (controller instanceof AcceuilEmploye)  ((AcceuilEmploye)  controller).setCurrentUser(personne);
            else if (controller instanceof AcceuilAgricole) ((AcceuilAgricole) controller).setCurrentUser(personne);
            else if (controller instanceof Acceuil)         ((Acceuil)         controller).setCurrentUser(personne);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1500, 700));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            showError("Erreur lors de la redirection");
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
        return email != null && !email.isEmpty()
                && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showError(String message) { errorLabel.setText(message); errorLabel.setVisible(true); }
    private void hideError()               { errorLabel.setVisible(false); errorLabel.setText(""); }
}