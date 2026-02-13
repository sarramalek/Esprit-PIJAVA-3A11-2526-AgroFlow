package services;

import models.Evenement;
import utilis.MyDatabase;

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
        String sql = "INSERT INTO evenement (titre,description,type_evenement,date_debut,date_fin,lieu,statut,id_categorie) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, evenement.getTitre());
        preparedStatement.setString(2, evenement.getDescription());
        preparedStatement.setString(3, evenement.getTypeEvenement());
        preparedStatement.setDate(4, Date.valueOf(evenement.getDateDebut().toLocalDate()));
        preparedStatement.setDate(5, Date.valueOf(evenement.getDateFin().toLocalDate()));
        preparedStatement.setString(6, evenement.getLieu());
        preparedStatement.setString(7, evenement.getStatut());
        preparedStatement.setInt(8, evenement.getIdCategorie());

        preparedStatement.executeUpdate();
    }

    @Override
    public void modifier(Evenement evenement) throws SQLException {
        String sql = "UPDATE evenement SET titre = ?, description = ?, type_evenement = ?, date_debut = ?, date_fin = ?, lieu = ?, statut = ?, id_categorie = ? WHERE id_evenement = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        preparedStatement.setString(1, evenement.getTitre());
        preparedStatement.setString(2, evenement.getDescription());
        preparedStatement.setString(3, evenement.getTypeEvenement());
        preparedStatement.setDate(4, evenement.getDateDebut());
        preparedStatement.setDate(5, evenement.getDateFin());
        preparedStatement.setString(6, evenement.getLieu());
        preparedStatement.setString(7, evenement.getStatut());
        preparedStatement.setInt(8, evenement.getIdCategorie());

        preparedStatement.setInt(9, evenement.getIdEvenement());

        preparedStatement.executeUpdate();

    }

    @Override
    public void supprimer(Evenement evenement) throws SQLException {
        String sql = "delete from evenement where id_evenement = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, evenement.getIdEvenement());
        preparedStatement.executeUpdate();
    }

    @Override
    public List<Evenement> recuperer() throws SQLException {
        String sql = "select * from evenement";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet rs = preparedStatement.executeQuery();
        List<Evenement> evenements = new ArrayList<>();
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
}
