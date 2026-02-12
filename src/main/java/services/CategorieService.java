package services;

import entities.Categorie;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements IService<Categorie> {

    private Connection connection;

    public CategorieService() {
        connection = MyDatabase.getInstance().connection;
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
}