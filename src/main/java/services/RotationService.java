package services;

import entities.rotation;
import utiles.Mydatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RotationService {

    private Connection connection;

    public RotationService() {
        connection = Mydatabase.getInstance().connection;
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

    // --- AFFICHER TOUT (Read) ---
    public List<rotation> afficherToutes() {
        List<rotation> rotations = new ArrayList<>();
        String query = "SELECT * FROM rotation";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                rotations.add(new rotation(
                        rs.getInt("id_rotation"),
                        rs.getInt("id_terrain"),
                        rs.getInt("id_plante"),
                        rs.getDate("date_debut_t"),
                        rs.getDate("date_fin_t"),
                        rs.getInt("status")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Erreur Affichage Rotation: " + e.getMessage());
        }
        return rotations;
    }
}