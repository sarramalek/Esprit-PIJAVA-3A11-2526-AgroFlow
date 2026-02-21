package services.User;

import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Service pour enregistrer l'historique des connexions dans login_history
 */
public class LoginHistoryService {

    private Connection connection;

    public LoginHistoryService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Enregistre une tentative de connexion (réussie ou échouée)
     *
     * @param userCin        CIN de l'utilisateur (null si inconnu)
     * @param email          Email utilisé lors de la tentative
     * @param ipAddress      Adresse IP du client
     * @param userAgent      Navigateur/OS utilisé (optionnel)
     * @param success        true = connexion réussie, false = échec
     * @param twoFactorUsed  true si la 2FA a été utilisée
     * @param failureReason  Raison de l'échec (null si succès)
     */
    public void saveLoginHistory(Integer userCin, String email, String ipAddress,
                                 String userAgent, boolean success,
                                 boolean twoFactorUsed, String failureReason) {
        String sql = "INSERT INTO login_history " +
                "(user_cin, email, login_time, ip_address, user_agent, success, two_factor_used, failure_reason) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (userCin != null) {
                ps.setInt(1, userCin);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, email);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, ipAddress);
            ps.setString(5, userAgent);
            ps.setBoolean(6, success);
            ps.setBoolean(7, twoFactorUsed);
            ps.setString(8, failureReason);

            ps.executeUpdate();
            System.out.println("📋 Login history saved — success: " + success + ", email: " + email);

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la sauvegarde du login history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Raccourci pour une connexion réussie sans 2FA
     */
    public void saveSuccessfulLogin(int userCin, String email, String ipAddress) {
        saveLoginHistory(userCin, email, ipAddress, null, true, false, null);
    }

    /**
     * Raccourci pour une connexion réussie avec 2FA
     */
    public void saveSuccessfulLoginWith2FA(int userCin, String email, String ipAddress) {
        saveLoginHistory(userCin, email, ipAddress, null, true, true, null);
    }

    /**
     * Raccourci pour une tentative échouée
     */
    public void saveFailedLogin(String email, String ipAddress, String reason) {
        saveLoginHistory(null, email, ipAddress, null, false, false, reason);
    }
}