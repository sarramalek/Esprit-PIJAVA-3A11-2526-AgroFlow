package services;

import entities.Article;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleService implements IService<Article> {

    private Connection connection;

    public ArticleService() {
        connection = MyDatabase.getInstance().connection;
    }

    @Override
    public void ajouter(Article article) throws SQLException {
        String sql = "INSERT INTO article (nom, quantite_en_stock, seuil_alerte, unite_mesure, id_categorie) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, article.getNom());
        ps.setDouble(2, article.getQuantiteEnStock());
        ps.setDouble(3, article.getSeuilAlerte());
        ps.setString(4, article.getUniteMesure());
        ps.setInt(5, article.getIdCategorie());
        ps.executeUpdate();
        System.out.println("Article ajouté !");
    }

    @Override
    public void modifier(Article article) throws SQLException {
        String sql = "UPDATE article SET nom=?, quantite_en_stock=?, seuil_alerte=?, unite_mesure=?, id_categorie=? " +
                "WHERE id_article=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, article.getNom());
        ps.setDouble(2, article.getQuantiteEnStock());
        ps.setDouble(3, article.getSeuilAlerte());
        ps.setString(4, article.getUniteMesure());
        ps.setInt(5, article.getIdCategorie());
        ps.setInt(6, article.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM article WHERE id_article=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Article> recuperer() throws SQLException {
        List<Article> articles = new ArrayList<>();
        String sql = "SELECT * FROM article";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Article a = new Article();
            a.setId(rs.getInt("id_article"));
            a.setNom(rs.getString("nom"));
            a.setQuantiteEnStock(rs.getDouble("quantite_en_stock"));
            a.setSeuilAlerte(rs.getDouble("seuil_alerte"));
            a.setUniteMesure(rs.getString("unite_mesure"));
            a.setIdCategorie(rs.getInt("id_categorie"));
            articles.add(a);
        }
        return articles;
    }

    @Override
    public Article rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM article WHERE id_article=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Article a = new Article();
            a.setId(rs.getInt("id_article"));
            a.setNom(rs.getString("nom"));
            a.setQuantiteEnStock(rs.getDouble("quantite_en_stock"));
            a.setSeuilAlerte(rs.getDouble("seuil_alerte"));
            a.setUniteMesure(rs.getString("unite_mesure"));
            a.setIdCategorie(rs.getInt("id_categorie"));
            return a;
        }
        return null;
    }
}