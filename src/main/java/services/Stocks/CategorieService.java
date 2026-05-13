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
    public void ajouter(Categorie c) throws SQLException {
        String req = "INSERT INTO categorie (nom, description, id_user, id_admin) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getDescription());
        ps.setInt(3, c.getIdUser());
        
        if (c.getIdAdmin() == null) {
            ps.setNull(4, java.sql.Types.INTEGER);
        } else {
            ps.setInt(4, c.getIdAdmin());
        }
        
        ps.executeUpdate();
    }

    @Override
    public void modifier(Categorie categorie) throws SQLException {
        String sql = "UPDATE categorie SET nom = ?, description = ?, id_user = ?, id_admin = ? WHERE id_categorie = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, categorie.getNom());
        ps.setString(2, categorie.getDescription());
        ps.setInt(3, categorie.getIdUser());
        
        if (categorie.getIdAdmin() == null) {
            ps.setNull(4, java.sql.Types.INTEGER);
        } else {
            ps.setInt(4, categorie.getIdAdmin());
        }
        
        ps.setInt(5, categorie.getId());

        ps.executeUpdate();
        System.out.println("Catégorie modifiée !");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM categorie WHERE id_categorie = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Catégorie supprimée !");
    }

    @Override
    public List<Categorie> recuperer() throws SQLException {
        String sql = "SELECT c.*, " +
                     "CONCAT(u.prenom, ' ', u.nom) as nom_agriculteur, " +
                     "(SELECT COUNT(*) FROM article a WHERE a.id_categorie = c.id_categorie) as nb_articles " +
                     "FROM categorie c " +
                     "LEFT JOIN users u ON c.id_user = u.cin";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        List<Categorie> categories = new ArrayList<>();

        while (rs.next()) {
            Categorie c = new Categorie();
            c.setId(rs.getInt("id_categorie"));
            c.setNom(rs.getString("nom"));
            c.setDescription(rs.getString("description"));
            c.setIdUser(rs.getInt("id_user"));
            c.setIdAdmin(rs.getObject("id_admin") != null ? rs.getInt("id_admin") : null);
            c.setNomAgriculteur(rs.getString("nom_agriculteur"));
            c.setNbArticles(rs.getInt("nb_articles"));

            categories.add(c);
        }
        return categories;
    }

    public List<Categorie> recupererParUser(int idUser) throws SQLException {
        String sql = "SELECT c.*, " +
                     "(SELECT COUNT(*) FROM article a WHERE a.id_categorie = c.id_categorie) as nb_articles " +
                     "FROM categorie c WHERE c.id_user = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idUser);
        ResultSet rs = ps.executeQuery();
        List<Categorie> categories = new ArrayList<>();

        while (rs.next()) {
            Categorie c = new Categorie();
            c.setId(rs.getInt("id_categorie"));
            c.setNom(rs.getString("nom"));
            c.setDescription(rs.getString("description"));
            c.setIdUser(rs.getInt("id_user"));
            c.setIdAdmin(rs.getObject("id_admin") != null ? rs.getInt("id_admin") : null);
            c.setNbArticles(rs.getInt("nb_articles"));
            categories.add(c);
        }
        return categories;
    }

    @Override
    public Categorie rechercherParId(int id) throws SQLException {
        return null;
    }

    public boolean existeDeja(String nom) throws SQLException {
        String query = "SELECT COUNT(*) FROM categorie WHERE nom = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, nom);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public String getNomById(int id) throws SQLException {
        if (id <= 0) return "Non défini";
        String query = "SELECT nom FROM categorie WHERE id_categorie = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
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
}