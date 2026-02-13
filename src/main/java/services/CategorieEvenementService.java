package services;

import models.CategorieEvenement;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieEvenementService implements IService<CategorieEvenement> {
    private Connection connection;

    public CategorieEvenementService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(CategorieEvenement categorie) throws SQLException {
        String sql = "INSERT INTO categorieevenement (nom_categorie, description) VALUES (?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, categorie.getNom_categorie());
        ps.setString(2, categorie.getDescription_categorie());
        ps.executeUpdate();
    }

    @Override
    public void modifier(CategorieEvenement categorie) throws SQLException {
        String sql = "UPDATE categorieevenement SET nom_categorie=?, description=? WHERE id_categorie=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, categorie.getNom_categorie());
        ps.setString(2, categorie.getDescription_categorie());
        ps.setInt(3, categorie.getId_categorie());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM categorieevenement WHERE id_categorie=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<CategorieEvenement> recuperer() throws SQLException {
        List<CategorieEvenement> categories = new ArrayList<>();
        String sql = "SELECT * FROM categorieevenement";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            CategorieEvenement c = new CategorieEvenement();
            c.setId_categorie(rs.getInt("id_categorie"));
            c.setNom_categorie(rs.getString("nom_categorie"));
            c.setDescription_categorie(rs.getString("description"));
            categories.add(c);
        }
        return categories;
    }

    @Override
    public CategorieEvenement rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM categorieevenement WHERE id_categorie=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            CategorieEvenement c = new CategorieEvenement();
            c.setId_categorie(rs.getInt("id_categorie"));
            c.setNom_categorie(rs.getString("nom_categorie"));
            c.setDescription_categorie(rs.getString("description"));
            return c;
        }
        return null;
    }

    // Méthode conservée pour compatibilité avec l'ancien code
    public void supprimer(CategorieEvenement categorie) throws SQLException {
        supprimer(categorie.getId_categorie());
    }
}