package services.Events;

import models.Events.Participation;
import services.IService;
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
        // Guard against duplicate participation
        if (isAlreadyRegistered(p.getId_user(), p.getId_evenement())) {
            throw new SQLException("User " + p.getId_user() + " is already registered for event " + p.getId_evenement());
        }

        String sql = "INSERT INTO participation (statut_participation, date_inscription, presence, id_evenement, id_user) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getStatut_participation());
            ps.setDate(2, Date.valueOf(p.getDate_inscription()));
            ps.setBoolean(3, p.isPresence());
            ps.setInt(4, p.getId_evenement());
            ps.setInt(5, p.getId_user());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(Participation p) throws SQLException {
        String sql = "UPDATE participation SET statut_participation = ?, date_inscription = ?, presence = ?, " +
                "id_evenement = ?, id_user = ? WHERE id_participation = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, p.getStatut_participation());
            ps.setDate(2, Date.valueOf(p.getDate_inscription()));
            ps.setBoolean(3, p.isPresence());
            ps.setInt(4, p.getId_evenement());
            ps.setInt(5, p.getId_user());
            ps.setInt(6, p.getId_participation());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM participation WHERE id_participation = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Participation> recuperer() throws SQLException {
        String sql = "SELECT * FROM participation";
        List<Participation> participations = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                participations.add(mapRow(rs));
            }
        }
        return participations;
    }

    @Override
    public Participation rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM participation WHERE id_participation = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Returns all participations for a given event.
     */
    public List<Participation> recupererParEvenement(int idEvenement) throws SQLException {
        String sql = "SELECT * FROM participation WHERE id_evenement = ?";
        List<Participation> participations = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idEvenement);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    participations.add(mapRow(rs));
                }
            }
        }
        return participations;
    }

    /**
     * Returns all participations for a given user.
     */
    public List<Participation> recupererParUser(int idUser) throws SQLException {
        String sql = "SELECT * FROM participation WHERE id_user = ?";
        List<Participation> participations = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    participations.add(mapRow(rs));
                }
            }
        }
        return participations;
    }

    /**
     * Checks if a user is already registered for a given event.
     */
    public boolean isAlreadyRegistered(int idUser, int idEvenement) throws SQLException {
        String sql = "SELECT COUNT(*) FROM participation WHERE id_user = ? AND id_evenement = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.setInt(2, idEvenement);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Maps a ResultSet row to a Participation object.
     * Centralises column name usage to make schema changes easier.
     */
    private Participation mapRow(ResultSet rs) throws SQLException {
        Participation p = new Participation();
        p.setId_participation(rs.getInt("id_participation"));
        p.setStatut_participation(rs.getString("statut_participation"));
        p.setDate_inscription(rs.getDate("date_inscription").toLocalDate());
        p.setPresence(rs.getBoolean("presence"));
        p.setId_evenement(rs.getInt("id_evenement"));
        p.setId_user(rs.getInt("id_user"));
        return p;
    }
}