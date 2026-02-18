package services.Materiels;

import models.Materiels.Achat;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AchatService {

    private Connection connection;

    public AchatService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ================= AJOUTER UN ACHAT =================
    /**
     * Ajoute un achat dans la base de données et met à jour son ID
     * @param achat L'achat à ajouter
     * @throws SQLException Si une erreur SQL survient
     */
    public void ajouter(Achat achat) throws SQLException {
        String sql = "INSERT INTO achat(dateAchat, idM, cin, quantite) VALUES (?, ?, ?, ?)";

        // ✅ CORRECTION : Utiliser Statement.RETURN_GENERATED_KEYS
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(achat.getDateAchat()));
            ps.setInt(2, achat.getIdM());
            ps.setInt(3, achat.getCin());
            ps.setInt(4, achat.getQuantite());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                // ✅ CORRECTION : Récupérer l'ID généré
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        achat.setIdAchat(generatedKeys.getInt(1));
                        System.out.println("✅ Achat ajouté avec ID : " + achat.getIdAchat());
                    } else {
                        throw new SQLException("Échec de la création de l'achat, aucun ID généré.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout de l'achat : " + e.getMessage());
            throw e; // Relancer l'exception pour que le test puisse la capturer
        }
    }

    // ================= MODIFIER UN ACHAT =================
    /**
     * Modifie un achat existant
     * @param achat L'achat avec les nouvelles valeurs
     * @return Le nombre de lignes modifiées (devrait être 1)
     * @throws SQLException Si une erreur SQL survient
     */
    public int modifier(Achat achat) throws SQLException {
        String sql = "UPDATE achat SET dateAchat = ?, idM = ?, cin = ?, quantite = ? WHERE idAchat = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(achat.getDateAchat()));
            ps.setInt(2, achat.getIdM());
            ps.setInt(3, achat.getCin());
            ps.setInt(4, achat.getQuantite());
            ps.setInt(5, achat.getIdAchat());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                System.err.println("⚠️  Aucun achat trouvé avec l'ID : " + achat.getIdAchat());
            } else {
                System.out.println("✅ Achat modifié (ID: " + achat.getIdAchat() + ")");
            }

            return rowsAffected;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification de l'achat : " + e.getMessage());
            throw e;
        }
    }

    // ================= SUPPRIMER UN ACHAT =================
    /**
     * Supprime un achat par son ID
     * @param idAchat L'ID de l'achat à supprimer
     * @return Le nombre de lignes supprimées (devrait être 1)
     * @throws SQLException Si une erreur SQL survient
     */
    public int supprimer(int idAchat) throws SQLException {
        String sql = "DELETE FROM achat WHERE idAchat = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAchat);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                System.err.println("⚠️  Aucun achat trouvé avec l'ID : " + idAchat);
            } else {
                System.out.println("✅ Achat supprimé (ID: " + idAchat + ")");
            }

            return rowsAffected;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression de l'achat : " + e.getMessage());
            throw e;
        }
    }

    // ================= RECUPERER TOUS LES ACHATS =================
    /**
     * Récupère tous les achats de la base de données
     * @return Liste de tous les achats
     * @throws SQLException Si une erreur SQL survient
     */
    public List<Achat> recuperer() throws SQLException {
        String sql = "SELECT * FROM achat";
        List<Achat> achats = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                Achat achat = new Achat();
                achat.setIdAchat(rs.getInt("idAchat"));
                achat.setDateAchat(rs.getDate("dateAchat").toLocalDate());
                achat.setIdM(rs.getInt("idM"));
                achat.setCin(rs.getInt("cin"));
                achat.setQuantite(rs.getInt("quantite"));
                achats.add(achat);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des achats : " + e.getMessage());
            throw e;
        }

        return achats;
    }

    // ================= RECUPERER UN ACHAT PAR ID =================
    /**
     * Récupère un achat spécifique par son ID
     * @param idAchat L'ID de l'achat à récupérer
     * @return L'achat trouvé ou null si non trouvé
     * @throws SQLException Si une erreur SQL survient
     */
    public Achat recupererParId(int idAchat) throws SQLException {
        String sql = "SELECT * FROM achat WHERE idAchat = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAchat);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Achat achat = new Achat();
                    achat.setIdAchat(rs.getInt("idAchat"));
                    achat.setDateAchat(rs.getDate("dateAchat").toLocalDate());
                    achat.setIdM(rs.getInt("idM"));
                    achat.setCin(rs.getInt("cin"));
                    achat.setQuantite(rs.getInt("quantite"));
                    return achat;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'achat ID " + idAchat + " : " + e.getMessage());
            throw e;
        }

        return null; // Achat non trouvé
    }

    // ================= AFFICHER ACHATS AVEC MACHINE =================
    /**
     * Affiche tous les achats avec leurs informations de machine associée
     * @throws SQLException Si une erreur SQL survient
     */
    public void afficherAchatAvecMachine() throws SQLException {
        String sql = "SELECT a.idAchat, a.dateAchat, a.quantite, a.cin, " +
                "m.idM, m.marque, m.modele " +
                "FROM achat a " +
                "INNER JOIN machine m ON a.idM = m.idM " +
                "ORDER BY a.dateAchat DESC"; // ✅ Ajout tri par date décroissante

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n========== ACHATS AVEC MACHINES ==========");
            boolean hasResults = false;

            while (rs.next()) {
                hasResults = true;
                System.out.printf(
                        "ID Achat: %-5d | Date: %-12s | Quantité: %-3d | CIN Client: %-10d | Machine: %-20s (ID: %d)%n",
                        rs.getInt("idAchat"),
                        rs.getDate("dateAchat"),
                        rs.getInt("quantite"),
                        rs.getInt("cin"),
                        rs.getString("marque") + " " + rs.getString("modele"),
                        rs.getInt("idM")
                );
            }

            if (!hasResults) {
                System.out.println("Aucun achat trouvé.");
            }
            System.out.println("==========================================\n");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage des achats : " + e.getMessage());
            throw e;
        }
    }

    // ================= VERIFIER SI UN ACHAT EXISTE =================
    /**
     * Vérifie si un achat existe par son ID
     * @param idAchat L'ID de l'achat à vérifier
     * @return true si l'achat existe, false sinon
     * @throws SQLException Si une erreur SQL survient
     */
    public boolean existe(int idAchat) throws SQLException {
        String sql = "SELECT COUNT(*) FROM achat WHERE idAchat = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAchat);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification de l'achat : " + e.getMessage());
            throw e;
        }

        return false;
    }


}