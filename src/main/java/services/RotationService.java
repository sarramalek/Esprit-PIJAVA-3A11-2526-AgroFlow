package services;

import entities.rotation;
import utils.Mydatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RotationService {

    private Connection connection;

    public RotationService() {
        connection = Mydatabase.getInstance().connection;
    }

    // --- AJOUTER ---
    public void ajouter(rotation r) {
        String query = "INSERT INTO rotation (id_terrain, id_plante, date_debut_t, date_fin_t, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, r.getId_terrain());
            pst.setInt(2, r.getId_plante());
            pst.setDate(3, new java.sql.Date(r.getDate_debut_t().getTime()));
            pst.setDate(4, new java.sql.Date(r.getDate_fin_t().getTime()));
            pst.setInt(5, r.getStatus());
            pst.executeUpdate();
            System.out.println("Rotation enregistrée !");
        } catch (SQLException e) {
            System.out.println("Erreur Ajout Rotation: " + e.getMessage());
        }
    }

    // --- MODIFIER ---
    public void modifier(rotation r) {
        String query = "UPDATE rotation SET id_terrain = ?, id_plante = ?, date_debut_t = ?, date_fin_t = ?, status = ? WHERE id_rotation = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, r.getId_terrain());
            pst.setInt(2, r.getId_plante());
            pst.setDate(3, new java.sql.Date(r.getDate_debut_t().getTime()));
            pst.setDate(4, new java.sql.Date(r.getDate_fin_t().getTime()));
            pst.setInt(5, r.getStatus());
            pst.setInt(6, r.getId_rotation());
            pst.executeUpdate();
            System.out.println("Rotation ID " + r.getId_rotation() + " mise à jour.");
        } catch (SQLException e) {
            System.out.println("Erreur Modification Rotation: " + e.getMessage());
        }
    }

    // --- SUPPRIMER ---
    public void supprimer(int id) {
        String query = "DELETE FROM rotation WHERE id_rotation = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Rotation supprimée.");
        } catch (SQLException e) {
            System.out.println("Erreur Suppression Rotation: " + e.getMessage());
        }
    }

    // --- AFFICHER TOUTES ---
    public List<rotation> afficherToutes() {
        List<rotation> rotations = new ArrayList<>();
        String query = "SELECT r.id_rotation, r.id_terrain, r.id_plante, " +
                "r.date_debut_t, r.date_fin_t, r.status, " +
                "t.nom_terrain, p.nom_p, p.variete " +
                "FROM rotation r " +
                "LEFT JOIN terrain t ON r.id_terrain = t.id_terrain " +
                "LEFT JOIN plante p ON r.id_plante = p.id_plante " +
                "ORDER BY r.date_debut_t DESC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                rotation rot = new rotation(
                        rs.getInt("id_rotation"), rs.getInt("id_terrain"),
                        rs.getInt("id_plante"), rs.getDate("date_debut_t"),
                        rs.getDate("date_fin_t"), rs.getInt("status"));
                rot.setNom_terrain(rs.getString("nom_terrain"));
                rot.setNom_plante(rs.getString("nom_p"));
                rot.setVariete_plante(rs.getString("variete"));
                rotations.add(rot);
            }
        } catch (SQLException e) {
            System.out.println("Erreur Affichage Rotation: " + e.getMessage());
        }
        return rotations;
    }

    // --- RECHERCHER ---
    public List<rotation> rechercher(String motCle) {
        List<rotation> rotations = new ArrayList<>();
        String query = "SELECT r.id_rotation, r.id_terrain, r.id_plante, " +
                "r.date_debut_t, r.date_fin_t, r.status, " +
                "t.nom_terrain, p.nom_p, p.variete " +
                "FROM rotation r " +
                "LEFT JOIN terrain t ON r.id_terrain = t.id_terrain " +
                "LEFT JOIN plante p ON r.id_plante = p.id_plante " +
                "WHERE t.nom_terrain LIKE ? OR p.nom_p LIKE ? OR p.variete LIKE ? " +
                "ORDER BY r.date_debut_t DESC";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            String pattern = "%" + motCle + "%";
            pst.setString(1, pattern); pst.setString(2, pattern); pst.setString(3, pattern);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                rotation rot = new rotation(
                        rs.getInt("id_rotation"), rs.getInt("id_terrain"),
                        rs.getInt("id_plante"), rs.getDate("date_debut_t"),
                        rs.getDate("date_fin_t"), rs.getInt("status"));
                rot.setNom_terrain(rs.getString("nom_terrain"));
                rot.setNom_plante(rs.getString("nom_p"));
                rot.setVariete_plante(rs.getString("variete"));
                rotations.add(rot);
            }
        } catch (SQLException e) {
            System.out.println("Erreur Recherche Rotation: " + e.getMessage());
        }
        return rotations;
    }

    // --- FILTRER PAR STATUT ---
    public List<rotation> filtrerParStatut(int statut) {
        List<rotation> rotations = new ArrayList<>();
        String query = "SELECT r.id_rotation, r.id_terrain, r.id_plante, " +
                "r.date_debut_t, r.date_fin_t, r.status, " +
                "t.nom_terrain, p.nom_p, p.variete " +
                "FROM rotation r " +
                "LEFT JOIN terrain t ON r.id_terrain = t.id_terrain " +
                "LEFT JOIN plante p ON r.id_plante = p.id_plante " +
                "WHERE r.status = ? ORDER BY r.date_debut_t DESC";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, statut);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                rotation rot = new rotation(
                        rs.getInt("id_rotation"), rs.getInt("id_terrain"),
                        rs.getInt("id_plante"), rs.getDate("date_debut_t"),
                        rs.getDate("date_fin_t"), rs.getInt("status"));
                rot.setNom_terrain(rs.getString("nom_terrain"));
                rot.setNom_plante(rs.getString("nom_p"));
                rot.setVariete_plante(rs.getString("variete"));
                rotations.add(rot);
            }
        } catch (SQLException e) {
            System.out.println("Erreur Filtrage Statut: " + e.getMessage());
        }
        return rotations;
    }

    // --- TRIER ---
    public List<rotation> trierPar(String critere) {
        List<rotation> rotations = new ArrayList<>();
        String orderBy;
        switch (critere) {
            case "Date début (récente)":   orderBy = "r.date_debut_t DESC"; break;
            case "Date début (ancienne)":  orderBy = "r.date_debut_t ASC";  break;
            case "Date fin (récente)":     orderBy = "r.date_fin_t DESC";   break;
            case "Date fin (ancienne)":    orderBy = "r.date_fin_t ASC";    break;
            case "Terrain (A-Z)":          orderBy = "t.nom_terrain ASC";   break;
            case "Terrain (Z-A)":          orderBy = "t.nom_terrain DESC";  break;
            case "Plante (A-Z)":           orderBy = "p.nom_p ASC";         break;
            case "Statut (En cours d'abord)": orderBy = "r.status DESC, r.date_debut_t DESC"; break;
            default: orderBy = "r.date_debut_t DESC";
        }
        String query = "SELECT r.id_rotation, r.id_terrain, r.id_plante, " +
                "r.date_debut_t, r.date_fin_t, r.status, " +
                "t.nom_terrain, p.nom_p, p.variete " +
                "FROM rotation r " +
                "LEFT JOIN terrain t ON r.id_terrain = t.id_terrain " +
                "LEFT JOIN plante p ON r.id_plante = p.id_plante " +
                "ORDER BY " + orderBy;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                rotation rot = new rotation(
                        rs.getInt("id_rotation"), rs.getInt("id_terrain"),
                        rs.getInt("id_plante"), rs.getDate("date_debut_t"),
                        rs.getDate("date_fin_t"), rs.getInt("status"));
                rot.setNom_terrain(rs.getString("nom_terrain"));
                rot.setNom_plante(rs.getString("nom_p"));
                rot.setVariete_plante(rs.getString("variete"));
                rotations.add(rot);
            }
        } catch (SQLException e) {
            System.out.println("Erreur Tri Rotation: " + e.getMessage());
        }
        return rotations;
    }

    // ============================================================
    // --- STATISTIQUES ---
    // ============================================================

    // Statistiques générales
    public Map<String, Object> getStatistiques() {
        Map<String, Object> stats = new LinkedHashMap<>();
        String query = "SELECT " +
                "COUNT(*) AS total, " +
                "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS en_cours, " +
                "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS terminees, " +
                "AVG(DATEDIFF(date_fin_t, date_debut_t)) AS duree_moy " +
                "FROM rotation";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                stats.put("Total rotations",          rs.getInt("total"));
                stats.put("En cours",                 rs.getInt("en_cours"));
                stats.put("Terminées",                rs.getInt("terminees"));
                stats.put("Durée moyenne (jours)",    String.format("%.1f", rs.getFloat("duree_moy")));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Statistiques: " + e.getMessage());
        }
        return stats;
    }

    // Répartition En cours vs Terminées (pour PieChart)
    public Map<String, Integer> getRepartitionStatut() {
        Map<String, Integer> repartition = new LinkedHashMap<>();
        String query = "SELECT " +
                "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS en_cours, " +
                "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS terminees " +
                "FROM rotation";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                repartition.put("En cours",  rs.getInt("en_cours"));
                repartition.put("Terminées", rs.getInt("terminees"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Répartition: " + e.getMessage());
        }
        return repartition;
    }

    // Top plantes les plus cultivées (pour PieChart)
    public Map<String, Integer> getTopPlantes() {
        Map<String, Integer> topPlantes = new LinkedHashMap<>();
        String query = "SELECT p.nom_p, COUNT(*) AS nb " +
                "FROM rotation r LEFT JOIN plante p ON r.id_plante = p.id_plante " +
                "GROUP BY p.nom_p ORDER BY nb DESC LIMIT 5";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                topPlantes.put(rs.getString("nom_p"), rs.getInt("nb"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Top Plantes: " + e.getMessage());
        }
        return topPlantes;
    }

    // Rechercher par ID
    public rotation rechercherParId(int id) {
        String query = "SELECT r.id_rotation, r.id_terrain, r.id_plante, " +
                "r.date_debut_t, r.date_fin_t, r.status, " +
                "t.nom_terrain, p.nom_p, p.variete " +
                "FROM rotation r " +
                "LEFT JOIN terrain t ON r.id_terrain = t.id_terrain " +
                "LEFT JOIN plante p ON r.id_plante = p.id_plante " +
                "WHERE r.id_rotation = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    rotation rot = new rotation(
                            rs.getInt("id_rotation"), rs.getInt("id_terrain"),
                            rs.getInt("id_plante"), rs.getDate("date_debut_t"),
                            rs.getDate("date_fin_t"), rs.getInt("status"));
                    rot.setNom_terrain(rs.getString("nom_terrain"));
                    rot.setNom_plante(rs.getString("nom_p"));
                    rot.setVariete_plante(rs.getString("variete"));
                    return rot;
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur Recherche Rotation: " + e.getMessage());
        }
        return null;
    }
}