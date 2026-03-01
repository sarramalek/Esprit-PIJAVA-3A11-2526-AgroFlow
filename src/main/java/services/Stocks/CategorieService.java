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
        // Ajout de la colonne image_url dans la requête
        String req = "INSERT INTO categorie (nom, nom_en, nom_ar, description, image_url) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getNomEn());
        ps.setString(3, c.getNomAr());
        ps.setString(4, c.getDescription());
        ps.setString(5, c.getImageUrl()); // Ajout de l'URL de l'image
        ps.executeUpdate();
    }

    @Override
    public void modifier(Categorie categorie) throws SQLException {
        // Mise à jour incluant les traductions et l'image_url
        String sql = "UPDATE categorie SET nom = ?, nom_en = ?, nom_ar = ?, description = ?, image_url = ? WHERE id_categorie = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, categorie.getNom());
        ps.setString(2, categorie.getNomEn());
        ps.setString(3, categorie.getNomAr());
        ps.setString(4, categorie.getDescription());
        ps.setString(5, categorie.getImageUrl());
        ps.setInt(6, categorie.getId());

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
        String sql = "SELECT id_categorie, nom, description, nom_en, nom_ar, image_url FROM categorie";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        List<Categorie> categories = new ArrayList<>();

        while (rs.next()) {
            Categorie c = new Categorie();
            c.setId(rs.getInt("id_categorie"));
            c.setNom(rs.getString("nom"));
            c.setDescription(rs.getString("description"));
            c.setNomEn(rs.getString("nom_en"));
            c.setNomAr(rs.getString("nom_ar"));
            c.setImageUrl(rs.getString("image_url")); // Récupération de l'image

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