package services.User;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * Service d'envoi de SMS via Twilio
 * Utilisé pour envoyer les codes OTP lors de la connexion 2FA
 */
public class SmsService {

    // ⚠️ REMPLACE CES VALEURS PAR TES VRAIES CREDENTIALS TWILIO
    private static final String ACCOUNT_SID  = "ACf729486344cda9e578a317f2151b0549";
    private static final String AUTH_TOKEN   = "8b7f4156b8784226291ae803e3ab191f";
    private static final String FROM_NUMBER  = "+18285225880"; // Ton numéro Twilio

    public SmsService() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    /**
     * Envoie un code OTP par SMS
     * @param toPhoneNumber Numéro du destinataire au format international (+21612345678)
     * @param otp           Code à 6 chiffres à envoyer
     * @return true si le SMS a été envoyé avec succès
     */
    public boolean sendOtpCode(String toPhoneNumber, String otp) {
        try {
            Message message = Message.creator(
                    new PhoneNumber("+216"+toPhoneNumber),
                    new PhoneNumber(FROM_NUMBER),
                    "AgroFlow - Votre code de connexion : " + otp +
                            "\nCe code expire dans 5 minutes. Ne le partagez jamais."
            ).create();

            System.out.println("✅ SMS envoyé à " + "+216"+toPhoneNumber + " | SID: " + message.getSid());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi SMS: " + e.getMessage());
            return false;
        }
    }
}