package services;

import models.Participation;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationService implements IService<Participation> {
    private Connection connection;

    public ParticipationService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Participation p) throws SQLException {
        String sql = "INSERT INTO participation (statut_participation, date_inscription, presence, id_evenement) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, p.getStatut_participation());
        ps.setDate(2, Date.valueOf(p.getDate_inscription())); // LocalDate → SQL Date
        ps.setBoolean(3, p.isPresence());
        ps.setInt(4, p.getId_evenement());

        ps.executeUpdate();
    }

    @Override
    public void modifier(Participation p) throws SQLException {
        String sql = "UPDATE participation SET statut_participation = ?, date_inscription = ?, presence = ?, id_evenement = ? " +
                "WHERE id_participation = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, p.getStatut_participation());
        ps.setDate(2, Date.valueOf(p.getDate_inscription()));
        ps.setBoolean(3, p.isPresence());
        ps.setInt(4, p.getId_evenement());
        ps.setInt(5, p.getId_participation());

        ps.executeUpdate();
    }

    @Override
    public void supprimer(Participation p) throws SQLException {
        String sql = "DELETE FROM participation WHERE id_participation = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, p.getId_participation());
        ps.executeUpdate();
    }

    @Override
    public List<Participation> recuperer() throws SQLException {
        String sql = "SELECT * FROM participation";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Participation> participations = new ArrayList<>();

        while (rs.next()) {
            Participation p = new Participation();
            p.setId_participation(rs.getInt("id_participation"));
            p.setStatut_participation(rs.getString("statut_participation"));
            p.setDate_inscription(rs.getDate("date_inscription").toLocalDate());
            p.setPresence(rs.getBoolean("presence"));
            p.setId_evenement(rs.getInt("id_evenement"));

            participations.add(p);
        }
        return participations;
    }

}
