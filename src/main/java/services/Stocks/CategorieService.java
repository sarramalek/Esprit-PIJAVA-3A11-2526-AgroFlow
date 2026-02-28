package services.Stocks;

import models.Stocks.Categorie;
import services.IService;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements IService<Categorie> {

    private Connection connection;

    public CategorieService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Categorie categorie) throws SQLException {
        String sql = "INSERT INTO categorie (nom, description) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, categorie.getNom());
        ps.setString(2, categorie.getDescription());
        ps.executeUpdate();
        System.out.println("Catégorie ajoutée avec succès !");
    }

    @Override
    public void modifier(Categorie categorie) throws SQLException {
        String sql = "UPDATE categorie SET nom=?, description=? WHERE id_categorie=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, categorie.getNom());
        ps.setString(2, categorie.getDescription());
        ps.setInt(3, categorie.getId());
        ps.executeUpdate();
        System.out.println("Catégorie modifiée !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM categorie WHERE id_categorie=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Catégorie supprimée !");
    }

    @Override
    public List<Categorie> recuperer() throws SQLException {
        List<Categorie> categories = new ArrayList<>();
        String sql = "SELECT * FROM categorie";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Categorie c = new Categorie();
            c.setId(rs.getInt("id_categorie"));
            c.setNom(rs.getString("nom"));
            c.setDescription(rs.getString("description"));
            categories.add(c);
        }
        return categories;
    }

    @Override
    public Categorie rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM categorie WHERE id_categorie=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Categorie c = new Categorie();
            c.setId(rs.getInt("id_categorie"));
            c.setNom(rs.getString("nom"));
            c.setDescription(rs.getString("description"));
            return c;
        }
        return null;
    }
    public String getNomById(int id) throws SQLException {
        if (id <= 0) return "Non défini";

        // REMPLACEZ 'id' PAR LE NOM RÉEL DE VOTRE COLONNE (ex: id_categorie)
        String query = "SELECT nom FROM categorie WHERE id_categorie = ?";

        try (java.sql.PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nom");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL getNomById : " + e.getMessage());
            throw e;
        }
        return "Inconnue";
    }
    public boolean existeDeja(String nom) throws SQLException {
        // La requête compte combien de catégories ont déjà ce nom
        String query = "SELECT COUNT(*) FROM categorie WHERE nom = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, nom);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Si le compte est supérieur à 0, le nom existe déjà
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}