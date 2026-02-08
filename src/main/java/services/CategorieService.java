package services;

import entities.Categorie;
import utiles.MyDatabase;

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
        // Utilisation de Statement avec concaténation (comme ton exemple PersonneService)
        String sql = "insert into categorie (nom, description) " +
                "values('" + categorie.getNom() + "','" + categorie.getDescription() + "')";

        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
        System.out.println("Catégorie ajoutée avec succès !");
    }

    @Override
    public void modifier(Categorie categorie) throws SQLException {
        // Utilisation de PreparedStatement pour la mise à jour (plus sécurisé)
        String sql = "update categorie set nom = ?, description = ? where id_categorie = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, categorie.getNom());
        ps.setString(2, categorie.getDescription());
        ps.setInt(3, categorie.getId());

        ps.executeUpdate();
        System.out.println("Catégorie modifiée !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "delete from categorie where id_categorie = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Catégorie supprimée !");
    }

    @Override
    public List<Categorie> recuperer() throws SQLException {
        String sql = "select * from categorie";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        List<Categorie> categories = new ArrayList<>();

        while (rs.next()) {
            Categorie c = new Categorie();
            c.setId(rs.getInt("id_categorie"));
            c.setNom(rs.getString("nom"));
            c.setDescription(rs.getString("description"));

            categories.add(c);
        }
        return categories;
    }
}