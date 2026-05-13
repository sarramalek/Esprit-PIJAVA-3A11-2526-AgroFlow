package services.Stocks;

import models.Stocks.Mouvement;
import utils.MyDatabase;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MouvementService {

    private Connection connection;

    public MouvementService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    public void ajouterMouvement(Mouvement m) throws SQLException {
        String sqlMvt = "INSERT INTO mouvement_stock (article_id, type, quantite, date_mouvement, motif, id_user, id_admin) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlUpdateStock = "";
        
        if ("ENTREE".equalsIgnoreCase(m.getType())) {
            sqlUpdateStock = "UPDATE article SET quantite_en_stock = quantite_en_stock + ? WHERE id_article = ?";
        } else if ("SORTIE".equalsIgnoreCase(m.getType())) {
            sqlUpdateStock = "UPDATE article SET quantite_en_stock = quantite_en_stock - ? WHERE id_article = ?";
        }

        try {
            connection.setAutoCommit(false);

            // 1. Ajouter le mouvement
            try (PreparedStatement psMvt = connection.prepareStatement(sqlMvt)) {
                psMvt.setInt(1, m.getArticleId());
                psMvt.setString(2, m.getType().toUpperCase());
                psMvt.setDouble(3, m.getQuantite());
                psMvt.setTimestamp(4, Timestamp.valueOf(m.getDateMouvement()));
                psMvt.setString(5, m.getMotif());
                psMvt.setInt(6, m.getIdUser());
                if (m.getIdAdmin() != null) psMvt.setInt(7, m.getIdAdmin());
                else psMvt.setNull(7, Types.INTEGER);
                psMvt.executeUpdate();
            }

            // 2. Mettre à jour le stock
            try (PreparedStatement psUpdate = connection.prepareStatement(sqlUpdateStock)) {
                psUpdate.setDouble(1, m.getQuantite());
                psUpdate.setInt(2, m.getArticleId());
                psUpdate.executeUpdate();
            }

            connection.commit();
            System.out.println("Mouvement enregistré et stock mis à jour !");
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public List<Mouvement> recupererParArticle(int articleId) throws SQLException {
        List<Mouvement> mouvements = new ArrayList<>();
        String sql = "SELECT * FROM mouvement_stock WHERE article_id = ? ORDER BY date_mouvement DESC";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Mouvement m = new Mouvement();
                m.setId(rs.getInt("id"));
                m.setArticleId(rs.getInt("article_id"));
                m.setType(rs.getString("type"));
                m.setQuantite(rs.getDouble("quantite"));
                m.setDateMouvement(rs.getTimestamp("date_mouvement").toLocalDateTime());
                m.setMotif(rs.getString("motif"));
                m.setIdUser(rs.getInt("id_user"));
                m.setIdAdmin(rs.getObject("id_admin") != null ? rs.getInt("id_admin") : null);
                mouvements.add(m);
            }
        }
        return mouvements;
    }

    public List<Mouvement> recupererParUser(int idUser) throws SQLException {
        List<Mouvement> mouvements = new ArrayList<>();
        String sql = "SELECT * FROM mouvement_stock WHERE id_user = ? ORDER BY date_mouvement DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Mouvement m = new Mouvement();
                m.setId(rs.getInt("id"));
                m.setArticleId(rs.getInt("article_id"));
                m.setType(rs.getString("type"));
                m.setQuantite(rs.getDouble("quantite"));
                m.setDateMouvement(rs.getTimestamp("date_mouvement").toLocalDateTime());
                m.setMotif(rs.getString("motif"));
                m.setIdUser(rs.getInt("id_user"));
                m.setIdAdmin(rs.getObject("id_admin") != null ? rs.getInt("id_admin") : null);
                mouvements.add(m);
            }
        }
        return mouvements;
    }
    public void modifierQuantiteMouvement(Mouvement m, double nouvelleQuantite) throws SQLException {
        double ancienneQuantite = m.getQuantite();
        double difference = nouvelleQuantite - ancienneQuantite;
        
        String sqlUpdateMvt = "UPDATE mouvement_stock SET quantite = ? WHERE id = ?";
        String sqlUpdateStock = "";
        
        if ("ENTREE".equalsIgnoreCase(m.getType())) {
            sqlUpdateStock = "UPDATE article SET quantite_en_stock = quantite_en_stock + ? WHERE id_article = ?";
        } else if ("SORTIE".equalsIgnoreCase(m.getType())) {
            sqlUpdateStock = "UPDATE article SET quantite_en_stock = quantite_en_stock - ? WHERE id_article = ?";
        }

        try {
            connection.setAutoCommit(false);
            
            // 1. Mettre à jour le mouvement
            try (PreparedStatement ps = connection.prepareStatement(sqlUpdateMvt)) {
                ps.setDouble(1, nouvelleQuantite);
                ps.setInt(2, m.getId());
                ps.executeUpdate();
            }
            
            // 2. Mettre à jour le stock de l'article
            try (PreparedStatement ps = connection.prepareStatement(sqlUpdateStock)) {
                ps.setDouble(1, difference);
                ps.setInt(2, m.getArticleId());
                ps.executeUpdate();
            }
            
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
