package services;

import entities.terrain;
import utils.Mydatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TerrainService {

    private Connection connection;

    public TerrainService() {
        connection = Mydatabase.getInstance().connection;
    }

    // --- AJOUTER ---
    public void ajouter(terrain t) {
        String query = "INSERT INTO terrain (nom_terrain, surface, type_sol, localisation, p_h) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, t.getNom_terrain());
            pst.setFloat(2, t.getSurface());
            pst.setString(3, t.getType_sol());
            pst.setString(4, t.getLocalisation());
            pst.setFloat(5, t.getP_h());
            pst.executeUpdate();
            System.out.println("Terrain '" + t.getNom_terrain() + "' ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur Ajout Terrain: " + e.getMessage());
        }
    }

    // --- MODIFIER ---
    public void modifier(terrain t) {
        String query = "UPDATE terrain SET nom_terrain = ?, surface = ?, type_sol = ?, localisation = ?, p_h = ? WHERE id_terrain = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, t.getNom_terrain());
            pst.setFloat(2, t.getSurface());
            pst.setString(3, t.getType_sol());
            pst.setString(4, t.getLocalisation());
            pst.setFloat(5, t.getP_h());
            pst.setInt(6, t.getId_terrain());
            pst.executeUpdate();
            System.out.println("Terrain ID " + t.getId_terrain() + " mis à jour !");
        } catch (SQLException e) {
            System.out.println("Erreur Modification Terrain: " + e.getMessage());
        }
    }

    // --- SUPPRIMER ---
    public void supprimer(int id) {
        String query = "DELETE FROM terrain WHERE id_terrain = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Terrain supprimé !");
        } catch (SQLException e) {
            System.out.println("Erreur Suppression Terrain: " + e.getMessage());
        }
    }

    // --- AFFICHER TOUT ---
    public List<terrain> afficherTous() {
        List<terrain> terrains = new ArrayList<>();
        String query = "SELECT * FROM terrain";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                terrains.add(new terrain(
                        rs.getInt("id_terrain"),
                        rs.getString("nom_terrain"),
                        rs.getFloat("surface"),
                        rs.getString("type_sol"),
                        rs.getString("localisation"),
                        rs.getFloat("p_h")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Affichage Terrain: " + e.getMessage());
        }
        return terrains;
    }

    // --- SUPPRIMER AVEC ROTATIONS ---
    public void supprimerAvecRotations(int id) {
        try {
            String deleteRotations = "DELETE FROM rotation WHERE id_terrain = ?";
            PreparedStatement pst1 = connection.prepareStatement(deleteRotations);
            pst1.setInt(1, id);
            pst1.executeUpdate();

            String deleteTerrain = "DELETE FROM terrain WHERE id_terrain = ?";
            PreparedStatement pst2 = connection.prepareStatement(deleteTerrain);
            pst2.setInt(1, id);
            pst2.executeUpdate();

            System.out.println("Terrain et ses rotations supprimés !");
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    // --- RECHERCHER ---
    public List<terrain> rechercher(String motCle) {
        List<terrain> terrains = new ArrayList<>();
        String query = "SELECT * FROM terrain WHERE nom_terrain LIKE ? OR type_sol LIKE ? OR localisation LIKE ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            String pattern = "%" + motCle + "%";
            pst.setString(1, pattern);
            pst.setString(2, pattern);
            pst.setString(3, pattern);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                terrains.add(new terrain(
                        rs.getInt("id_terrain"),
                        rs.getString("nom_terrain"),
                        rs.getFloat("surface"),
                        rs.getString("type_sol"),
                        rs.getString("localisation"),
                        rs.getFloat("p_h")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Recherche Terrain: " + e.getMessage());
        }
        return terrains;
    }

    // --- TRIER ---
    public List<terrain> trierPar(String critere) {
        List<terrain> terrains = new ArrayList<>();
        String orderBy = "";
        switch (critere) {
            case "Nom (A-Z)": orderBy = "nom_terrain ASC"; break;
            case "Nom (Z-A)": orderBy = "nom_terrain DESC"; break;
            case "Surface (croissante)": orderBy = "surface ASC"; break;
            case "Surface (décroissante)": orderBy = "surface DESC"; break;
            case "pH (acide au basique)": orderBy = "p_h ASC"; break;
            case "pH (basique à acide)": orderBy = "p_h DESC"; break;
            case "Type de sol (A-Z)": orderBy = "type_sol ASC"; break;
            default: orderBy = "id_terrain ASC";
        }
        String query = "SELECT * FROM terrain ORDER BY " + orderBy;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                terrains.add(new terrain(
                        rs.getInt("id_terrain"),
                        rs.getString("nom_terrain"),
                        rs.getFloat("surface"),
                        rs.getString("type_sol"),
                        rs.getString("localisation"),
                        rs.getFloat("p_h")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Tri Terrain: " + e.getMessage());
        }
        return terrains;
    }

    // ============================================================
    // --- STATISTIQUES ---
    // ============================================================
    public Map<String, Object> getStatistiques() {
        Map<String, Object> stats = new LinkedHashMap<>();
        String query = "SELECT " +
                "COUNT(*) AS total, " +
                "AVG(surface) AS moy_surface, " +
                "MAX(surface) AS max_surface, " +
                "MIN(surface) AS min_surface, " +
                "AVG(p_h) AS moy_ph, " +
                "MAX(p_h) AS max_ph, " +
                "MIN(p_h) AS min_ph " +
                "FROM terrain";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                stats.put("Total de terrains",       rs.getInt("total"));
                stats.put("Surface moyenne (m²)",    String.format("%.2f", rs.getFloat("moy_surface")));
                stats.put("Surface maximum (m²)",    rs.getFloat("max_surface"));
                stats.put("Surface minimum (m²)",    rs.getFloat("min_surface"));
                stats.put("pH moyen",                String.format("%.2f", rs.getFloat("moy_ph")));
                stats.put("pH maximum",              rs.getFloat("max_ph"));
                stats.put("pH minimum",              rs.getFloat("min_ph"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Statistiques: " + e.getMessage());
        }
        return stats;
    }

    // Répartition par type de sol (pour PieChart)
    public Map<String, Integer> getRepartitionTypeSol() {
        Map<String, Integer> repartition = new LinkedHashMap<>();
        String query = "SELECT type_sol, COUNT(*) AS nb FROM terrain GROUP BY type_sol";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                repartition.put(rs.getString("type_sol"), rs.getInt("nb"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Répartition: " + e.getMessage());
        }
        return repartition;
    }

    // Terrain avec la plus grande surface
    public terrain getTerrainMaxSurface() {
        String query = "SELECT * FROM terrain ORDER BY surface DESC LIMIT 1";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return new terrain(
                        rs.getInt("id_terrain"),
                        rs.getString("nom_terrain"),
                        rs.getFloat("surface"),
                        rs.getString("type_sol"),
                        rs.getString("localisation"),
                        rs.getFloat("p_h")
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur: " + e.getMessage());
        }
        return null;
    }
}