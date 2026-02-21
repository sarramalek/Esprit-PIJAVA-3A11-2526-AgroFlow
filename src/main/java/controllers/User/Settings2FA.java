package controllers.User;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import models.User.Personne;
import services.User.SmsService;
import services.User.TwoFactorAuthService;
import utils.SessionManager;

import java.sql.SQLException;

/**
 * Gestion de la 2FA par SMS — sans FXML
 * Flux : envoi OTP → validation → activation/désactivation en DB
 */
public class Settings2FA {

    private TwoFactorAuthService twoFactorService;
    private SmsService smsService;
    private Personne currentUser;

    public Settings2FA() {
        this.twoFactorService = new TwoFactorAuthService();
        this.smsService       = new SmsService();
        this.currentUser      = SessionManager.getCurrentUser();
    }

    // ═══════════════════════════════════════════════════════════════
    // POINT D'ENTRÉE — appelé depuis DashboardPersonnes
    // ═══════════════════════════════════════════════════════════════

    /**
     * Lance le flux complet :
     * - Envoie un OTP par SMS
     * - Affiche le dialogue de saisie
     * - Active ou désactive la 2FA selon l'état actuel
     */
    public void launch() {
        String telephone = currentUser.getTel();

        if (telephone == null || telephone.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Téléphone manquant",
                    "Aucun numéro de téléphone n'est associé à votre compte.\n" +
                            "Veuillez contacter un administrateur.");
            return;
        }

        try {
            boolean already2FA = twoFactorService.isTwoFactorEnabled(currentUser.getCin());

            if (already2FA) {
                // Proposer désactivation
                showDisableDialog(telephone);
            } else {
                // Proposer activation
                showEnableDialog(telephone);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur DB",
                    "Impossible de vérifier l'état de la 2FA : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ACTIVATION
    // ═══════════════════════════════════════════════════════════════

    private void showEnableDialog(String telephone) {
        // Masquer le numéro
        String masked = maskPhone(telephone);

        // Générer et envoyer OTP
        String otp = generateOtp();
        boolean sent = smsService.sendOtpCode(telephone, otp);

        if (!sent) {
            showAlert(Alert.AlertType.ERROR, "Échec SMS",
                    "Impossible d'envoyer le SMS de vérification.\nVérifiez votre numéro.");
            return;
        }

        // Dialogue de saisie
        Dialog<String> dialog = buildOtpDialog(
                "🔒 Activation de la 2FA",
                "Un code a été envoyé au " + masked + "\nEntrez-le pour activer la protection SMS."
        );

        final String[] currentOtp = { otp };
        setupResendButton(dialog, telephone, currentOtp);

        dialog.showAndWait().ifPresent(code -> {
            if (code.equals(currentOtp[0])) {
                // ✅ Code correct → activer en DB
                try {
                    twoFactorService.enableTwoFactorAuth(currentUser.getCin());
                    showAlert(Alert.AlertType.INFORMATION, "✅ 2FA activée",
                            "La protection par SMS est maintenant active.\n\n" +
                                    "À chaque connexion, un code sera envoyé au :\n" + masked);
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible d'activer la 2FA en base de données.");
                    e.printStackTrace();
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Code incorrect",
                        "Le code saisi est invalide. La 2FA n'a pas été activée.");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    // DÉSACTIVATION
    // ═══════════════════════════════════════════════════════════════

    private void showDisableDialog(String telephone) {
        String masked = maskPhone(telephone);

        // Confirmation d'abord
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Désactiver la 2FA");
        confirm.setHeaderText("Votre compte est actuellement protégé");
        confirm.setContentText(
                "Pour désactiver la 2FA, vous devez confirmer votre identité.\n" +
                        "Un code sera envoyé au : " + masked
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Envoyer OTP
                String otp = generateOtp();
                boolean sent = smsService.sendOtpCode(telephone, otp);

                if (!sent) {
                    showAlert(Alert.AlertType.ERROR, "Échec SMS",
                            "Impossible d'envoyer le SMS de vérification.");
                    return;
                }

                // Dialogue de saisie
                Dialog<String> dialog = buildOtpDialog(
                        "🔓 Désactivation de la 2FA",
                        "Code envoyé au " + masked + "\nEntrez-le pour désactiver la 2FA."
                );

                final String[] currentOtp = { otp };
                setupResendButton(dialog, telephone, currentOtp);

                dialog.showAndWait().ifPresent(code -> {
                    if (code.equals(currentOtp[0])) {
                        try {
                            twoFactorService.disableTwoFactorAuth(currentUser.getCin());
                            showAlert(Alert.AlertType.INFORMATION, "2FA désactivée",
                                    "La protection par SMS a été désactivée.\n" +
                                            "Votre compte est moins sécurisé.");
                        } catch (SQLException e) {
                            showAlert(Alert.AlertType.ERROR, "Erreur",
                                    "Impossible de désactiver la 2FA.");
                            e.printStackTrace();
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Code incorrect",
                                "Le code saisi est invalide. La 2FA reste active.");
                    }
                });
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Construit le dialogue OTP réutilisable
     */
    private Dialog<String> buildOtpDialog(String title, String header) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        ButtonType verifyBtn = new ButtonType("Vérifier ✔", ButtonBar.ButtonData.OK_DONE);
        ButtonType resendBtn = new ButtonType("Renvoyer 🔄", ButtonBar.ButtonData.LEFT);
        dialog.getDialogPane().getButtonTypes().addAll(verifyBtn, resendBtn, ButtonType.CANCEL);

        TextField codeField = new TextField();
        codeField.setPromptText("● ● ● ● ● ●");
        codeField.setMaxWidth(200);
        codeField.setStyle(
                "-fx-font-family: 'Courier New';" +
                        "-fx-font-size: 24px;" +
                        "-fx-alignment: center;" +
                        "-fx-pref-height: 52px;" +
                        "-fx-border-color: #52B788;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-width: 2;"
        );

        // Uniquement chiffres, max 6
        codeField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) codeField.setText(n.replaceAll("[^\\d]", ""));
            if (n.length() > 6)     codeField.setText(n.substring(0, 6));
        });

        VBox content = new VBox(14);
        content.setAlignment(Pos.CENTER);
        content.setPrefWidth(320);
        content.getChildren().addAll(
                new Label("📱 Code à 6 chiffres :"),
                codeField,
                new Label("⏱  Valable 5 minutes — ne le partagez pas")
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == verifyBtn) return codeField.getText();
            return null; // CANCEL ou RESEND géré ailleurs
        });

        return dialog;
    }

    /**
     * Configure le bouton Renvoyer dans le dialogue
     */
    private void setupResendButton(Dialog<String> dialog, String telephone, String[] currentOtp) {
        // Surcharger le converter pour gérer le renvoi
        dialog.setResultConverter(btn -> {
            String label = btn.getText();
            if (label != null && label.contains("Renvoyer")) {
                String newOtp = generateOtp();
                currentOtp[0] = newOtp;
                boolean sent = smsService.sendOtpCode(telephone, newOtp);
                Alert info = new Alert(sent ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                info.setTitle(sent ? "SMS renvoyé" : "Erreur");
                info.setHeaderText(null);
                info.setContentText(sent
                        ? "Un nouveau code a été envoyé au " + maskPhone(telephone)
                        : "Échec de l'envoi. Réessayez.");
                info.showAndWait();
                return null; // garder le dialogue ouvert
            }
            if (btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                TextField tf = (TextField) ((VBox) dialog.getDialogPane().getContent()).getChildren().get(1);
                return tf.getText();
            }
            return null;
        });
    }

    private String generateOtp() {
        return String.format("%06d", (int)(Math.random() * 999999));
    }

    private String maskPhone(String tel) {
        return tel.length() > 4
                ? tel.substring(0, tel.length() - 4).replaceAll("\\d", "*")
                + tel.substring(tel.length() - 4)
                : tel;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}