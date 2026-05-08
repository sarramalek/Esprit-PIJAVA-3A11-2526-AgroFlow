package services.User;

import models.User.Tache;
import services.IService;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TacheService implements IService<Tache> {
    private Connection connection;

    public TacheService() {
        connection = MyDatabase.getInstance().getConnection();
        if (connection != null) {
            System.out.println("TacheService: Connection initialized successfully!");
        } else {
            System.err.println("TacheService: Connection is NULL!");
        }
    }

    @Override
    public void ajouter(Tache tache) throws SQLException {
        String query = "INSERT INTO taches (nom_tache, description, assignee, etat, priorite, date_echeancee) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, tache.getNomTache());
            pst.setString(2, tache.getDescription());
            pst.setInt(3, tache.getAssignee());
            pst.setString(4, tache.getEtat());
            pst.setString(5, tache.getPriorite());
            pst.setDate(6, java.sql.Date.valueOf(tache.getDateEcheance()));
            pst.executeUpdate();
        }
    }

    @Override
    public void modifier(Tache tache) throws SQLException {
        String query = "UPDATE taches SET nom_tache=?, description=?, assignee=?, etat=?, priorite=?, date_echeancee=? " +
                "WHERE id_tache=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, tache.getNomTache());
            pst.setString(2, tache.getDescription());
            pst.setInt(3, tache.getAssignee());
            pst.setString(4, tache.getEtat());
            pst.setString(5, tache.getPriorite());
            pst.setDate(6, java.sql.Date.valueOf(tache.getDateEcheance()));
            pst.setInt(7, tache.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM taches WHERE id_tache=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Tache> recuperer() throws SQLException {
        List<Tache> taches = new ArrayList<>();
        String query = "SELECT * FROM taches";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                taches.add(mapResultSet(rs));
            }
        }
        return taches;
    }

    @Override
    public Tache rechercherParId(int id) throws SQLException {
        String query = "SELECT * FROM taches WHERE id_tache=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    // Méthode helper pour éviter la duplication de code
    private Tache mapResultSet(ResultSet rs) throws SQLException {
        Tache tache = new Tache();
        tache.setId(rs.getInt("id_tache"));
        tache.setNomTache(rs.getString("nom_tache"));
        tache.setDescription(rs.getString("description"));
        tache.setAssignee(rs.getInt("assignee"));
        tache.setEtat(rs.getString("etat"));
        tache.setPriorite(rs.getString("priorite"));
        tache.setDateEcheance(rs.getDate("date_echeancee").toLocalDate());
        return tache;
    }

    public List<Tache> recupererTachesParPersonne(int cin) throws SQLException {
        List<Tache> taches = new ArrayList<>();
        String query = "SELECT * FROM taches WHERE assignee = ? ORDER BY date_echeancee ASC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, cin);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    taches.add(mapResultSet(rs));
                }
            }
        }

        System.out.println("✓ " + taches.size() + " tâches trouvées pour CIN " + cin);
        return taches;
    }
}