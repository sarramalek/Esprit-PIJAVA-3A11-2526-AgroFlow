package services.Materiels;

import models.Materiels.Machine;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MachineService {

    private static final Logger LOGGER = Logger.getLogger(MachineService.class.getName());
    private Connection connection;

    public MachineService() {
        try {
            connection = MyDatabase.getInstance().getConnection();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur de connexion à la base de données", e);
            throw new RuntimeException("Impossible d'établir la connexion à la base de données", e);
        }
    }

    // ═══════════════════════════════════════════
    //  AJOUTER
    // ═══════════════════════════════════════════
    public void ajouter(Machine m) throws SQLException {
        // Validation des données obligatoires
        if (m == null) {
            throw new IllegalArgumentException("La machine ne peut pas être null");
        }
        if (m.getNumeroSerie() == null || m.getNumeroSerie().trim().isEmpty()) {
            throw new IllegalArgumentException("Le numéro de série est obligatoire");
        }
        if (m.getNom() == null || m.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la machine est obligatoire");
        }
        if (m.getCin() <= 0) {
            throw new IllegalArgumentException("Le CIN du responsable est invalide");
        }

        String sql = """
                INSERT INTO machine
                  (nom, marque, modele, numeroSerie, etatM,
                   dateAchat, kilometrage, dateLastVisite,
                   kmLastVisite, prochaineMaintenance, cin)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getNom());
            ps.setString(2, m.getMarque());
            ps.setString(3, m.getModele());
            ps.setString(4, m.getNumeroSerie());
            ps.setString(5, m.getEtatM());

            // dateAchat — nullable
            ps.setDate(6, m.getDateAchat() != null ? Date.valueOf(m.getDateAchat()) : null);
            ps.setInt(7, m.getKilometrage());

            // dateLastVisite — nullable
            ps.setDate(8, m.getDateLastVisite() != null ? Date.valueOf(m.getDateLastVisite()) : null);
            ps.setInt(9, m.getKmLastVisite());

            // prochaineMaintenance — nullable
            ps.setDate(10, m.getProchaineMaintenance() != null ? Date.valueOf(m.getProchaineMaintenance()) : null);
            ps.setInt(11, m.getCin());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Échec de l'ajout de la machine, aucune ligne affectée");
            }

            // Récupérer l'id auto-généré
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    m.setIdM(rs.getInt(1));
                } else {
                    throw new SQLException("Échec de l'ajout, aucun ID généré");
                }
            }
        }

        LOGGER.info("Machine ajoutée avec succès - ID: " + m.getIdM() + ", Nom: " + m.getNom());
    }

    // ═══════════════════════════════════════════
    //  MODIFIER
    // ═══════════════════════════════════════════
    public int modifier(Machine m) throws SQLException {
        // Validation des données
        if (m == null) {
            throw new IllegalArgumentException("La machine ne peut pas être null");
        }
        if (m.getIdM() <= 0) {
            throw new IllegalArgumentException("ID de machine invalide");
        }
        if (m.getNumeroSerie() == null || m.getNumeroSerie().trim().isEmpty()) {
            throw new IllegalArgumentException("Le numéro de série est obligatoire");
        }

        String sql = """
                UPDATE machine SET
                  nom=?, marque=?, modele=?, numeroSerie=?, etatM=?,
                  dateAchat=?, kilometrage=?, dateLastVisite=?,
                  kmLastVisite=?, prochaineMaintenance=?, cin=?
                WHERE idM=?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, m.getNom());
            ps.setString(2, m.getMarque());
            ps.setString(3, m.getModele());
            ps.setString(4, m.getNumeroSerie());
            ps.setString(5, m.getEtatM());
            ps.setDate(6, m.getDateAchat() != null ? Date.valueOf(m.getDateAchat()) : null);
            ps.setInt(7, m.getKilometrage());
            ps.setDate(8, m.getDateLastVisite() != null ? Date.valueOf(m.getDateLastVisite()) : null);
            ps.setInt(9, m.getKmLastVisite());
            ps.setDate(10, m.getProchaineMaintenance() != null ? Date.valueOf(m.getProchaineMaintenance()) : null);
            ps.setInt(11, m.getCin());
            ps.setInt(12, m.getIdM());

            int result = ps.executeUpdate();

            if (result > 0) {
                LOGGER.info("Machine modifiée avec succès - ID: " + m.getIdM());
            } else {
                LOGGER.warning("Aucune machine trouvée avec l'ID: " + m.getIdM());
            }

            return result;
        }
    }

    // ═══════════════════════════════════════════
    //  SUPPRIMER (par ID)
    // ═══════════════════════════════════════════
    public int supprimer(int idM) throws SQLException {
        if (idM <= 0) {
            throw new IllegalArgumentException("ID de machine invalide");
        }

        String sql = "DELETE FROM machine WHERE idM = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idM);
            int result = ps.executeUpdate();

            if (result > 0) {
                LOGGER.info("Machine supprimée avec succès - ID: " + idM);
            } else {
                LOGGER.warning("Aucune machine trouvée avec l'ID: " + idM);
            }

            return result;
        }
    }

    // ═══════════════════════════════════════════
    //  SUPPRIMER (par objet)
    // ═══════════════════════════════════════════
    public int supprimer(Machine m) throws SQLException {
        if (m == null) {
            throw new IllegalArgumentException("La machine ne peut pas être null");
        }
        return supprimer(m.getIdM());
    }

    // ═══════════════════════════════════════════
    //  RECUPERER (tous)
    // ═══════════════════════════════════════════
    public List<Machine> recuperer() throws SQLException {
        String sql = "SELECT * FROM machine ORDER BY idM DESC";
        List<Machine> machines = new ArrayList<>();

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                machines.add(extractMachineFromResultSet(rs));
            }
        }

        LOGGER.info("Récupération de " + machines.size() + " machine(s)");
        return machines;
    }

    // ═══════════════════════════════════════════
    //  RECUPERER PAR ID
    // ═══════════════════════════════════════════
    public Machine recupererParId(int idM) throws SQLException {
        if (idM <= 0) {
            throw new IllegalArgumentException("ID de machine invalide");
        }

        String sql = "SELECT * FROM machine WHERE idM = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idM);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Machine machine = extractMachineFromResultSet(rs);
                    LOGGER.info("Machine trouvée - ID: " + idM);
                    return machine;
                }
            }
        }

        LOGGER.warning("Aucune machine trouvée avec l'ID: " + idM);
        return null;
    }

    // ═══════════════════════════════════════════
    //  RECUPERER PAR NUMERO DE SERIE
    // ═══════════════════════════════════════════
    public Machine recupererParNumeroSerie(String numeroSerie) throws SQLException {
        if (numeroSerie == null || numeroSerie.trim().isEmpty()) {
            throw new IllegalArgumentException("Le numéro de série est obligatoire");
        }

        String sql = "SELECT * FROM machine WHERE numeroSerie = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, numeroSerie);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractMachineFromResultSet(rs);
                }
            }
        }

        return null;
    }

    // ═══════════════════════════════════════════
    //  RECUPERER PAR CIN (responsable)
    // ═══════════════════════════════════════════
    public List<Machine> recupererParCin(int cin) throws SQLException {
        if (cin <= 0) {
            throw new IllegalArgumentException("CIN invalide");
        }

        String sql = "SELECT * FROM machine WHERE cin = ? ORDER BY dateAchat DESC";
        List<Machine> machines = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, cin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    machines.add(extractMachineFromResultSet(rs));
                }
            }
        }

        LOGGER.info("Récupération de " + machines.size() + " machine(s) pour le CIN: " + cin);
        return machines;
    }

    // ═══════════════════════════════════════════
    //  RECHERCHE AVANCÉE
    // ═══════════════════════════════════════════
    public List<Machine> rechercher(String critere) throws SQLException {
        if (critere == null || critere.trim().isEmpty()) {
            return recuperer();
        }

        String sql = """
                SELECT * FROM machine 
                WHERE LOWER(nom) LIKE ? 
                   OR LOWER(marque) LIKE ? 
                   OR LOWER(modele) LIKE ? 
                   OR LOWER(numeroSerie) LIKE ?
                ORDER BY nom
                """;

        List<Machine> machines = new ArrayList<>();
        String searchPattern = "%" + critere.toLowerCase() + "%";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 1; i <= 4; i++) {
                ps.setString(i, searchPattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    machines.add(extractMachineFromResultSet(rs));
                }
            }
        }

        LOGGER.info("Recherche de '" + critere + "' - " + machines.size() + " résultat(s)");
        return machines;
    }

    // ═══════════════════════════════════════════
    //  VÉRIFIER EXISTENCE NUMÉRO DE SÉRIE
    // ═══════════════════════════════════════════
    public boolean existeNumeroSerie(String numeroSerie) throws SQLException {
        if (numeroSerie == null || numeroSerie.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM machine WHERE numeroSerie = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, numeroSerie);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    // ═══════════════════════════════════════════
    //  COMPTER LE NOMBRE TOTAL DE MACHINES
    // ═══════════════════════════════════════════
    public int compterMachines() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM machine";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        }

        return 0;
    }

    // ═══════════════════════════════════════════
    //  MÉTHODE UTILITAIRE : Extraire une Machine du ResultSet
    // ═══════════════════════════════════════════
    private Machine extractMachineFromResultSet(ResultSet rs) throws SQLException {
        Machine m = new Machine();

        m.setIdM(rs.getInt("idM"));
        m.setNom(rs.getString("nom"));
        m.setMarque(rs.getString("marque"));
        m.setModele(rs.getString("modele"));
        m.setNumeroSerie(rs.getString("numeroSerie"));
        m.setEtatM(rs.getString("etatM"));
        m.setKilometrage(rs.getInt("kilometrage"));
        m.setKmLastVisite(rs.getInt("kmLastVisite"));
        m.setCin(rs.getInt("cin"));

        // Colonnes DATE nullable → vérifier null avant toLocalDate()
        Date dateAchat = rs.getDate("dateAchat");
        m.setDateAchat(dateAchat != null ? dateAchat.toLocalDate() : null);

        Date dateLastVisite = rs.getDate("dateLastVisite");
        m.setDateLastVisite(dateLastVisite != null ? dateLastVisite.toLocalDate() : null);

        Date prochaineMaintenance = rs.getDate("prochaineMaintenance");
        m.setProchaineMaintenance(prochaineMaintenance != null ? prochaineMaintenance.toLocalDate() : null);

        return m;
    }

    // ═══════════════════════════════════════════
    //  MÉTHODE DE TEST DE CONNEXION
    // ═══════════════════════════════════════════
    public boolean testConnexion() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur de test de connexion", e);
            return false;
        }
    }
}