package services;

import entities.plante;
import utils.Mydatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlanteService {

    private Connection connection;

    public PlanteService() {
        connection = Mydatabase.getInstance().connection;
    }

    // --- AJOUTER ---
    public void ajouter(plante p) {
        String query = "INSERT INTO plante (nom_p, variete, besoin_eau, cycle_jours) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, p.getNom_p());
            pst.setString(2, p.getVariete());
            pst.setFloat(3, p.getBesoin_eau());
            pst.setInt(4, p.getCycle_jours());
            pst.executeUpdate();
            System.out.println("Plante '" + p.getNom_p() + "' ajoutée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur Ajout: " + e.getMessage());
        }
    }

    // --- MODIFIER ---
    public void modifier(plante p) {
        String query = "UPDATE plante SET nom_p = ?, variete = ?, besoin_eau = ?, cycle_jours = ? WHERE id_plante = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, p.getNom_p());
            pst.setString(2, p.getVariete());
            pst.setFloat(3, p.getBesoin_eau());
            pst.setInt(4, p.getCycle_jours());
            pst.setInt(5, p.getId_plante());
            pst.executeUpdate();
            System.out.println("Plante ID " + p.getId_plante() + " mise à jour !");
        } catch (SQLException e) {
            System.out.println("Erreur Modification: " + e.getMessage());
        }
    }

    // --- SUPPRIMER ---
    public void supprimer(int id) {
        String query = "DELETE FROM plante WHERE id_plante = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Plante supprimée !");
        } catch (SQLException e) {
            System.out.println("Erreur Suppression: " + e.getMessage());
        }
    }

    // --- SUPPRIMER AVEC ROTATIONS ---
    public void supprimerAvecRotations(int id) {
        try {
            String deleteRotations = "DELETE FROM rotation WHERE id_plante = ?";
            PreparedStatement pst1 = connection.prepareStatement(deleteRotations);
            pst1.setInt(1, id);
            int rotationsSupprimees = pst1.executeUpdate();
            System.out.println(rotationsSupprimees + " rotation(s) supprimée(s)");

            String deletePlante = "DELETE FROM plante WHERE id_plante = ?";
            PreparedStatement pst2 = connection.prepareStatement(deletePlante);
            pst2.setInt(1, id);
            pst2.executeUpdate();

            System.out.println("Plante et ses rotations supprimées !");
        } catch (SQLException e) {
            System.out.println("Erreur supprimerAvecRotations : " + e.getMessage());
            throw new RuntimeException("Impossible de supprimer la plante.");
        }
    }

    // --- AFFICHER TOUTES ---
    public List<plante> afficherToutes() {
        List<plante> plantes = new ArrayList<>();
        String query = "SELECT * FROM plante";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                plantes.add(new plante(
                        rs.getInt("id_plante"),
                        rs.getString("nom_p"),
                        rs.getString("variete"),
                        rs.getFloat("besoin_eau"),
                        rs.getInt("cycle_jours")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Affichage: " + e.getMessage());
        }
        return plantes;
    }

    // --- RECHERCHER PAR NOM OU VARIÉTÉ ---
    public List<plante> rechercher(String motCle) {
        List<plante> plantes = new ArrayList<>();
        String query = "SELECT * FROM plante WHERE nom_p LIKE ? OR variete LIKE ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            String pattern = "%" + motCle + "%";
            pst.setString(1, pattern);
            pst.setString(2, pattern);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                plantes.add(new plante(
                        rs.getInt("id_plante"),
                        rs.getString("nom_p"),
                        rs.getString("variete"),
                        rs.getFloat("besoin_eau"),
                        rs.getInt("cycle_jours")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Recherche: " + e.getMessage());
        }
        return plantes;
    }

    // --- TRIER ---
    public List<plante> trierPar(String critere) {
        List<plante> plantes = new ArrayList<>();
        String orderBy;
        switch (critere) {
            case "Nom (A-Z)":                  orderBy = "nom_p ASC";        break;
            case "Nom (Z-A)":                  orderBy = "nom_p DESC";       break;
            case "Besoin en eau (croissant)":  orderBy = "besoin_eau ASC";   break;
            case "Besoin en eau (décroissant)":orderBy = "besoin_eau DESC";  break;
            case "Cycle (court au long)":      orderBy = "cycle_jours ASC";  break;
            case "Cycle (long au court)":      orderBy = "cycle_jours DESC"; break;
            default:                           orderBy = "id_plante ASC";
        }
        String query = "SELECT * FROM plante ORDER BY " + orderBy;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                plantes.add(new plante(
                        rs.getInt("id_plante"),
                        rs.getString("nom_p"),
                        rs.getString("variete"),
                        rs.getFloat("besoin_eau"),
                        rs.getInt("cycle_jours")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Tri: " + e.getMessage());
        }
        return plantes;
    }

    // ============================================================
    // --- STATISTIQUES ---
    // ============================================================

    // Statistiques générales
    public Map<String, Object> getStatistiques() {
        Map<String, Object> stats = new LinkedHashMap<>();
        String query = "SELECT " +
                "COUNT(*) AS total, " +
                "AVG(besoin_eau) AS moy_eau, " +
                "MAX(besoin_eau) AS max_eau, " +
                "MIN(besoin_eau) AS min_eau, " +
                "AVG(cycle_jours) AS moy_cycle, " +
                "MAX(cycle_jours) AS max_cycle, " +
                "MIN(cycle_jours) AS min_cycle " +
                "FROM plante";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                stats.put("Total de plantes",        rs.getInt("total"));
                stats.put("Besoin eau moyen (L)",    String.format("%.2f", rs.getFloat("moy_eau")));
                stats.put("Besoin eau maximum (L)",  rs.getFloat("max_eau"));
                stats.put("Besoin eau minimum (L)",  rs.getFloat("min_eau"));
                stats.put("Cycle moyen (jours)",     String.format("%.1f", rs.getFloat("moy_cycle")));
                stats.put("Cycle maximum (jours)",   rs.getInt("max_cycle"));
                stats.put("Cycle minimum (jours)",   rs.getInt("min_cycle"));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Statistiques: " + e.getMessage());
        }
        return stats;
    }

    // Plante qui consomme le plus d'eau
    public plante getPlanteMaxEau() {
        String query = "SELECT * FROM plante ORDER BY besoin_eau DESC LIMIT 1";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return new plante(
                        rs.getInt("id_plante"),
                        rs.getString("nom_p"),
                        rs.getString("variete"),
                        rs.getFloat("besoin_eau"),
                        rs.getInt("cycle_jours")
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur getPlanteMaxEau: " + e.getMessage());
        }
        return null;
    }

    // Plante avec le cycle le plus long
    public plante getPlanteMaxCycle() {
        String query = "SELECT * FROM plante ORDER BY cycle_jours DESC LIMIT 1";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {
                return new plante(
                        rs.getInt("id_plante"),
                        rs.getString("nom_p"),
                        rs.getString("variete"),
                        rs.getFloat("besoin_eau"),
                        rs.getInt("cycle_jours")
                );
            }
        } catch (SQLException e) {
            System.out.println("Erreur getPlanteMaxCycle: " + e.getMessage());
        }
        return null;
    }
    public String getRecommandationArrosage(plante p, double temperature, double humidite, double precipitation) {
        float besoinEau = p.getBesoin_eau();
        String nom      = p.getNom_p();
        String conseil;

        // ── CAS 1 : Il pleut déjà assez ──
        if (precipitation >= besoinEau) {
            return "✅ Pas d'arrosage nécessaire pour " + nom + "\n" +
                    "🌧️ La pluie (" + precipitation + " mm) couvre le besoin (" + besoinEau + " L)";
        }

        // ── CAS 2 : Chaleur extrême ──
        if (temperature > 38) {
            return "🚨 ARROSAGE URGENT pour " + nom + " !\n" +
                    "🌡️ Température critique (" + temperature + "°C)\n" +
                    "💧 Quantité recommandée : " + (besoinEau * 2) + " L\n" +
                    "⏰ Arrosez tôt le matin (6h-8h) ou après 18h";
        }

        // ── CAS 3 : Forte chaleur + air sec ──
        if (temperature > 30 && humidite < 40) {
            return "⚠️ Arrosage important pour " + nom + "\n" +
                    "🌡️ Chaleur (" + temperature + "°C) + Air sec (" + humidite + "%)\n" +
                    "💧 Quantité recommandée : " + (besoinEau * 1.5) + " L\n" +
                    "⏰ Arrosez le matin et le soir";
        }

        // ── CAS 4 : Selon besoin en eau de la plante ──
        if (besoinEau > 3.0f) {
            // Plante gourmande en eau
            if (temperature > 25) {
                conseil = "⚠️ Arrosage important recommandé\n" +
                        "💧 " + nom + " est gourmande en eau (" + besoinEau + " L)\n" +
                        "🌡️ Température : " + temperature + "°C\n" +
                        "💧 Quantité : " + besoinEau + " L — Arrosez maintenant";
            } else {
                conseil = "💧 Arrosage modéré recommandé\n" +
                        "🌱 " + nom + " besoin : " + besoinEau + " L\n" +
                        "💧 Quantité : " + (besoinEau * 0.8) + " L aujourd'hui";
            }

        } else if (besoinEau >= 1.5f) {
            // Plante à besoin moyen
            if (humidite > 70) {
                conseil = "✅ Arrosage réduit pour " + nom + "\n" +
                        "💧 Humidité élevée (" + humidite + "%) — Réduisez les doses\n" +
                        "⚠️ Attention aux maladies fongiques\n" +
                        "💧 Quantité : " + (besoinEau * 0.5) + " L maximum";
            } else {
                conseil = "💧 Arrosage standard pour " + nom + "\n" +
                        "🌡️ Température : " + temperature + "°C  💧 Humidité : " + humidite + "%\n" +
                        "💧 Quantité recommandée : " + besoinEau + " L";
            }

        } else {
            // Plante peu gourmande en eau
            if (humidite > 60) {
                conseil = "✅ Pas d'arrosage nécessaire pour " + nom + "\n" +
                        "💧 " + nom + " a un faible besoin (" + besoinEau + " L)\n" +
                        "🌫️ Humidité suffisante (" + humidite + "%)";
            } else {
                conseil = "💧 Arrosage léger pour " + nom + "\n" +
                        "🌱 Plante économe en eau\n" +
                        "💧 Quantité : " + besoinEau + " L — 1 fois tous les 2 jours";
            }
        }

        return conseil;
    }
}