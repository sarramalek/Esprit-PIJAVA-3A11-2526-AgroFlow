package services;

import models.Evenement;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements IService<Evenement> {

    private Connection connection;

    public EvenementService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Evenement evenement) throws SQLException {
        String sql = "INSERT INTO evenement (titre, description, type_evenement, date_debut, date_fin, lieu, statut, id_categorie) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, evenement.getTitre());
        ps.setString(2, evenement.getDescription());
        ps.setString(3, evenement.getTypeEvenement());
        ps.setDate(4, Date.valueOf(evenement.getDateDebut().toLocalDate()));
        ps.setDate(5, Date.valueOf(evenement.getDateFin().toLocalDate()));
        ps.setString(6, evenement.getLieu());
        ps.setString(7, evenement.getStatut());
        ps.setInt(8, evenement.getIdCategorie());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Evenement evenement) throws SQLException {
        String sql = "UPDATE evenement SET titre=?, description=?, type_evenement=?, date_debut=?, date_fin=?, " +
                "lieu=?, statut=?, id_categorie=? WHERE id_evenement=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, evenement.getTitre());
        ps.setString(2, evenement.getDescription());
        ps.setString(3, evenement.getTypeEvenement());
        ps.setDate(4, evenement.getDateDebut());
        ps.setDate(5, evenement.getDateFin());
        ps.setString(6, evenement.getLieu());
        ps.setString(7, evenement.getStatut());
        ps.setInt(8, evenement.getIdCategorie());
        ps.setInt(9, evenement.getIdEvenement());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM evenement WHERE id_evenement=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Evenement> recuperer() throws SQLException {
        List<Evenement> evenements = new ArrayList<>();
        String sql = "SELECT * FROM evenement";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Evenement e = new Evenement();
            e.setIdEvenement(rs.getInt("id_evenement"));
            e.setTitre(rs.getString("titre"));
            e.setDescription(rs.getString("description"));
            e.setTypeEvenement(rs.getString("type_evenement"));
            e.setDateDebut(rs.getDate("date_debut"));
            e.setDateFin(rs.getDate("date_fin"));
            e.setLieu(rs.getString("lieu"));
            e.setStatut(rs.getString("statut"));
            e.setIdCategorie(rs.getInt("id_categorie"));
            evenements.add(e);
        }
        return evenements;
    }

    @Override
    public Evenement rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM evenement WHERE id_evenement=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Evenement e = new Evenement();
            e.setIdEvenement(rs.getInt("id_evenement"));
            e.setTitre(rs.getString("titre"));
            e.setDescription(rs.getString("description"));
            e.setTypeEvenement(rs.getString("type_evenement"));
            e.setDateDebut(rs.getDate("date_debut"));
            e.setDateFin(rs.getDate("date_fin"));
            e.setLieu(rs.getString("lieu"));
            e.setStatut(rs.getString("statut"));
            e.setIdCategorie(rs.getInt("id_categorie"));
            return e;
        }
        return null;
    }

    // Méthode conservée pour compatibilité avec l'ancien code
    public void supprimer(Evenement evenement) throws SQLException {
        supprimer(evenement.getIdEvenement());
    }
}