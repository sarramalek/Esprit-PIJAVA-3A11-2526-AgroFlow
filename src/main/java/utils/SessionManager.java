package utils;

import models.User.Personne;

/**
 * Gestionnaire de session utilisateur
 * Stocke l'utilisateur connecté et l'OTP temporaire pour la 2FA SMS
 */
public class SessionManager {

    private static Personne currentUser;
    private static String tempOtp;  // OTP temporaire pour la vérification SMS

    // ── Utilisateur courant ──────────────────────────────────────────

    public static void setCurrentUser(Personne user) {
        currentUser = user;
    }

    public static Personne getCurrentUser() {
        return currentUser;
    }

    public static void clearSession() {
        currentUser = null;
        tempOtp     = null;
    }

    // ── OTP temporaire SMS ───────────────────────────────────────────

    public static void setTempOtp(String otp) {
        tempOtp = otp;
    }

    public static String getTempOtp() {
        return tempOtp;
    }

    public static void clearTempOtp() {
        tempOtp = null;
    }

    // ── Utilitaires ──────────────────────────────────────────────────

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}