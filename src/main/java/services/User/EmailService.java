package services.User;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Service d'envoi d'emails avec JavaMail
 * Utilisé pour la récupération de mot de passe, notifications, etc.
 */
public class EmailService {

    // ═══════════════════════════════════════════════════════════════════
    // CONFIGURATION
    // ═══════════════════════════════════════════════════════════════════

    // TODO: Remplacez par vos identifiants Gmail
    private final String USERNAME = "maleksarra362@gmail.com";
    private final String PASSWORD = "plkcjwhpqlgsetrh";

    // Pour créer un mot de passe d'application:
    // 1. Allez sur https://myaccount.google.com/security
    // 2. Activez la validation en deux étapes
    // 3. Allez dans "Mots de passe des applications"
    // 4. Créez un mot de passe pour "Mail" ou "Autre"

    /**
     * Configuration SMTP pour Gmail
     */
    private Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════
    // MÉTHODES D'ENVOI D'EMAIL
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Envoie un email HTML formaté
     * @param toEmail Destinataire
     * @param subject Sujet
     * @param htmlBody Corps HTML
     * @return true si l'envoi a réussi
     */
    public boolean sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(USERNAME, "AgroFlow"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Email envoyé à: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur d'envoi d'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Envoie un code de réinitialisation de mot de passe
     * @param toEmail Email du destinataire
     * @param userName Nom de l'utilisateur
     * @param resetCode Code de vérification
     * @return true si l'envoi a réussi
     */
    public boolean sendPasswordResetCode(String toEmail, String userName, String resetCode) {
        String subject = "🔑 AgroFlow - Réinitialisation de mot de passe";

        String htmlBody = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { 
                        font-family: 'Segoe UI', Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #333;
                        margin: 0;
                        padding: 0;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 0 auto; 
                        padding: 20px;
                        background-color: #f9f9f9;
                    }
                    .header { 
                        background: linear-gradient(135deg, #66BB6A 0%%, #2E7D32 100%%); 
                        color: white; 
                        padding: 30px 20px; 
                        text-align: center; 
                        border-radius: 10px 10px 0 0;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                    }
                    .content {
                        background: white;
                        padding: 30px;
                        border-radius: 0 0 10px 10px;
                    }
                    .code-box { 
                        background: #f5f5f5; 
                        padding: 25px; 
                        margin: 25px 0; 
                        text-align: center; 
                        border-radius: 8px;
                        border: 2px dashed #4CAF50;
                    }
                    .code { 
                        font-size: 36px; 
                        font-weight: bold; 
                        color: #2E7D32; 
                        letter-spacing: 8px;
                        font-family: 'Courier New', monospace;
                    }
                    .warning {
                        background-color: #FFF3E0;
                        border-left: 4px solid #FF9800;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .footer { 
                        margin-top: 30px; 
                        padding-top: 20px;
                        border-top: 1px solid #e0e0e0;
                        color: #666; 
                        font-size: 12px;
                        text-align: center;
                    }
                    .button {
                        display: inline-block;
                        padding: 12px 30px;
                        background: #4CAF50;
                        color: white;
                        text-decoration: none;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🌱 AgroFlow</h1>
                        <p style="margin: 10px 0 0 0;">Réinitialisation de mot de passe</p>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Vous avez demandé la réinitialisation de votre mot de passe AgroFlow.</p>
                        <p>Voici votre code de vérification :</p>
                        <div class="code-box">
                            <div class="code">%s</div>
                        </div>
                        <div class="warning">
                            <strong>⚠️ Ce code expire dans 15 minutes.</strong><br>
                            Pour des raisons de sécurité, n'utilisez ce code qu'une seule fois.
                        </div>
                        <p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email et votre mot de passe restera inchangé.</p>
                        <p>Pour toute question, n'hésitez pas à nous contacter.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 AgroFlow - Tous droits réservés</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName, resetCode);

        return sendHtmlEmail(toEmail, subject, htmlBody);
    }

    /**
     * Envoie une notification de connexion suspecte
     * @param toEmail Email du destinataire
     * @param userName Nom de l'utilisateur
     * @param ipAddress Adresse IP de connexion
     * @param dateTime Date et heure de connexion
     * @return true si l'envoi a réussi
     */
    public boolean sendLoginAlert(String toEmail, String userName, String ipAddress, String dateTime) {
        String subject = "⚠️ AgroFlow - Nouvelle connexion détectée";

        String htmlBody = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .alert-header { 
                        background: linear-gradient(135deg, #FF9800 0%%, #F57C00 100%%);
                        color: white; 
                        padding: 20px; 
                        text-align: center; 
                        border-radius: 8px;
                    }
                    .content { background: white; padding: 20px; margin-top: 20px; }
                    .info-box {
                        background: #f5f5f5;
                        padding: 15px;
                        border-left: 4px solid #FF9800;
                        margin: 20px 0;
                    }
                    .info-item { margin: 10px 0; }
                    .footer { margin-top: 20px; color: #666; font-size: 12px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="alert-header">
                        <h2 style="margin: 0;">🔔 Nouvelle connexion détectée</h2>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Une nouvelle connexion a été effectuée sur votre compte AgroFlow :</p>
                        <div class="info-box">
                            <div class="info-item"><strong>📅 Date et heure :</strong> %s</div>
                            <div class="info-item"><strong>🌐 Adresse IP :</strong> %s</div>
                        </div>
                        <p><strong>Si c'était vous :</strong> Aucune action nécessaire.</p>
                        <p><strong>Si ce n'était pas vous :</strong> Changez immédiatement votre mot de passe et contactez notre support.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 AgroFlow · Tous droits réservés</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName, dateTime, ipAddress);

        return sendHtmlEmail(toEmail, subject, htmlBody);
    }

    /**
     * Envoie un email de bienvenue pour nouveau compte
     * @param toEmail Email du nouveau utilisateur
     * @param userName Nom de l'utilisateur
     * @param role Rôle de l'utilisateur (1, 2 ou 3)
     * @return true si l'envoi a réussi
     */
    public boolean sendWelcomeEmail(String toEmail, String userName, int role) {
        String subject = "🎉 Bienvenue sur AgroFlow !";

        String roleText = switch(role) {
            case 1 -> "Utilisateur/Agriculteur";
            case 2 -> "Employé";
            case 3 -> "Administrateur";
            default -> "Utilisateur";
        };

        String htmlBody = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { 
                        background: linear-gradient(135deg, #66BB6A 0%%, #2E7D32 100%%);
                        color: white; 
                        padding: 40px 20px; 
                        text-align: center; 
                        border-radius: 10px;
                    }
                    .content { padding: 30px 20px; }
                    .feature-box {
                        background: #f5f5f5;
                        padding: 15px;
                        margin: 10px 0;
                        border-left: 4px solid #4CAF50;
                        border-radius: 4px;
                    }
                    .footer { margin-top: 30px; color: #666; font-size: 12px; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1 style="margin: 0;">🌱 Bienvenue sur AgroFlow !</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Votre compte AgroFlow a été créé avec succès en tant que <strong>%s</strong> !</p>
                        <h3>🚀 Commencez dès maintenant :</h3>
                        <div class="feature-box">
                            <strong>🌾 Gérez vos terrains et cultures</strong><br>
                            Suivez l'état de vos parcelles en temps réel
                        </div>
                        <div class="feature-box">
                            <strong>📊 Suivi des ressources</strong><br>
                            Optimisez l'utilisation de vos ressources
                        </div>
                        <div class="feature-box">
                            <strong>👥 Gestion d'équipe</strong><br>
                            Coordonnez vos employés et affectations
                        </div>
                        <p style="margin-top: 30px;">Connectez-vous à votre compte pour découvrir toutes les fonctionnalités !</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 AgroFlow · Tous droits réservés</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName, roleText);

        return sendHtmlEmail(toEmail, subject, htmlBody);
    }

    /**
     * Envoie une notification de changement de mot de passe
     * @param toEmail Email du destinataire
     * @param userName Nom de l'utilisateur
     * @return true si l'envoi a réussi
     */
    public boolean sendPasswordChangedNotification(String toEmail, String userName) {
        String subject = "✅ AgroFlow - Mot de passe modifié";

        String htmlBody = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .success-header { 
                        background: #4CAF50;
                        color: white; 
                        padding: 20px; 
                        text-align: center; 
                        border-radius: 8px;
                    }
                    .content { padding: 20px; }
                    .warning {
                        background: #FFF3E0;
                        border-left: 4px solid #FF9800;
                        padding: 15px;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success-header">
                        <h2 style="margin: 0;">✅ Mot de passe modifié</h2>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Votre mot de passe AgroFlow a été modifié avec succès.</p>
                        <div class="warning">
                            <strong>⚠️ Si vous n'êtes pas à l'origine de cette modification :</strong><br>
                            Contactez immédiatement notre support technique.
                        </div>
                        <p>Pour toute question, notre équipe est à votre disposition.</p>
                    </div>
                    <div style="margin-top: 20px; color: #666; font-size: 12px; text-align: center;">
                        <p>© 2025 AgroFlow · Tous droits réservés</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName);

        return sendHtmlEmail(toEmail, subject, htmlBody);
    }
}