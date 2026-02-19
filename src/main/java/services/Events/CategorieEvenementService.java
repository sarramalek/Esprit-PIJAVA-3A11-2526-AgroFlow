package services.Events;

import models.Events.CategorieEvenement;
import services.IService;
import utils.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        String sql = "UPDATE categorieevenement SET nom_categorie = ?, description = ? WHERE id_categorie = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, categorie.getNom_categorie());
        ps.setString(2, categorie.getDescription_categorie());
        ps.setInt(3, categorie.getId_categorie());

        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id ) throws SQLException {
        String sql = "DELETE FROM categorieevenement WHERE id_categorie = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<CategorieEvenement> recuperer() throws SQLException {
        String sql = "SELECT * FROM categorieevenement";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<CategorieEvenement> categories = new ArrayList<>();

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
        return null;
    }

    public String getNomCategorieById(int idCategorie) throws SQLException {
        String sql = "SELECT nom_categorie FROM categorieevenement WHERE id_categorie = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idCategorie);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("nom_categorie");
            }
        }

        return "Non défini";
    }

    public CategorieEvenement getCategorieById(int idCategorie) throws SQLException {
        String sql = "SELECT * FROM categorieevenement WHERE id_categorie = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idCategorie);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                CategorieEvenement c = new CategorieEvenement();
                c.setId_categorie(rs.getInt("id_categorie"));
                c.setNom_categorie(rs.getString("nom_categorie"));
                c.setDescription_categorie(rs.getString("description_categorie"));
                return c;
            }
        }

        return null;
    }
}
