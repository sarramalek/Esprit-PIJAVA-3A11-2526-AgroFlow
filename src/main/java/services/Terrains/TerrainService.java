package services.Terrains;

import models.Terrains.terrain;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TerrainService {

    private Connection connection;

    public TerrainService() {
        connection = MyDatabase.getInstance().connection;
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
    // ── RECOMMANDATION DE PLANTE SELON pH ──
    public String getRecommandationPlante(float ph) {
        if (ph < 4.5f) {
            return "⚠️ Sol très acide - Peu de plantes adaptées.\n" +
                    "✅ Recommandées : Myrtille, Rhododendron, Azalée\n" +
                    "❌ Éviter : Blé, Maïs, Luzerne";

        } else if (ph < 5.5f) {
            return "🟡 Sol acide\n" +
                    "✅ Recommandées : Pomme de terre, Fraise, Patate douce, Pastèque\n" +
                    "❌ Éviter : Chou, Asperge, Betterave";

        } else if (ph < 6.0f) {
            return "🟡 Sol légèrement acide\n" +
                    "✅ Recommandées : Tomate, Maïs, Concombre, Poivron, Courge\n" +
                    "❌ Éviter : Asperge, Epinard, Céleri";

        } else if (ph <= 7.0f) {
            return "🟢 Sol neutre - Idéal pour la majorité des cultures !\n" +
                    "✅ Recommandées : Blé, Laitue, Haricot, Carotte, Oignon, Persil\n" +
                    "✅ Toutes les grandes cultures sont adaptées";

        } else if (ph <= 7.5f) {
            return "🟡 Sol légèrement basique\n" +
                    "✅ Recommandées : Asperge, Chou, Betterave, Epinard, Céleri\n" +
                    "❌ Éviter : Pomme de terre, Tomate, Fraise";

        } else if (ph <= 8.0f) {
            return "🟠 Sol basique\n" +
                    "✅ Recommandées : Asperge, Choux de Bruxelles, Artichaut\n" +
                    "❌ Éviter : La plupart des fruits et légumes courants";

        } else {
            return "🔴 Sol très basique - Sol difficile à cultiver.\n" +
                    "✅ Recommandées : Peu de plantes résistent\n" +
                    "💡 Conseil : Amender le sol avec du soufre pour réduire le pH";
        }
    }

    // ── SCORE DE SANTÉ DU TERRAIN /100 ──
    public int calculerScoreSante(terrain t) {
        int score = 0;

        // ── Critère 1 : pH (40 points) ──
        float ph = t.getP_h();
        if (ph >= 6.0f && ph <= 7.0f)        score += 40; // Parfait
        else if (ph >= 5.5f && ph < 6.0f)    score += 30; // Bon
        else if (ph > 7.0f && ph <= 7.5f)    score += 30; // Bon
        else if (ph >= 5.0f && ph < 5.5f)    score += 18; // Moyen
        else if (ph > 7.5f && ph <= 8.0f)    score += 18; // Moyen
        else                                  score += 5;  // Mauvais

        // ── Critère 2 : Surface (30 points) ──
        float surface = t.getSurface();
        if (surface >= 5000)                  score += 30; // Grande exploitation
        else if (surface >= 2000)             score += 24; // Moyenne
        else if (surface >= 500)              score += 16; // Petite
        else if (surface >= 100)              score += 10; // Très petite
        else                                  score += 4;  // Micro parcelle

        // ── Critère 3 : Type de sol (30 points) ──
        String typeSol = t.getType_sol() != null ? t.getType_sol().toLowerCase() : "";
        if (typeSol.contains("limon") || typeSol.contains("argilo-limoneux"))
            score += 30; // Meilleur sol agricole
        else if (typeSol.contains("argile") || typeSol.contains("argileux"))
            score += 22; // Bon mais dense
        else if (typeSol.contains("sable") || typeSol.contains("sableux"))
            score += 16; // Drainant mais peu fertile
        else if (typeSol.contains("calcaire"))
            score += 12; // Basique, difficile
        else if (typeSol.contains("pierreux") || typeSol.contains("rocheux"))
            score += 6;  // Mauvais
        else
            score += 15; // Sol inconnu = score moyen

        return Math.min(score, 100); // Maximum 100
    }

    // ── DESCRIPTION DU SCORE ──
    public String getDescriptionScore(int score) {
        if (score >= 85)
            return "🏆 Excellent - Terrain de haute qualité agricole";
        else if (score >= 70)
            return "✅ Bon - Terrain bien adapté à la culture";
        else if (score >= 50)
            return "🟡 Moyen - Terrain cultivable avec améliorations";
        else if (score >= 30)
            return "🟠 Faible - Terrain nécessite des travaux";
        else
            return "🔴 Mauvais - Terrain difficile à exploiter";
    }

}