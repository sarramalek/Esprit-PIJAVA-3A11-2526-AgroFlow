package services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private static final String MON_EMAIL = "eyamallouli167@gmail.com";
    // Utilise le mot de passe sans espaces
    private static final String MON_PASSWORD = "fwwjnatzpwycgbue";

    // Changement du nom et des paramètres pour correspondre au contrôleur
    public static void envoyerMailAlerte(String nomArticle, double quantite) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MON_EMAIL, MON_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(MON_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(MON_EMAIL));
            message.setSubject("⚠️ ALERTE STOCK : " + nomArticle);

            String contenu = "<h3>Attention !</h3>"
                    + "<p>L'article <b>" + nomArticle + "</b> a atteint son seuil critique.</p>"
                    + "<p>Stock actuel : <span style='color:red;'>" + quantite + "</span></p>"
                    + "<p>Veuillez réapprovisionner rapidement.</p>"
                    + "<br><hr><p>AgroFlow Management System</p>";

            message.setContent(contenu, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email d'alerte envoyé pour " + nomArticle);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}