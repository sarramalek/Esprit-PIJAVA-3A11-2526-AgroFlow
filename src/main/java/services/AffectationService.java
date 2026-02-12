package services;

import entities.Affectation;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AffectationService implements IService<Affectation> {
    private Connection connection;

    public AffectationService() {
        connection = MyDatabase.getInstance().getConnection();
        if (connection != null) {
            System.out.println("AffectationService: Connection initialized successfully!");
        } else {
            System.err.println("AffectationService: Connection is NULL!");
        }
    }

    @Override
    public void ajouter(Affectation affectation) throws SQLException {
        String query = "INSERT INTO affectations (cin, id_tache) VALUES (?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, affectation.getCin());
            pst.setInt(2, affectation.getId_tache());
            pst.executeUpdate();
        }
    }

    @Override
    public void modifier(Affectation affectation) throws SQLException {
        String query = "UPDATE affectations SET cin=?, id_tache=? WHERE id_affect=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, affectation.getCin());
            pst.setInt(2, affectation.getId_tache());
            pst.setInt(3, affectation.getId_affect());
            pst.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM affectations WHERE id_affect=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Affectation> recuperer() throws SQLException {
        List<Affectation> affectations = new ArrayList<>();
        String query = "SELECT * FROM affectations";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Affectation affectation = new Affectation();
                affectation.setId_affect(rs.getInt("id_affect"));
                affectation.setCin(rs.getInt("cin"));
                affectation.setId_tache(rs.getInt("id_tache"));
                affectations.add(affectation);
            }
        }
        return affectations;
    }

    @Override
    public Affectation rechercherParId(int id) throws SQLException {
        String query = "SELECT * FROM affectations WHERE id_affect=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Affectation affectation = new Affectation();
                    affectation.setId_affect(rs.getInt("id_affect"));
                    affectation.setCin(rs.getInt("cin"));
                    affectation.setId_tache(rs.getInt("id_tache"));
                    return affectation;
                }
            }
        }
        return null;
    }

    // Méthodes supplémentaires utiles
    public List<Affectation> getAffectationsByUser(int cin) throws SQLException {
        List<Affectation> affectations = new ArrayList<>();
        String query = "SELECT * FROM affectations WHERE cin=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, cin);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Affectation affectation = new Affectation();
                    affectation.setId_affect(rs.getInt("id_affect"));
                    affectation.setCin(rs.getInt("cin"));
                    affectation.setId_tache(rs.getInt("id_tache"));
                    affectations.add(affectation);
                }
            }
        }
        return affectations;
    }

    public List<Affectation> getAffectationsByTache(int idTache) throws SQLException {
        List<Affectation> affectations = new ArrayList<>();
        String query = "SELECT * FROM affectations WHERE id_tache=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, idTache);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Affectation affectation = new Affectation();
                    affectation.setId_affect(rs.getInt("id_affect"));
                    affectation.setCin(rs.getInt("cin"));
                    affectation.setId_tache(rs.getInt("id_tache"));
                    affectations.add(affectation);
                }
            }
        }
        return affectations;
    }
}