package services;

import entities.plante;
import utiles.Mydatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanteService {

    private Connection connection;

    public PlanteService() {
        // On récupère la connexion via ton Singleton
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

    // --- AFFICHER TOUT ---
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
}