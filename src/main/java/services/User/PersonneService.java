package services.User;

import models.User.Admin;
import models.User.Employe;
import models.User.Personne;
import models.User.Utilisateur;
import services.IService;
import utils.MyDatabase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des personnes avec sécurité renforcée
 * Intègre BCrypt pour le hachage des mots de passe
 */
public class PersonneService implements IService<Personne> {
    private Connection connection;

    public PersonneService() {
        connection = MyDatabase.getInstance().getConnection();
        if (connection != null) {
            System.out.println("PersonneService: Connection initialized successfully!");
        } else {
            System.err.println("PersonneService: Connection is NULL!");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // API 1: BCRYPT - HACHAGE SÉCURISÉ DES MOTS DE PASSE
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Hache un mot de passe avec BCrypt
     * @param plainPassword Mot de passe en clair
     * @return Hash BCrypt du mot de passe
     */
    public String hashPassword(String plainPassword) {
        // Génère un salt et hache le mot de passe
        // Le coût 12 est un bon équilibre entre sécurité et performance
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    /**
     * Vérifie un mot de passe contre son hash BCrypt
     * @param plainPassword Mot de passe en clair à vérifier
     * @param hashedPassword Hash BCrypt stocké en base
     * @return true si le mot de passe correspond
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification du mot de passe: " + e.getMessage());
            return false;
        }
    }

    /**
     * Authentification sécurisée avec BCrypt
     * Le rôle est automatiquement récupéré depuis la base de données
     */
    public Personne authenticate(String email, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("mdp");

                    // Vérifier le mot de passe avec BCrypt
                    if (verifyPassword(password, hashedPassword)) {
                        Personne personne = createPersonneFromResultSet(rs);
                        System.out.println("✅ Authentification réussie pour: " + email + " (Rôle: " + personne.getRole() + ")");
                        return personne;
                    } else {
                        System.out.println("❌ Mot de passe incorrect pour: " + email);
                        return null;
                    }
                } else {
                    System.out.println("❌ Email non trouvé: " + email);
                    return null;
                }
            }
        }
    }

    /**
     * Authentification simple (sans BCrypt) - Pour compatibilité
     * À utiliser si les mots de passe ne sont pas encore hashés
     */
    public Personne authenticateSimple(String email, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ? AND mdp = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setString(2, password);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Personne personne = createPersonneFromResultSet(rs);
                    System.out.println("✅ Authentification simple réussie pour: " + email);
                    return personne;
                }
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // MÉTHODES CRUD AMÉLIORÉES AVEC BCRYPT
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void ajouter(Personne personne) throws SQLException {
        // Hasher le mot de passe avant l'insertion
        String hashedPassword = hashPassword(personne.getMdp());

        String query = "INSERT INTO users (cin, nom, prenom, tel, date_naiss, email, mdp, adresse, ville, role, date_creationcpt, date_dernierchg) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, personne.getCin());
            pst.setString(2, personne.getNom());
            pst.setString(3, personne.getPrenom());
            pst.setString(4, personne.getTel());
            pst.setString(5, personne.getDate_naiss());
            pst.setString(6, personne.getEmail());
            pst.setString(7, hashedPassword); // Mot de passe hashé avec BCrypt
            pst.setString(8, personne.getAdresse());
            pst.setString(9, personne.getVille());
            pst.setInt(10, personne.getRole());
            pst.setString(11, personne.getDate_creationcpt());
            pst.setString(12, personne.getDate_dernierchg());
            pst.executeUpdate();

            System.out.println("✅ Utilisateur ajouté avec mot de passe sécurisé (BCrypt)");
        }
    }

    @Override
    public void modifier(Personne personne) throws SQLException {
        // Vérifier si le mot de passe a changé (si c'est un nouveau mot de passe en clair)
        String passwordToSave = personne.getMdp();

        // Si le mot de passe ne commence pas par "$2a$" (format BCrypt), le hasher
        if (!passwordToSave.startsWith("$2a$")) {
            passwordToSave = hashPassword(passwordToSave);
            System.out.println("🔐 Nouveau mot de passe hashé avec BCrypt");
        }

        String query = "UPDATE users SET nom=?, prenom=?, tel=?, date_naiss=?, email=?, mdp=?, " +
                "adresse=?, ville=?, role=?, date_creationcpt=?, date_dernierchg=? WHERE cin=?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, personne.getNom());
            pst.setString(2, personne.getPrenom());
            pst.setString(3, personne.getTel());
            pst.setString(4, personne.getDate_naiss());
            pst.setString(5, personne.getEmail());
            pst.setString(6, passwordToSave);
            pst.setString(7, personne.getAdresse());
            pst.setString(8, personne.getVille());
            pst.setInt(9, personne.getRole());
            pst.setString(10, personne.getDate_creationcpt());
            pst.setString(11, personne.getDate_dernierchg());
            pst.setInt(12, personne.getCin());
            pst.executeUpdate();
        }
    }

    /**
     * Change le mot de passe d'un utilisateur
     * @param cin CIN de l'utilisateur
     * @param oldPassword Ancien mot de passe
     * @param newPassword Nouveau mot de passe
     * @return true si le changement a réussi
     */
    public boolean changePassword(int cin, String oldPassword, String newPassword) throws SQLException {
        // Récupérer l'utilisateur
        Personne personne = rechercherParId(cin);
        if (personne == null) {
            return false;
        }

        // Vérifier l'ancien mot de passe
        if (!verifyPassword(oldPassword, personne.getMdp())) {
            System.out.println("❌ Ancien mot de passe incorrect");
            return false;
        }

        // Hasher et sauvegarder le nouveau mot de passe
        String hashedNewPassword = hashPassword(newPassword);
        String query = "UPDATE users SET mdp = ?, date_dernierchg = NOW() WHERE cin = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, hashedNewPassword);
            pst.setInt(2, cin);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Mot de passe changé avec succès");
                return true;
            }
        }

        return false;
    }

    /**
     * Génère un code de réinitialisation et le sauvegarde en base
     * @param email Email de l'utilisateur
     * @return Le code généré (à envoyer par email)
     */
    public String generateResetCode(String email) throws SQLException {
        // Générer un code à 6 chiffres
        String code = String.format("%06d", (int)(Math.random() * 999999));

        // Sauvegarder en base avec expiration de 15 minutes
        String query = "INSERT INTO password_resets (email, code, expires_at) " +
                "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 15 MINUTE)) " +
                "ON DUPLICATE KEY UPDATE code = ?, expires_at = DATE_ADD(NOW(), INTERVAL 15 MINUTE)";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setString(2, code);
            pst.setString(3, code);
            pst.executeUpdate();

            System.out.println("✅ Code de réinitialisation généré: " + code);
            return code;
        }
    }

    /**
     * Vérifie un code de réinitialisation
     * @param email Email de l'utilisateur
     * @param code Code à vérifier
     * @return true si le code est valide et non expiré
     */
    public boolean verifyResetCode(String email, String code) throws SQLException {
        String query = "SELECT * FROM password_resets WHERE email = ? AND code = ? AND expires_at > NOW()";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setString(2, code);

            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Réinitialise le mot de passe avec un code
     * @param email Email de l'utilisateur
     * @param code Code de vérification
     * @param newPassword Nouveau mot de passe
     * @return true si la réinitialisation a réussi
     */
    public boolean resetPassword(String email, String code, String newPassword) throws SQLException {
        // Vérifier le code
        if (!verifyResetCode(email, code)) {
            System.out.println("❌ Code invalide ou expiré");
            return false;
        }

        // Hasher le nouveau mot de passe
        String hashedPassword = hashPassword(newPassword);

        // Mettre à jour le mot de passe
        String query = "UPDATE users SET mdp = ?, date_dernierchg = NOW() WHERE email = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, hashedPassword);
            pst.setString(2, email);
            int rows = pst.executeUpdate();

            if (rows > 0) {
                // Supprimer le code utilisé
                String deleteQuery = "DELETE FROM password_resets WHERE email = ?";
                try (PreparedStatement deletePst = connection.prepareStatement(deleteQuery)) {
                    deletePst.setString(1, email);
                    deletePst.executeUpdate();
                }

                System.out.println("✅ Mot de passe réinitialisé avec succès");
                return true;
            }
        }

        return false;
    }

    // ═══════════════════════════════════════════════════════════════════
    // MIGRATION DES MOTS DE PASSE EXISTANTS VERS BCRYPT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Migre tous les mots de passe en clair vers BCrypt
     * ATTENTION: À exécuter une seule fois !
     */
    public void migratePasswordsToBCrypt() throws SQLException {
        List<Personne> users = recuperer();
        int migrated = 0;

        for (Personne user : users) {
            String currentPassword = user.getMdp();

            // Vérifier si le mot de passe est déjà hashé avec BCrypt
            if (!currentPassword.startsWith("$2a$")) {
                // Hasher avec BCrypt
                String hashedPassword = hashPassword(currentPassword);

                // Mettre à jour en base
                String query = "UPDATE users SET mdp = ? WHERE cin = ?";
                try (PreparedStatement pst = connection.prepareStatement(query)) {
                    pst.setString(1, hashedPassword);
                    pst.setInt(2, user.getCin());
                    pst.executeUpdate();
                    migrated++;
                }
            }
        }

        System.out.println("✅ Migration terminée : " + migrated + " mots de passe hashés avec BCrypt");
    }

    // ═══════════════════════════════════════════════════════════════════
    // MÉTHODES EXISTANTES (INCHANGÉES)
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void supprimer(int cin) throws SQLException {
        String query = "DELETE FROM users WHERE cin=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, cin);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Personne> recuperer() throws SQLException {
        List<Personne> personnes = new ArrayList<>();
        String query = "SELECT * FROM users";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Personne personne = createPersonneFromResultSet(rs);
                personnes.add(personne);
            }
        }
        return personnes;
    }

    public Personne rechercherParId(int cin) throws SQLException {
        String query = "SELECT * FROM users WHERE cin=?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, cin);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return createPersonneFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public Personne rechercherParEmail(String email) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return createPersonneFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, email);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Personne createPersonneFromResultSet(ResultSet rs) throws SQLException {
        int role = rs.getInt("role");
        Personne personne;

        switch (role) {
            case 1:
                personne = new Utilisateur();
                break;
            case 2:
                personne = new Employe();
                break;
            case 3:
                personne = new Admin();
                break;
            default:
                personne = new Utilisateur();
        }

        personne.setCin(rs.getInt("cin"));
        personne.setNom(rs.getString("nom"));
        personne.setPrenom(rs.getString("prenom"));
        personne.setTel(rs.getString("tel"));
        personne.setDate_naiss(rs.getString("date_naiss"));
        personne.setEmail(rs.getString("email"));
        personne.setMdp(rs.getString("mdp"));
        personne.setAdresse(rs.getString("adresse"));
        personne.setVille(rs.getString("ville"));
        personne.setDate_creationcpt(rs.getString("date_creationcpt"));
        personne.setDate_dernierchg(rs.getString("date_dernierchg"));

        return personne;
    }

    public List<Utilisateur> getUtilisateurs() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM users WHERE role=1";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                utilisateurs.add((Utilisateur) createPersonneFromResultSet(rs));
            }
        }
        return utilisateurs;
    }

    public List<Employe> getEmployes() throws SQLException {
        List<Employe> employes = new ArrayList<>();
        String query = "SELECT * FROM users WHERE role=2";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                employes.add((Employe) createPersonneFromResultSet(rs));
            }
        }
        return employes;
    }

    public List<Admin> getAdmins() throws SQLException {
        List<Admin> admins = new ArrayList<>();
        String query = "SELECT * FROM users WHERE role=3";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                admins.add((Admin) createPersonneFromResultSet(rs));
            }
        }
        return admins;
    }
}