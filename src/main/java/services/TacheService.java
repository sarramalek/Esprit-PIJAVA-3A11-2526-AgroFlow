package services;

import models.Tache;
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
        String query = "INSERT INTO taches (nom_tache, description) VALUES (?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, tache.getNom_tache());
            pst.setString(2, tache.getDescription());
            pst.executeUpdate();
        }
    }

    @Override
    public void modifier(Tache tache) throws SQLException {
        String query = "UPDATE taches SET nom_tache=?, description=? WHERE id_tache=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, tache.getNom_tache());
            pst.setString(2, tache.getDescription());
            pst.setInt(3, tache.getId_tache());
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
                Tache tache = new Tache();
                tache.setId_tache(rs.getInt("id_tache"));
                tache.setNom_tache(rs.getString("nom_tache"));
                tache.setDescription(rs.getString("description"));
                taches.add(tache);
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
                    Tache tache = new Tache();
                    tache.setId_tache(rs.getInt("id_tache"));
                    tache.setNom_tache(rs.getString("nom_tache"));
                    tache.setDescription(rs.getString("description"));
                    return tache;
                }
            }
        }
        return null;
    }
}