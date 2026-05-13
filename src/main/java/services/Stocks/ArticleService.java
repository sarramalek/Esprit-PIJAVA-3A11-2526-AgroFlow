package services.Stocks;

import models.Stocks.Article;
import services.IService;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleService implements IService<Article> {

    private Connection connection;

    public ArticleService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    public int ajouterWithId(Article article) throws SQLException {
        String sql = "INSERT INTO article (nom, quantite_en_stock, seuil_alerte, unite_mesure, id_categorie, prix_unitaire, devise, prix_achat_devise, id_admin, id_user) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, article.getNom());
            ps.setDouble(2, article.getQuantiteEnStock());
            ps.setDouble(3, article.getSeuilAlerte());
            ps.setString(4, article.getUniteMesure());
            ps.setInt(5, article.getIdCategorie());
            ps.setDouble(6, article.getPrixUnitaire());
            ps.setString(7, article.getDevise());
            ps.setFloat(8, article.getPrixAchatDevise());
            if (article.getIdAdmin() > 0) {
                ps.setInt(9, article.getIdAdmin());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            if (article.getIdUser() > 0) {
                ps.setInt(10, article.getIdUser());
            } else {
                ps.setNull(10, Types.INTEGER);
            }

            ps.executeUpdate();
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            System.out.println("Article ajouté avec succès !");
        }
        return -1;
    }
    
    @Override
    public void ajouter(Article article) throws SQLException {
        ajouterWithId(article);
    }
    @Override
    public void modifier(Article article) throws SQLException {
        String sql = "UPDATE article SET nom = ?, quantite_en_stock = ?, seuil_alerte = ?, unite_mesure = ?, id_categorie = ?, prix_unitaire = ?, devise = ?, prix_achat_devise = ?, id_admin = ?, id_user = ? WHERE id_article = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, article.getNom());
        ps.setDouble(2, article.getQuantiteEnStock());
        ps.setDouble(3, article.getSeuilAlerte());
        ps.setString(4, article.getUniteMesure());
        ps.setInt(5, article.getIdCategorie());
        ps.setDouble(6, article.getPrixUnitaire());
        ps.setString(7, article.getDevise());
        ps.setFloat(8, article.getPrixAchatDevise());
        if (article.getIdAdmin() > 0) {
            ps.setInt(9, article.getIdAdmin());
        } else {
            ps.setNull(9, Types.INTEGER);
        }
        if (article.getIdUser() > 0) {
            ps.setInt(10, article.getIdUser());
        } else {
            ps.setNull(10, Types.INTEGER);
        }
        ps.setInt(11, article.getId());
        ps.executeUpdate();

        // --- LOGIQUE D'EMAIL UNIQUE ---
        // On vérifie si l'article modifié est en alerte
        if (article.getQuantiteEnStock() <= article.getSeuilAlerte()) {
            new Thread(() -> {
                // L'envoi se fait ici, une seule fois pour cet article
                services.Stocks.EmailService.envoyerMailAlerte(article.getNom(), article.getQuantiteEnStock());
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
        String sql = "SELECT a.*, c.nom AS nom_cat, CONCAT(u.prenom, ' ', u.nom) AS nom_agri " +
                "FROM article a " +
                "LEFT JOIN categorie c ON a.id_categorie = c.id_categorie " +
                "LEFT JOIN users u ON a.id_user = u.cin";

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
            a.setPrixUnitaire(rs.getDouble("prix_unitaire"));
            a.setDevise(rs.getString("devise"));
            a.setPrixAchatDevise(rs.getFloat("prix_achat_devise"));
            a.setIdAdmin(rs.getInt("id_admin"));
            a.setIdUser(rs.getInt("id_user"));

            // 2. IMPORTANT : On récupère le nom de la catégorie ici
            a.setNomCategorie(rs.getString("nom_cat"));
            a.setNomAgriculteur(rs.getString("nom_agri"));

            articles.add(a);
        }
        return articles;
    }

    public List<Article> recupererParUser(int idUser) throws SQLException {
        String sql = "SELECT a.*, c.nom AS nom_cat, CONCAT(u.prenom, ' ', u.nom) AS nom_agri " +
                "FROM article a " +
                "LEFT JOIN categorie c ON a.id_categorie = c.id_categorie " +
                "LEFT JOIN users u ON a.id_user = u.cin " +
                "WHERE a.id_user = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idUser);
        ResultSet rs = ps.executeQuery();
        List<Article> articles = new ArrayList<>();

        while (rs.next()) {
            Article a = new Article();
            a.setId(rs.getInt("id_article"));
            a.setNom(rs.getString("nom"));
            a.setQuantiteEnStock(rs.getDouble("quantite_en_stock"));
            a.setSeuilAlerte(rs.getDouble("seuil_alerte"));
            a.setUniteMesure(rs.getString("unite_mesure"));
            a.setIdCategorie(rs.getInt("id_categorie"));
            a.setPrixUnitaire(rs.getDouble("prix_unitaire"));
            a.setDevise(rs.getString("devise"));
            a.setPrixAchatDevise(rs.getFloat("prix_achat_devise"));
            a.setIdAdmin(rs.getInt("id_admin"));
            a.setIdUser(rs.getInt("id_user"));
            a.setNomCategorie(rs.getString("nom_cat"));
            a.setNomAgriculteur(rs.getString("nom_agri"));
            articles.add(a);
        }
        return articles;
    }

    @Override
    public Article rechercherParId(int id) throws SQLException {
        String sql = "SELECT a.*, c.nom AS nom_cat, CONCAT(u.prenom, ' ', u.nom) AS nom_agri " +
                "FROM article a " +
                "LEFT JOIN categorie c ON a.id_categorie = c.id_categorie " +
                "LEFT JOIN users u ON a.id_user = u.cin " +
                "WHERE a.id_article = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Article a = new Article();
                    a.setId(rs.getInt("id_article"));
                    a.setNom(rs.getString("nom"));
                    a.setQuantiteEnStock(rs.getDouble("quantite_en_stock"));
                    a.setSeuilAlerte(rs.getDouble("seuil_alerte"));
                    a.setUniteMesure(rs.getString("unite_mesure"));
                    a.setIdCategorie(rs.getInt("id_categorie"));
                    a.setPrixUnitaire(rs.getDouble("prix_unitaire"));
                    a.setDevise(rs.getString("devise"));
                    a.setPrixAchatDevise(rs.getFloat("prix_achat_devise"));
                    a.setIdAdmin(rs.getInt("id_admin"));
                    a.setIdUser(rs.getInt("id_user"));
                    a.setNomCategorie(rs.getString("nom_cat"));
                    a.setNomAgriculteur(rs.getString("nom_agri"));
                    return a;
                }
            }
        }
        return null;
    }

    public String genererLienQRCode(Article a) {
        // On ne garde que le nom de l'article pour le contenu du QR Code
        String data = a.getNom();

        // Encodage pour que l'URL soit valide (ex: "Pomme de terre" -> "Pomme%20de%20terre")
        String encodedData = java.net.URLEncoder.encode(data, java.nio.charset.StandardCharsets.UTF_8);

        // Retourne l'URL de l'API (taille 200x200)
        return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=" + encodedData;
    }

    public String recupererNomParId(int id) throws SQLException {
        String sql = "SELECT nom FROM article WHERE id_article = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("nom");
            }
        }
        return "Inconnu";
    }
}