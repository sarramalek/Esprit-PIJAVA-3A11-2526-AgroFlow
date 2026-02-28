package services.Materiels;

import models.Materiels.Achat;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class AntiFraudeService {

    private static final int SEUIL_QUANTITE        = 100;
    private static final int SEUIL_ACHATS_PAR_JOUR = 3;

    private final Connection connection;

    public AntiFraudeService() {
        this.connection = MyDatabase.getInstance().getConnection();
        creerTableSiAbsente();
    }

    // ═══════════════════════════════════════════════════════════════
    //  ENUM
    // ═══════════════════════════════════════════════════════════════
    public enum TypeAlerte {
        QUANTITE_EXCESSIVE,
        ACHATS_MULTIPLES_JOUR
    }

    // ═══════════════════════════════════════════════════════════════
    //  FABRIQUE D'ALERTE
    // ═══════════════════════════════════════════════════════════════
    public static Map<String, Object> nouvelleAlerte(
            int idAchat, int cin, TypeAlerte type, String description) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("idAlerte",       0);
        a.put("idAchat",        idAchat);   // ← achat.getIdAchat()
        a.put("cin",            cin);        // ← achat.getCin()
        a.put("dateDetection",  LocalDate.now());
        a.put("heureDetection", LocalDateTime.now());
        a.put("typeAlerte",     type);
        a.put("description",    description);
        a.put("traitee",        false);
        return a;
    }

    // ═══════════════════════════════════════════════════════════════
    //  ANALYSE D'UN ACHAT
    // ═══════════════════════════════════════════════════════════════
    public List<Map<String, Object>> analyserAchat(Achat achat) throws SQLException {
        List<Map<String, Object>> alertes = new ArrayList<>();

        // ── Règle 1 : quantite > 100 ─────────────────────────────
        if (achat.getQuantite() > SEUIL_QUANTITE) {
            if (!alerteExiste(achat.getIdAchat(), TypeAlerte.QUANTITE_EXCESSIVE)) {
                Map<String, Object> a = nouvelleAlerte(
                        achat.getIdAchat(),
                        achat.getCin(),
                        TypeAlerte.QUANTITE_EXCESSIVE,
                        String.format(
                                "Quantité excessive : %d unités (seuil : %d) — Achat #%d — CIN %d",
                                achat.getQuantite(),   // ← getQuantite()
                                SEUIL_QUANTITE,
                                achat.getIdAchat(),    // ← getIdAchat()
                                achat.getCin())        // ← getCin()
                );
                enregistrerAlerte(a);
                alertes.add(a);
            }
        }

        // ── Règle 2 : même client ≥ 3 achats le même jour ────────
        int nb = compterAchatsClientJour(achat.getCin(), achat.getDateAchat());
        if (nb >= SEUIL_ACHATS_PAR_JOUR) {
            if (!alerteExiste(achat.getIdAchat(), TypeAlerte.ACHATS_MULTIPLES_JOUR)) {
                Map<String, Object> a = nouvelleAlerte(
                        achat.getIdAchat(),
                        achat.getCin(),
                        TypeAlerte.ACHATS_MULTIPLES_JOUR,
                        String.format(
                                "Achats multiples : %d achats le %s pour CIN %d (seuil : %d/jour)",
                                nb,
                                achat.getDateAchat(),  // ← getDateAchat()
                                achat.getCin(),        // ← getCin()
                                SEUIL_ACHATS_PAR_JOUR)
                );
                enregistrerAlerte(a);
                alertes.add(a);
            }
        }

        return alertes;
    }

    // ═══════════════════════════════════════════════════════════════
    //  ANALYSE DE TOUS LES ACHATS
    // ═══════════════════════════════════════════════════════════════
    public List<Map<String, Object>> analyserTousLesAchats() throws SQLException {
        List<Map<String, Object>> toutes = new ArrayList<>();
        AchatService achatService = new AchatService();
        // recuperer() retourne List<Achat> — chaque Achat a :
        // idAchat, dateAchat, idM, cin, quantite
        for (Achat achat : achatService.recuperer()) {
            toutes.addAll(analyserAchat(achat));
        }
        return toutes;
    }

    // ═══════════════════════════════════════════════════════════════
    //  CRUD ALERTES
    // ═══════════════════════════════════════════════════════════════
    public List<Map<String, Object>> recupererAlertes() throws SQLException {
        return lireAlertes(
                "SELECT * FROM alerte_fraude ORDER BY heure_detection DESC");
    }

    public List<Map<String, Object>> recupererAlertesNonTraitees() throws SQLException {
        return lireAlertes(
                "SELECT * FROM alerte_fraude WHERE traitee = false ORDER BY heure_detection DESC");
    }

    public void marquerCommeTraitee(int idAlerte) throws SQLException {
        String sql = "UPDATE alerte_fraude SET traitee = true WHERE id_alerte = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAlerte);
            ps.executeUpdate();
        }
    }

    public void supprimerAlerte(int idAlerte) throws SQLException {
        String sql = "DELETE FROM alerte_fraude WHERE id_alerte = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAlerte);
            ps.executeUpdate();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS PRIVÉS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Vérifie si une alerte existe déjà pour éviter les doublons
     * lors de clics multiples sur "Analyser"
     */
    private boolean alerteExiste(int idAchat, TypeAlerte type) throws SQLException {
        String sql = "SELECT COUNT(*) FROM alerte_fraude WHERE id_achat = ? AND type_alerte = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAchat);          // ← idAchat de l'entité Achat
            ps.setString(2, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Compte le nombre d'achats d'un client (cin) sur une date donnée
     * Utilise dateAchat de l'entité Achat
     */
    private int compterAchatsClientJour(int cin, LocalDate dateAchat) throws SQLException {
        String sql = "SELECT COUNT(*) FROM achat WHERE cin = ? AND DATE(dateAchat) = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cin);                                    // ← achat.getCin()
            ps.setDate(2, java.sql.Date.valueOf(dateAchat));      // ← achat.getDateAchat()
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void enregistrerAlerte(Map<String, Object> alerte) {
        String sql = """
                INSERT INTO alerte_fraude
                    (id_achat, cin, date_detection, heure_detection, type_alerte, description, traitee)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,       (int)          alerte.get("idAchat"));   // ← idAchat
            ps.setInt(2,       (int)          alerte.get("cin"));        // ← cin
            ps.setDate(3,      java.sql.Date.valueOf(
                    (LocalDate)    alerte.get("dateDetection")));
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(
                    (LocalDateTime) alerte.get("heureDetection")));
            ps.setString(5,    ((TypeAlerte)  alerte.get("typeAlerte")).name());
            ps.setString(6,    (String)       alerte.get("description"));
            ps.setBoolean(7,   (boolean)      alerte.get("traitee"));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) alerte.put("idAlerte", keys.getInt(1));
            }
        } catch (SQLException e) {
            System.err.println("Erreur enregistrement alerte : " + e.getMessage());
        }
    }

    private List<Map<String, Object>> lireAlertes(String sql) throws SQLException {
        List<Map<String, Object>> liste = new ArrayList<>();
        try (Statement st  = connection.createStatement();
             ResultSet  rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> a = new LinkedHashMap<>();
                a.put("idAlerte",       rs.getInt("id_alerte"));
                a.put("idAchat",        rs.getInt("id_achat"));
                a.put("cin",            rs.getInt("cin"));
                a.put("dateDetection",  rs.getDate("date_detection").toLocalDate());
                a.put("heureDetection", rs.getTimestamp("heure_detection").toLocalDateTime());
                a.put("typeAlerte",     TypeAlerte.valueOf(rs.getString("type_alerte")));
                a.put("description",    rs.getString("description"));
                a.put("traitee",        rs.getBoolean("traitee"));
                liste.add(a);
            }
        }
        return liste;
    }

    private void creerTableSiAbsente() {
        String ddl = """
                CREATE TABLE IF NOT EXISTS alerte_fraude (
                    id_alerte        INT AUTO_INCREMENT PRIMARY KEY,
                    id_achat         INT NOT NULL,
                    cin              INT NOT NULL,
                    date_detection   DATE NOT NULL,
                    heure_detection  DATETIME NOT NULL,
                    type_alerte      VARCHAR(50) NOT NULL,
                    description      TEXT,
                    traitee          BOOLEAN DEFAULT FALSE
                )
                """;
        try (Statement st = connection.createStatement()) {
            st.execute(ddl);
        } catch (SQLException e) {
            System.err.println("Erreur création table : " + e.getMessage());
        }
    }
}