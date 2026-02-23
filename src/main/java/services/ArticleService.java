package services;

import entities.Article;
import utiles.MyDatabase; // Vérifie si ton package est 'utiles' ou 'utils'
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.net.URLEncoder; // Pour transformer le texte en format URL
import java.nio.charset.StandardCharsets; // Pour définir l'encodage (UTF-8)


public class ArticleService implements IService<Article> {

    private Connection connection;

    public ArticleService() {
        connection = MyDatabase.getInstance().connection;
    }

    @Override
    public void ajouter(Article article) throws SQLException {
        // On utilise des "?" comme paramètres de remplacement
        String sql = "INSERT INTO article (nom, quantite_en_stock, seuil_alerte, unite_mesure, id_categorie) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // On remplace les "?" par les vraies valeurs de l'objet article
            ps.setString(1, article.getNom());
            ps.setDouble(2, article.getQuantiteEnStock());
            ps.setDouble(3, article.getSeuilAlerte());
            ps.setString(4, article.getUniteMesure());
            ps.setInt(5, article.getIdCategorie()); // C'est ici que l'ID de la catégorie est enregistré

            ps.executeUpdate();
            System.out.println("Article ajouté avec succès !");
        }
    }
    @Override
    public void modifier(Article article) throws SQLException {
        String sql = "UPDATE article SET nom = ?, quantite_en_stock = ?, seuil_alerte = ?, unite_mesure = ?, id_categorie = ? WHERE id_article = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, article.getNom());
        ps.setDouble(2, article.getQuantiteEnStock());
        ps.setDouble(3, article.getSeuilAlerte());
        ps.setString(4, article.getUniteMesure());
        ps.setInt(5, article.getIdCategorie());
        ps.setInt(6, article.getId());
        ps.executeUpdate();

        // --- LOGIQUE D'EMAIL UNIQUE ---
        // On vérifie si l'article modifié est en alerte
        if (article.getQuantiteEnStock() <= article.getSeuilAlerte()) {
            new Thread(() -> {
                // L'envoi se fait ici, une seule fois pour cet article
                services.EmailService.envoyerMailAlerte(article.getNom(), article.getQuantiteEnStock());
            }).start();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM article WHERE id_article = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Article> recuperer() throws SQLException {
        // 1. Modification de la requête SQL pour inclure la jointure
        String sql = "SELECT a.*, c.nom AS nom_cat " +
                "FROM article a " +
                "INNER JOIN categorie c ON a.id_categorie = c.id_categorie";

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        List<Article> articles = new ArrayList<>();

        while (rs.next()) {
            Article a = new Article();
            a.setId(rs.getInt("id_article"));
            a.setNom(rs.getString("nom"));
            a.setQuantiteEnStock(rs.getDouble("quantite_en_stock"));
            a.setSeuilAlerte(rs.getDouble("seuil_alerte"));
            a.setUniteMesure(rs.getString("unite_mesure"));
            a.setIdCategorie(rs.getInt("id_categorie"));

            // 2. IMPORTANT : On récupère le nom de la catégorie ici
            a.setNomCategorie(rs.getString("nom_cat"));

            articles.add(a);
        }
        return articles;
    }
    public String genererLienQRCode(Article a) {
        // On ne garde que le nom de l'article pour le contenu du QR Code
        String data = a.getNom();

        // Encodage pour que l'URL soit valide (ex: "Pomme de terre" -> "Pomme%20de%20terre")
        String encodedData = java.net.URLEncoder.encode(data, java.nio.charset.StandardCharsets.UTF_8);

        // Retourne l'URL de l'API (taille 200x200)
        return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encodedData;
    }
}