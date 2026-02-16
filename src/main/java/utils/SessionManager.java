package utils;

import models.Personne;
import models.Admin;
import models.Employe;
import models.Utilisateur;

/**
 * Gestionnaire de session pour stocker l'utilisateur connecté
 * Adapté au modèle Personne existant
 */
public class SessionManager {

    private static Personne currentUser = null;

    /**
     * Définit l'utilisateur actuellement connecté
     * @param user L'utilisateur connecté
     */
    public static void setCurrentUser(Personne user) {
        currentUser = user;
        if (user != null) {
            System.out.println("📝 Session créée pour: " + user.getEmail());
        }
    }

    /**
     * Obtient l'utilisateur actuellement connecté
     * @return L'utilisateur connecté ou null
     */
    public static Personne getCurrentUser() {
        return currentUser;
    }

    /**
     * Vérifie si un utilisateur est connecté
     * @return true si un utilisateur est connecté
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Déconnecte l'utilisateur actuel
     */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("👋 Déconnexion de: " + currentUser.getEmail());
            currentUser = null;
        }
    }

    /**
     * Obtient le rôle de l'utilisateur connecté
     * @return Le rôle (1=Utilisateur, 2=Employé, 3=Admin) ou -1 si pas connecté
     */
    public static int getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : -1;
    }

    /**
     * Vérifie si l'utilisateur connecté est un admin (role = 3)
     * @return true si admin
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == 3;
    }

    /**
     * Vérifie si l'utilisateur connecté est un utilisateur/agricole (role = 1)
     * @return true si utilisateur
     */
    public static boolean isUtilisateur() {
        return currentUser != null && currentUser.getRole() == 1;
    }

    /**
     * Vérifie si l'utilisateur connecté est un employé (role = 2)
     * @return true si employé
     */
    public static boolean isEmploye() {
        return currentUser != null && currentUser.getRole() == 2;
    }

    /**
     * Obtient le nom complet de l'utilisateur connecté
     * @return Le nom complet ou "Invité"
     */
    public static String getCurrentUserFullName() {
        return currentUser != null
                ? currentUser.getPrenom() + " " + currentUser.getNom()
                : "Invité";
    }

    /**
     * Obtient le CIN de l'utilisateur connecté
     * @return Le CIN ou -1 si pas connecté
     */
    public static int getCurrentUserCin() {
        return currentUser != null ? currentUser.getCin() : -1;
    }

    /**
     * Obtient l'email de l'utilisateur connecté
     * @return L'email ou null si pas connecté
     */
    public static String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }

    /**
     * Obtient le nom du rôle en texte
     * @return Le nom du rôle
     */
    public static String getCurrentUserRoleName() {
        if (currentUser == null) {
            return "Non connecté";
        }

        switch (currentUser.getRole()) {
            case 1: return "Utilisateur";
            case 2: return "Employé";
            case 3: return "Administrateur";
            default: return "Inconnu";
        }
    }

    /**
     * Vérifie si l'utilisateur a les permissions pour une action
     * @param requiredRole Le rôle minimum requis (1, 2, ou 3)
     * @return true si l'utilisateur a les permissions
     */
    public static boolean hasPermission(int requiredRole) {
        if (currentUser == null) {
            return false;
        }

        // Admin (3) a accès à tout
        // Employé (2) peut accéder aux fonctions de Utilisateur (1)
        return currentUser.getRole() >= requiredRole;
    }
}