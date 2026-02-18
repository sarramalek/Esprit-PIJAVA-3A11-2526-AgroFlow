package services.Terrains;

import models.Terrains.rotation;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RotationService {

    private Connection connection;

    public RotationService() {
        connection = MyDatabase.getInstance().connection;
    }

    // --- AJOUTER (Create) ---
    public void ajouter(rotation r) {
        String query = "INSERT INTO rotation (id_terrain, id_plante, date_debut_t, date_fin_t, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, r.getId_terrain());
            pst.setInt(2, r.getId_plante());

            // Conversion java.util.Date -> java.sql.Date
            pst.setDate(3, new java.sql.Date(r.getDate_debut_t().getTime()));
            pst.setDate(4, new java.sql.Date(r.getDate_fin_t().getTime()));

            pst.setInt(5, r.getStatus());

            pst.executeUpdate();
            System.out.println("Rotation enregistrée ! (Terrain ID: " + r.getId_terrain() + ")");
        } catch (SQLException e) {
            System.out.println("Erreur Ajout Rotation: " + e.getMessage());
        }
    }

    // --- MODIFIER (Update) ---
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

    // --- SUPPRIMER (Delete) ---
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

    // --- AFFICHER TOUTES (Read avec JOIN) - VERSION COMPLETE ---
    /**
     * ✨ METHODE AMELIOREE avec JOIN pour récupérer les noms
     * Cette méthode récupère les rotations avec les noms de terrain, plante et variété
     */
    public List<rotation> afficherToutes() {
        List<rotation> rotations = new ArrayList<>();

        // ✅ Requête avec JOIN pour avoir tous les détails
        String query = "SELECT " +
                "r.id_rotation, r.id_terrain, r.id_plante, " +
                "r.date_debut_t, r.date_fin_t, r.status, " +
                "t.nom_terrain, " +
                "p.nom_p, p.variete " +
                "FROM rotation r " +
                "LEFT JOIN terrain t ON r.id_terrain = t.id_terrain " +
                "LEFT JOIN plante p ON r.id_plante = p.id_plante " +
                "ORDER BY r.date_debut_t DESC";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                // Créer l'objet rotation avec les données de base
                rotation rot = new rotation(
                        rs.getInt("id_rotation"),
                        rs.getInt("id_terrain"),
                        rs.getInt("id_plante"),
                        rs.getDate("date_debut_t"),
                        rs.getDate("date_fin_t"),
                        rs.getInt("status")
                );

                // ✨ Ajouter les informations d'affichage
                rot.setNom_terrain(rs.getString("nom_terrain"));
                rot.setNom_plante(rs.getString("nom_p"));
                rot.setVariete_plante(rs.getString("variete"));

                rotations.add(rot);
            }

            System.out.println("✅ " + rotations.size() + " rotation(s) chargée(s) avec détails");

        } catch (SQLException e) {
            System.out.println("❌ Erreur Affichage Rotation: " + e.getMessage());
            e.printStackTrace();
        }

        return rotations;
    }

    // --- RECHERCHER PAR ID ---
    public rotation rechercherParId(int id) {
        String query = "SELECT " +
                "r.id_rotation, r.id_terrain, r.id_plante, " +
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
                            rs.getInt("id_rotation"),
                            rs.getInt("id_terrain"),
                            rs.getInt("id_plante"),
                            rs.getDate("date_debut_t"),
                            rs.getDate("date_fin_t"),
                            rs.getInt("status")
                    );
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
    // --- RECHERCHER PAR TERRAIN, PLANTE OU STATUT ---
    public List<rotation> rechercher(String motCle) {
        List<rotation> rotations = new ArrayList<>();
        String query = "SELECT " +
                "r.id_rotation, r.id_terrain, r.id_plante, " +
                "r.date_debut_t, r.date_fin_t, r.status, " +
                "t.nom_terrain, p.nom_p, p.variete " +
                "FROM rotation r " +
                "LEFT JOIN terrain t ON r.id_terrain = t.id_terrain " +
                "LEFT JOIN plante p ON r.id_plante = p.id_plante " +
                "WHERE t.nom_terrain LIKE ? OR p.nom_p LIKE ? OR p.variete LIKE ? " +
                "ORDER BY r.date_debut_t DESC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            String pattern = "%" + motCle + "%";
            pst.setString(1, pattern);
            pst.setString(2, pattern);
            pst.setString(3, pattern);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                rotation rot = new rotation(
                        rs.getInt("id_rotation"),
                        rs.getInt("id_terrain"),
                        rs.getInt("id_plante"),
                        rs.getDate("date_debut_t"),
                        rs.getDate("date_fin_t"),
                        rs.getInt("status")
                );
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
        String query = "SELECT " +
                "r.id_rotation, r.id_terrain, r.id_plante, " +
                "r.date_debut_t, r.date_fin_t, r.status, " +
                "t.nom_terrain, p.nom_p, p.variete " +
                "FROM rotation r " +
                "LEFT JOIN terrain t ON r.id_terrain = t.id_terrain " +
                "LEFT JOIN plante p ON r.id_plante = p.id_plante " +
                "WHERE r.status = ? " +
                "ORDER BY r.date_debut_t DESC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, statut);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                rotation rot = new rotation(
                        rs.getInt("id_rotation"),
                        rs.getInt("id_terrain"),
                        rs.getInt("id_plante"),
                        rs.getDate("date_debut_t"),
                        rs.getDate("date_fin_t"),
                        rs.getInt("status")
                );
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
        String orderBy = "";

        switch (critere) {
            case "Date début (récente)":
                orderBy = "r.date_debut_t DESC";
                break;
            case "Date début (ancienne)":
                orderBy = "r.date_debut_t ASC";
                break;
            case "Date fin (récente)":
                orderBy = "r.date_fin_t DESC";
                break;
            case "Date fin (ancienne)":
                orderBy = "r.date_fin_t ASC";
                break;
            case "Terrain (A-Z)":
                orderBy = "t.nom_terrain ASC";
                break;
            case "Terrain (Z-A)":
                orderBy = "t.nom_terrain DESC";
                break;
            case "Plante (A-Z)":
                orderBy = "p.nom_p ASC";
                break;
            case "Statut (En cours d'abord)":
                orderBy = "r.status DESC, r.date_debut_t DESC";
                break;
            default:
                orderBy = "r.date_debut_t DESC";
        }

        String query = "SELECT " +
                "r.id_rotation, r.id_terrain, r.id_plante, " +
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
                        rs.getInt("id_rotation"),
                        rs.getInt("id_terrain"),
                        rs.getInt("id_plante"),
                        rs.getDate("date_debut_t"),
                        rs.getDate("date_fin_t"),
                        rs.getInt("status")
                );
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
}