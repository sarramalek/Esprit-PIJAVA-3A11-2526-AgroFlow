package services.User;

import models.User.Employe;
import models.User.Tache;
import utils.MyDatabase;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des Ouvriers et de leurs Tâches.
 * FIX: Suppression de toute référence à ouvrier_terrain.
 *      L'association ouvrier ↔ terrain est stockée directement
 *      dans la colonne id_terrain de la table users.
 */
public class OuvrierService {

    private final Connection connection;
    private final EmailService emailService;

    public OuvrierService() {
        this.connection   = MyDatabase.getInstance().getConnection();
        this.emailService = new EmailService();
    }

    // ═══════════════════════════════════════════════════════════════════
    // GESTION DES OUVRIERS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Ajoute un ouvrier avec id_terrain dans la table users,
     * et envoie ses identifiants par email.
     * Le mot de passe par défaut = numéro de téléphone.
     */
    public void ajouterOuvrier(Employe o, int cinAgriculteur) throws Exception {
        // Mot de passe = numéro de téléphone (hashé ou non selon votre logique)
        String mdp = o.getTel();

        String sql = """
            INSERT INTO users 
                (cin, nom, prenom, tel, email, adresse, ville,
                 date_naiss, id_terrain, role, mdp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?)
            """;

        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt   (1,  o.getCin());
            pst.setString(2,  o.getNom());
            pst.setString(3,  o.getPrenom());
            pst.setString(4,  o.getTel());
            pst.setString(5,  o.getEmail());
            pst.setString(6,  o.getAdresse());
            pst.setString(7,  o.getVille());
            pst.setString(8,  o.getDate_naiss());
            pst.setInt   (9,  o.getIdTerrain());  // ← lien indirect à l'agriculteur
            pst.setString(10, mdp);
            pst.executeUpdate();
        }

        // Envoi email si vous avez cette logique
        // EmailService.sendCredentials(o.getEmail(), o.getCin(), mdp);
    }

    /**
     * Envoie un email HTML à l'ouvrier avec ses identifiants de connexion.
     */
    private void envoyerEmailIdentifiants(Employe ouvrier, String plainPassword) {
        String subject = "🌱 AgroFlow - Vos identifiants de connexion";

        String html = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                <style>
                  body { font-family:'Segoe UI',Arial,sans-serif; color:#333; margin:0; padding:0; }
                  .container { max-width:600px; margin:0 auto; padding:20px; background:#f9f9f9; }
                  .header { background:linear-gradient(135deg,#2E7D32,#43A047);
                             color:white; padding:30px 20px; text-align:center;
                             border-radius:10px 10px 0 0; }
                  .content { background:white; padding:30px; border-radius:0 0 10px 10px; }
                  .box { background:#F1F8E9; border:2px solid #4CAF50; border-radius:8px;
                         padding:20px; margin:20px 0; }
                  .row { margin:10px 0; }
                  .label { font-weight:bold; color:#2E7D32; display:inline-block; width:150px; }
                  .val { background:#fff; border:1px solid #c8e6c9; padding:6px 12px;
                         border-radius:5px; font-family:monospace; font-size:15px; }
                  .warn { background:#FFF3E0; border-left:4px solid #FF9800;
                          padding:12px; margin:15px 0; border-radius:4px; }
                  .footer { margin-top:20px; color:#888; font-size:12px; text-align:center; }
                </style>
                </head>
                <body>
                <div class="container">
                  <div class="header">
                    <h1 style="margin:0;">🌾 AgroFlow</h1>
                    <p style="margin:8px 0 0;">Bienvenue dans l'équipe !</p>
                  </div>
                  <div class="content">
                    <p>Bonjour <strong>%s %s</strong>,</p>
                    <p>Votre compte ouvrier a été créé. Voici vos identifiants :</p>
                    <div class="box">
                      <div class="row">
                        <span class="label">📧 Email :</span>
                        <span class="val">%s</span>
                      </div>
                      <div class="row" style="margin-top:10px;">
                        <span class="label">🔑 Mot de passe :</span>
                        <span class="val">%s</span>
                      </div>
                    </div>
                    <div class="warn">
                      <strong>⚠️ Votre mot de passe par défaut est votre numéro de téléphone.</strong><br>
                      Veuillez le modifier dès votre première connexion.
                    </div>
                    <p>Ouvrez l'application AgroFlow et connectez-vous avec ces identifiants.</p>
                  </div>
                  <div class="footer">© 2025 AgroFlow · Tous droits réservés</div>
                </div>
                </body>
                </html>
                """,
                ouvrier.getPrenom(), ouvrier.getNom(),
                ouvrier.getEmail(), plainPassword);

        boolean sent = emailService.sendHtmlEmail(ouvrier.getEmail(), subject, html);
        System.out.println(sent
                ? "✅ Email envoyé à " + ouvrier.getEmail()
                : "❌ Échec envoi email à " + ouvrier.getEmail());
    }

    /**
     * FIX: Récupère tous les ouvriers d'un agriculteur
     * directement depuis users.cin_agriculteur — sans ouvrier_terrain.
     */
    public List<Employe> getOuvriersParAgriculteur(int cinAgriculteur) throws SQLException {
        List<Employe> list = new ArrayList<>();
        String q = """
            SELECT u.* FROM users u
            INNER JOIN terrain t ON u.id_terrain = t.id_terrain
            WHERE t.cin = ? AND u.role = 1
            """;
        System.out.println("🔍 Chargement ouvriers pour agriculteur CIN: " + cinAgriculteur);
        try (PreparedStatement pst = connection.prepareStatement(q)) {
            pst.setInt(1, cinAgriculteur);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapOuvrier(rs));
            }
        }
        System.out.println("✅ Ouvriers trouvés: " + list.size());
        return list;
    }

    /**
     * Récupère le CIN de l'agriculteur propriétaire pour un ouvrier donné.
     */
    public int getAgriculteurCinForOuvrier(int cinOuvrier) throws SQLException {
        String q = """
            SELECT t.cin 
            FROM users u
            INNER JOIN terrain t ON u.id_terrain = t.id_terrain
            WHERE u.cin = ?
            """;
        try (PreparedStatement pst = connection.prepareStatement(q)) {
            pst.setInt(1, cinOuvrier);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) return rs.getInt("cin");
            }
        }
        return -1;
    }

    /**
     * FIX: Supprime un ouvrier sans toucher à ouvrier_terrain.
     */
    public void supprimerOuvrier(int cinOuvrier) throws SQLException {
        run("DELETE FROM taches WHERE assignee = ?", cinOuvrier);
        run("DELETE FROM users  WHERE cin      = ?", cinOuvrier);
        System.out.println("✅ Ouvrier supprimé: CIN " + cinOuvrier);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GESTION DES TÂCHES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Insère une nouvelle tâche dans la table taches.
     */
    public void assignerTache(Tache t) throws SQLException {
        String q = """
                INSERT INTO taches (nom_tache, description, etat, priorite, date_echeancee, assignee)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement pst = connection.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, t.getNomTache());
            pst.setString(2, t.getDescription());
            pst.setString(3, t.getEtat()     != null ? t.getEtat()     : "EN_ATTENTE");
            pst.setString(4, t.getPriorite() != null ? t.getPriorite() : "NORMALE");
            pst.setDate(5, t.getDateEcheance() != null
                    ? Date.valueOf(t.getDateEcheance()) : null);
            pst.setInt(6, t.getAssignee());
            pst.executeUpdate();

            try (ResultSet keys = pst.getGeneratedKeys()) {
                if (keys.next()) t.setId(keys.getInt(1));
            }
        }
        System.out.println("✅ Tâche assignée: " + t.getNomTache()
                + " → ouvrier CIN " + t.getAssignee());
    }

    /**
     * Récupère toutes les tâches d'un ouvrier donné.
     */
    public List<Tache> getTachesParOuvrier(int cinOuvrier) throws SQLException {
        return queryTaches(
                "SELECT * FROM taches WHERE assignee = ? ORDER BY date_echeancee",
                cinOuvrier);
    }

    /**
     * FIX: Récupère toutes les tâches des ouvriers d'un agriculteur
     * via users.cin_agriculteur — sans jointure ouvrier_terrain.
     */
    public List<Tache> getTachesParAgriculteur(int cinAgriculteur) throws SQLException {
        String q = """
            SELECT t.*
            FROM taches t
            JOIN users u ON t.assignee = u.cin
            JOIN terrain ter ON u.id_terrain = ter.id_terrain
            WHERE ter.cin = ? AND u.role = 1
            ORDER BY t.date_echeancee
            """;
        return queryTaches(q, cinAgriculteur);
    }

    /**
     * Change l'état d'une tâche.
     */
    public void modifierEtatTache(int idTache, String nouvelEtat) throws SQLException {
        String q = "UPDATE taches SET etat = ? WHERE id_tache = ?";
        try (PreparedStatement pst = connection.prepareStatement(q)) {
            pst.setString(1, nouvelEtat);
            pst.setInt(2, idTache);
            pst.executeUpdate();
        }
    }

    /**
     * Supprime une tâche.
     */
    public void supprimerTache(int idTache) throws SQLException {
        run("DELETE FROM taches WHERE id_tache = ?", idTache);
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPERS PRIVÉS
    // ═══════════════════════════════════════════════════════════════════

    private void run(String sql, int param) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, param);
            pst.executeUpdate();
        }
    }

    private List<Tache> queryTaches(String sql, int param) throws SQLException {
        List<Tache> list = new ArrayList<>();
        try (PreparedStatement pst = connection.prepareStatement(sql)) {
            pst.setInt(1, param);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) list.add(mapTache(rs));
            }
        }
        return list;
    }

    Employe mapOuvrier(ResultSet rs) throws SQLException {
        Employe o = new Employe();
        o.setCin(rs.getInt("cin"));
        o.setNom(rs.getString("nom"));
        o.setPrenom(rs.getString("prenom"));
        o.setTel(rs.getString("tel"));
        o.setEmail(rs.getString("email"));
        o.setAdresse(rs.getString("adresse"));
        o.setVille(rs.getString("ville"));
        o.setIdTerrain(rs.getInt("id_terrain"));

        // Colonnes optionnelles — protégées contre SQLException
        try { o.setDate_naiss(rs.getString("date_naiss")); } catch (SQLException ignored) {}
        try { o.setMdp(rs.getString("mdp")); } catch (SQLException ignored) {}
        try { o.setPhotoUrl(rs.getString("img")); } catch (SQLException ignored) {}
        try { o.setDate_creationcpt(rs.getString("date_creationcpt")); } catch (SQLException ignored) {}
        try { o.setDate_dernierchg(rs.getString("date_dernierchg")); } catch (SQLException ignored) {}

        return o;
    }

    private Tache mapTache(ResultSet rs) throws SQLException {
        Tache t = new Tache();
        t.setId(rs.getInt("id_tache"));
        t.setNomTache(rs.getString("nom_tache"));
        t.setDescription(rs.getString("description"));
        t.setEtat(rs.getString("etat"));
        t.setPriorite(rs.getString("priorite"));
        Date d = rs.getDate("date_echeancee");
        if (d != null) t.setDateEcheance(d.toLocalDate());
        t.setAssignee(rs.getInt("assignee"));
        return t;
    }
}