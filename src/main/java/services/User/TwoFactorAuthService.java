package services.User;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service d'authentification à deux facteurs (2FA) avec Google Authenticator
 * Utilise TOTP (Time-based One-Time Password)
 */
public class TwoFactorAuthService {

    private GoogleAuthenticator gAuth;
    private Connection connection;

    public TwoFactorAuthService() {
        this.gAuth = new GoogleAuthenticator();
        this.connection = MyDatabase.getInstance().getConnection();

        if (connection != null) {
            System.out.println("TwoFactorAuthService: Connection initialized successfully!");
        } else {
            System.err.println("TwoFactorAuthService: Connection is NULL!");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ACTIVATION / DÉSACTIVATION DE LA 2FA
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Active la 2FA pour un utilisateur et génère une clé secrète
     * @param userCin CIN de l'utilisateur
     * @return La clé secrète à afficher (pour Google Authenticator)
     */
    public String enableTwoFactorAuth(int userCin) throws SQLException {
        // Générer une nouvelle clé secrète
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secretKey = key.getKey();

        // Sauvegarder en base de données
        String query = "UPDATE users SET two_factor_secret = ?, two_factor_enabled = 1 WHERE cin = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, secretKey);
            pst.setInt(2, userCin);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ 2FA activé pour l'utilisateur CIN: " + userCin);
                return secretKey;
            }
        }

        return null;
    }

    /**
     * Désactive la 2FA pour un utilisateur
     * @param userCin CIN de l'utilisateur
     */
    public void disableTwoFactorAuth(int userCin) throws SQLException {
        String query = "UPDATE users SET two_factor_secret = NULL, two_factor_enabled = 0 WHERE cin = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userCin);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ 2FA désactivé pour l'utilisateur CIN: " + userCin);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // GÉNÉRATION DU QR CODE
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Génère l'URL du QR Code pour Google Authenticator
     * @param email Email de l'utilisateur (affiché dans l'app)
     * @param secretKey Clé secrète générée
     * @return URL otpauth:// pour générer le QR Code
     */
    public String generateQRCodeUrl(String email, String secretKey) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                "AgroFlow",      // Nom de l'application (affiché dans Google Authenticator)
                email,           // Identifiant de l'utilisateur (généralement email)
                new GoogleAuthenticatorKey.Builder(secretKey).build()
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    // VÉRIFICATION DES CODES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Vérifie un code 2FA
     * @param secretKey Clé secrète de l'utilisateur
     * @param code Code à 6 chiffres entré par l'utilisateur
     * @return true si le code est valide
     */
    public boolean verifyCode(String secretKey, int code) {
        try {
            boolean isValid = gAuth.authorize(secretKey, code);
            if (isValid) {
                System.out.println("✅ Code 2FA valide");
            } else {
                System.out.println("❌ Code 2FA invalide");
            }
            return isValid;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification du code 2FA: " + e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie un code 2FA pour un utilisateur spécifique
     * @param userCin CIN de l'utilisateur
     * @param code Code à vérifier
     * @return true si le code est valide
     */
    public boolean verifyCodeForUser(int userCin, int code) throws SQLException {
        String secretKey = getSecretKey(userCin);
        if (secretKey == null) {
            return false;
        }
        return verifyCode(secretKey, code);
    }

    // ═══════════════════════════════════════════════════════════════════
    // REQUÊTES BASE DE DONNÉES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Vérifie si un utilisateur a la 2FA activée
     * @param userCin CIN de l'utilisateur
     * @return true si la 2FA est activée
     */
    public boolean isTwoFactorEnabled(int userCin) throws SQLException {
        String query = "SELECT two_factor_enabled FROM users WHERE cin = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userCin);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("two_factor_enabled");
                }
            }
        }
        return false;
    }

    /**
     * Récupère la clé secrète d'un utilisateur
     * @param userCin CIN de l'utilisateur
     * @return La clé secrète ou null
     */
    public String getSecretKey(int userCin) throws SQLException {
        String query = "SELECT two_factor_secret FROM users WHERE cin = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userCin);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("two_factor_secret");
                }
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // CODES DE SECOURS (BACKUP CODES)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Génère des codes de secours pour un utilisateur
     * Ces codes peuvent être utilisés si l'utilisateur perd son téléphone
     * @param userCin CIN de l'utilisateur
     * @param numberOfCodes Nombre de codes à générer (généralement 10)
     * @return Liste des codes générés
     */
    public String[] generateBackupCodes(int userCin, int numberOfCodes) throws SQLException {
        String[] backupCodes = new String[numberOfCodes];

        // Générer des codes aléatoires
        for (int i = 0; i < numberOfCodes; i++) {
            backupCodes[i] = String.format("%08d", (int)(Math.random() * 99999999));
        }

        // Les sauvegarder en base (hashés avec BCrypt pour plus de sécurité)
        PersonneService personneService = new PersonneService();
        StringBuilder allCodes = new StringBuilder();

        for (int i = 0; i < backupCodes.length; i++) {
            String hashedCode = personneService.hashPassword(backupCodes[i]);
            allCodes.append(hashedCode);
            if (i < backupCodes.length - 1) {
                allCodes.append(",");
            }
        }

        String query = "UPDATE users SET two_factor_backup_codes = ? WHERE cin = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, allCodes.toString());
            pst.setInt(2, userCin);
            pst.executeUpdate();
        }

        System.out.println("✅ " + numberOfCodes + " codes de secours générés");
        return backupCodes;
    }

    /**
     * Vérifie et utilise un code de secours
     * Le code ne peut être utilisé qu'une seule fois
     * @param userCin CIN de l'utilisateur
     * @param backupCode Code de secours à vérifier
     * @return true si le code est valide et a été utilisé
     */
    public boolean verifyAndUseBackupCode(int userCin, String backupCode) throws SQLException {
        // Récupérer les codes de secours
        String query = "SELECT two_factor_backup_codes FROM users WHERE cin = ?";
        String backupCodesStr = null;

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, userCin);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    backupCodesStr = rs.getString("two_factor_backup_codes");
                }
            }
        }

        if (backupCodesStr == null || backupCodesStr.isEmpty()) {
            return false;
        }

        // Vérifier si le code correspond
        PersonneService personneService = new PersonneService();
        String[] hashedCodes = backupCodesStr.split(",");

        for (int i = 0; i < hashedCodes.length; i++) {
            if (personneService.verifyPassword(backupCode, hashedCodes[i])) {
                // Code trouvé ! Le supprimer de la liste
                StringBuilder newCodes = new StringBuilder();
                for (int j = 0; j < hashedCodes.length; j++) {
                    if (j != i) {
                        if (newCodes.length() > 0) newCodes.append(",");
                        newCodes.append(hashedCodes[j]);
                    }
                }

                // Mettre à jour en base
                String updateQuery = "UPDATE users SET two_factor_backup_codes = ? WHERE cin = ?";
                try (PreparedStatement pst = connection.prepareStatement(updateQuery)) {
                    pst.setString(1, newCodes.toString());
                    pst.setInt(2, userCin);
                    pst.executeUpdate();
                }

                System.out.println("✅ Code de secours valide et utilisé");
                return true;
            }
        }

        System.out.println("❌ Code de secours invalide");
        return false;
    }
}