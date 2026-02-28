package services.Materiels;

import models.Materiels.Maintenance;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Service API direct — sans serveur HTTP
 * Remplace MaintenanceApiController + MaintenanceApiServer
 */
public class MaintenanceApiService {

    private final Connection connection;

    public MaintenanceApiService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    // ═══════════════════════════════════════════════════════════════
    //  RÉSULTAT STRUCTURÉ
    // ═══════════════════════════════════════════════════════════════
    public static class ResultatCout {
        public final double  total;
        public final double  moyenne;
        public final double  min;
        public final double  max;
        public final int     nombre;
        public final String  nomMachine;
        public final List<Map<String, Object>> maintenances;

        public ResultatCout(double total, double moyenne, double min,
                            double max, int nombre, String nomMachine,
                            List<Map<String, Object>> maintenances) {
            this.total        = total;
            this.moyenne      = moyenne;
            this.min          = min;
            this.max          = max;
            this.nombre       = nombre;
            this.nomMachine   = nomMachine;
            this.maintenances = maintenances;
        }
    }

    public static class ResultatAlerte {
        public final List<Map<String, Object>> alertes;
        public final double  seuilApplique;
        public final double  moyenneGlobale;
        public final double  coutTotal;
        public final int     nbTotal;
        public final Map<String, Integer> parType;
        public final Map<String, Integer> parMachine;

        public ResultatAlerte(List<Map<String, Object>> alertes,
                              double seuilApplique, double moyenneGlobale,
                              double coutTotal, int nbTotal,
                              Map<String, Integer> parType,
                              Map<String, Integer> parMachine) {
            this.alertes        = alertes;
            this.seuilApplique  = seuilApplique;
            this.moyenneGlobale = moyenneGlobale;
            this.coutTotal      = coutTotal;
            this.nbTotal        = nbTotal;
            this.parType        = parType;
            this.parMachine     = parMachine;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  1. COÛT TOTAL PAR MACHINE (ou global)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Coût total global — toutes machines confondues
     */
    public ResultatCout getCoutTotalGlobal() throws SQLException {
        String sql = """
                SELECT m.idMain, m.typePanne, m.dateMain, m.cout, m.description,
                       ma.nom AS nomMachine
                FROM maintenance m
                LEFT JOIN machine ma ON m.idM = ma.idM
                ORDER BY m.cout DESC
                """;
        return executerRequeteCout(sql, null, "Toutes machines");
    }

    /**
     * Coût total pour une machine spécifique (par nom)
     */
    public ResultatCout getCoutTotalParMachine(String nomMachine) throws SQLException {
        if (nomMachine == null || nomMachine.isBlank()) {
            return getCoutTotalGlobal();
        }
        String sql = """
                SELECT m.idMain, m.typePanne, m.dateMain, m.cout, m.description,
                       ma.nom AS nomMachine
                FROM maintenance m
                LEFT JOIN machine ma ON m.idM = ma.idM
                WHERE ma.nom LIKE ?
                ORDER BY m.cout DESC
                """;
        return executerRequeteCout(sql, "%" + nomMachine.trim() + "%", nomMachine);
    }

    /**
     * Coût total pour une machine spécifique (par idM)
     */
    public ResultatCout getCoutTotalParIdMachine(int idM) throws SQLException {
        String sql = """
                SELECT m.idMain, m.typePanne, m.dateMain, m.cout, m.description,
                       ma.nom AS nomMachine
                FROM maintenance m
                LEFT JOIN machine ma ON m.idM = ma.idM
                WHERE m.idM = ?
                ORDER BY m.cout DESC
                """;
        return executerRequeteCoutInt(sql, idM, "Machine #" + idM);
    }

    /**
     * Coûts groupés par machine (tableau de synthèse)
     */
    public List<Map<String, Object>> getCoutsParMachine() throws SQLException {
        String sql = """
                SELECT ma.idM, ma.nom,
                       COUNT(m.idMain)   AS nb,
                       SUM(m.cout)       AS total,
                       AVG(m.cout)       AS moyenne,
                       MAX(m.cout)       AS max,
                       MIN(m.cout)       AS min
                FROM maintenance m
                LEFT JOIN machine ma ON m.idM = ma.idM
                GROUP BY ma.idM, ma.nom
                ORDER BY total DESC
                """;
        List<Map<String, Object>> liste = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("idM",     rs.getInt("idM"));
                row.put("nom",     rs.getString("nom"));
                row.put("nb",      rs.getInt("nb"));
                row.put("total",   rs.getDouble("total"));
                row.put("moyenne", rs.getDouble("moyenne"));
                row.put("max",     rs.getDouble("max"));
                row.put("min",     rs.getDouble("min"));
                liste.add(row);
            }
        }
        return liste;
    }

    // ═══════════════════════════════════════════════════════════════
    //  2. ALERTES — MAINTENANCES COÛTEUSES (coût > seuil)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Retourne toutes les maintenances dont le coût dépasse le seuil.
     * Si seuil <= 0, utilise la moyenne globale comme seuil automatique.
     * Si nomMachine non null/vide, filtre sur cette machine.
     */
    public ResultatAlerte getMaintenancesCoûteuses(double seuil, String nomMachine) throws SQLException {

        // Calculer moyenne globale
        double moyenneGlobale = 0;
        double coutTotalGlobal = 0;
        int    nbTotal = 0;

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) nb, SUM(cout) total, AVG(cout) moy FROM maintenance")) {
            if (rs.next()) {
                nbTotal        = rs.getInt("nb");
                coutTotalGlobal = rs.getDouble("total");
                moyenneGlobale = rs.getDouble("moy");
            }
        }

        double seuilFinal = (seuil <= 0) ? moyenneGlobale : seuil;

        // Requête principale
        StringBuilder sql = new StringBuilder("""
                SELECT m.idMain, m.typePanne, m.dateMain, m.cout, m.description,
                       ma.nom AS nomMachine, ma.idM
                FROM maintenance m
                LEFT JOIN machine ma ON m.idM = ma.idM
                WHERE m.cout > ?
                """);

        boolean filtrerMachine = (nomMachine != null && !nomMachine.isBlank());
        if (filtrerMachine) sql.append(" AND ma.nom LIKE ?");
        sql.append(" ORDER BY m.cout DESC");

        List<Map<String, Object>> alertes = new ArrayList<>();
        Map<String, Integer> parType    = new LinkedHashMap<>();
        Map<String, Integer> parMachine = new LinkedHashMap<>();

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setDouble(1, seuilFinal);
            if (filtrerMachine) ps.setString(2, "%" + nomMachine.trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id",          rs.getInt("idMain"));
                    row.put("typePanne",   rs.getString("typePanne"));
                    row.put("dateMain",    rs.getDate("dateMain") != null
                            ? rs.getDate("dateMain").toLocalDate().toString() : "");
                    row.put("cout",        rs.getDouble("cout"));
                    row.put("description", rs.getString("description"));
                    row.put("nomMachine",  rs.getString("nomMachine"));
                    row.put("idM",         rs.getInt("idM"));

                    // Niveau d'alerte
                    double cout = rs.getDouble("cout");
                    String niveau;
                    if      (cout > seuilFinal * 3) niveau = "🔴 CRITIQUE";
                    else if (cout > seuilFinal * 2) niveau = "🟠 ÉLEVÉ";
                    else                            niveau = "🟡 MODÉRÉ";
                    row.put("niveau", niveau);

                    alertes.add(row);

                    // Comptages par type et par machine
                    String type = rs.getString("typePanne");
                    String mach = rs.getString("nomMachine");
                    parType.merge(type != null ? type : "Inconnu", 1, Integer::sum);
                    parMachine.merge(mach != null ? mach : "Inconnue", 1, Integer::sum);
                }
            }
        }

        return new ResultatAlerte(alertes, seuilFinal, moyenneGlobale,
                coutTotalGlobal, nbTotal, parType, parMachine);
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════
    private ResultatCout executerRequeteCout(String sql, String param, String nomMachine) throws SQLException {
        List<Map<String, Object>> liste = new ArrayList<>();
        double total = 0, min = Double.MAX_VALUE, max = 0;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (param != null) ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id",          rs.getInt("idMain"));
                    row.put("typePanne",   rs.getString("typePanne"));
                    row.put("dateMain",    rs.getDate("dateMain") != null
                            ? rs.getDate("dateMain").toLocalDate().toString() : "");
                    row.put("cout",        rs.getDouble("cout"));
                    row.put("description", rs.getString("description"));
                    row.put("nomMachine",  rs.getString("nomMachine"));
                    liste.add(row);
                    double c = rs.getDouble("cout");
                    total += c;
                    if (c < min) min = c;
                    if (c > max) max = c;
                }
            }
        }

        int nb = liste.size();
        double moy = nb > 0 ? total / nb : 0;
        if (nb == 0) min = 0;
        return new ResultatCout(total, moy, min, max, nb, nomMachine, liste);
    }

    private ResultatCout executerRequeteCoutInt(String sql, int param, String nomMachine) throws SQLException {
        List<Map<String, Object>> liste = new ArrayList<>();
        double total = 0, min = Double.MAX_VALUE, max = 0;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id",          rs.getInt("idMain"));
                    row.put("typePanne",   rs.getString("typePanne"));
                    row.put("dateMain",    rs.getDate("dateMain") != null
                            ? rs.getDate("dateMain").toLocalDate().toString() : "");
                    row.put("cout",        rs.getDouble("cout"));
                    row.put("description", rs.getString("description"));
                    row.put("nomMachine",  rs.getString("nomMachine"));
                    liste.add(row);
                    double c = rs.getDouble("cout");
                    total += c;
                    if (c < min) min = c;
                    if (c > max) max = c;
                }
            }
        }

        int nb = liste.size();
        double moy = nb > 0 ? total / nb : 0;
        if (nb == 0) min = 0;
        return new ResultatCout(total, moy, min, max, nb, nomMachine, liste);
    }
}