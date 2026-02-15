package services;

import entities.plante;
import utils.Mydatabase;
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
        String orderBy = "";

        switch (critere) {
            case "Nom (A-Z)":
                orderBy = "nom_p ASC";
                break;
            case "Nom (Z-A)":
                orderBy = "nom_p DESC";
                break;
            case "Besoin en eau (croissant)":
                orderBy = "besoin_eau ASC";
                break;
            case "Besoin en eau (décroissant)":
                orderBy = "besoin_eau DESC";
                break;
            case "Cycle (court au long)":
                orderBy = "cycle_jours ASC";
                break;
            case "Cycle (long au court)":
                orderBy = "cycle_jours DESC";
                break;
            default:
                orderBy = "id_plante ASC";
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
    // --- SUPPRIMER AVEC ROTATIONS ---
    public void supprimerAvecRotations(int id) {
        try {
            // 1. Supprimer d'abord toutes les rotations liées
            String deleteRotations = "DELETE FROM rotation WHERE id_plante = ?";
            PreparedStatement pst1 = connection.prepareStatement(deleteRotations);
            pst1.setInt(1, id);
            int rotationsSupprimees = pst1.executeUpdate();
            System.out.println(rotationsSupprimees + " rotation(s) supprimée(s)");

            // 2. Ensuite supprimer la plante
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

}