package services;

import entities.Abonnements;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbonnementService implements IService<Abonnements> {
    private Connection connection;

    public AbonnementService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Abonnements abonnement) throws SQLException {
        String query = "INSERT INTO abonnements (cin, id_offre, date_inscription, date_expiration, situation) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, abonnement.getCin());
            pst.setInt(2, abonnement.getId_offre());
            pst.setString(3, abonnement.getDate_inscription());
            pst.setString(4, abonnement.getDate_expiration());
            pst.setString(5, abonnement.getSituation());
            pst.executeUpdate();
        }
    }

    @Override
    public void modifier(Abonnements abonnement) throws SQLException {
        String query = "UPDATE abonnements SET cin=?, id_offre=?, date_inscription=?, date_expiration=?, situation=? WHERE id_abonn=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, abonnement.getCin());
            pst.setInt(2, abonnement.getId_offre());
            pst.setString(3, abonnement.getDate_inscription());
            pst.setString(4, abonnement.getDate_expiration());
            pst.setString(5, abonnement.getSituation());
            pst.setInt(6, abonnement.getId_abonn());
            pst.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM abonnements WHERE id_abonn=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Abonnements> recuperer() throws SQLException {
        List<Abonnements> abonnements = new ArrayList<>();
        String query = "SELECT * FROM abonnements";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Abonnements abonnement = new Abonnements();
                abonnement.setId_abonn(rs.getInt("id_abonn"));
                abonnement.setCin(rs.getInt("cin"));
                abonnement.setId_offre(rs.getInt("id_offre"));
                abonnement.setDate_inscription(rs.getString("date_inscription"));
                abonnement.setDate_expiration(rs.getString("date_expiration"));
                abonnement.setSituation(rs.getString("situation"));
                abonnements.add(abonnement);
            }
        }
        return abonnements;
    }

    @Override
    public Abonnements rechercherParId(int id) throws SQLException {
        String query = "SELECT * FROM abonnements WHERE id_abonn=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Abonnements abonnement = new Abonnements();
                    abonnement.setId_abonn(rs.getInt("id_abonn"));
                    abonnement.setCin(rs.getInt("cin"));
                    abonnement.setId_offre(rs.getInt("id_offre"));
                    abonnement.setDate_inscription(rs.getString("date_inscription"));
                    abonnement.setDate_expiration(rs.getString("date_expiration"));
                    abonnement.setSituation(rs.getString("situation"));
                    return abonnement;
                }
            }
        }
        return null;
    }

    // Méthodes supplémentaires utiles
    public List<Abonnements> getAbonnementsByUser(int cin) throws SQLException {
        List<Abonnements> abonnements = new ArrayList<>();
        String query = "SELECT * FROM abonnements WHERE cin=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, cin);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Abonnements abonnement = new Abonnements();
                    abonnement.setId_abonn(rs.getInt("id_abonn"));
                    abonnement.setCin(rs.getInt("cin"));
                    abonnement.setId_offre(rs.getInt("id_offre"));
                    abonnement.setDate_inscription(rs.getString("date_inscription"));
                    abonnement.setDate_expiration(rs.getString("date_expiration"));
                    abonnement.setSituation(rs.getString("situation"));
                    abonnements.add(abonnement);
                }
            }
        }
        return abonnements;
    }
}