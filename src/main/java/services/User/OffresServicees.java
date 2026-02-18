package services.User;
import models.User.offres;

import services.IService;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OffresServicees implements IService<offres> {

    private Connection connection;

    // Constructor - THIS IS CRITICAL
    public OffresServicees() {
        this.connection = MyDatabase.getInstance().getConnection();

        // Add this debug line temporarily
        if (this.connection == null) {
            System.err.println("ERROR: Connection is NULL in PersonneService constructor!");
        } else {
            System.out.println("PersonneService: Connection initialized successfully!");
        }
    }

    @Override
    public void ajouter(offres Offre) throws SQLException {
        String sql = "INSERT INTO offres (nom_offre, description, prix, duree_offre) VALUES (?, ?, ?, ?)";

        // Add debug line
        System.out.println("Connection status before prepareStatement: " + (connection == null ? "NULL" : "OK"));

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, Offre.getNom_offre());
        preparedStatement.setString(2, Offre.getDescription());
        preparedStatement.setDouble(3, Offre.getPrix());
        preparedStatement.setInt(4,Offre.getDuree_offre());


        preparedStatement.executeUpdate();
        System.out.println("Personne ajoutée avec succès!");
    }

    @Override
    public void modifier(offres Offre) throws SQLException {
        String sql = "UPDATE offres SET nom_offre=?, description=?, prix=?, duree_offre=? WHERE id_offres=?";

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, Offre.getNom_offre());
        preparedStatement.setString(2, Offre.getDescription());
        preparedStatement.setDouble(3, Offre.getPrix());
        preparedStatement.setInt(4,Offre.getDuree_offre());
        preparedStatement.setInt(5, Offre.getId_offres());


        preparedStatement.executeUpdate();


    }

    @Override
    public void supprimer(int cin) throws SQLException {
        String sql = "delete from offres where id_offres = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, cin);
        preparedStatement.executeUpdate();
    }

    @Override
    public List<offres> recuperer() throws SQLException {
        String sql = "select * from offres";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        List<offres> Offre = new ArrayList<>();
        while (rs.next()) {
            offres o = new offres();
            o.setId_offres(rs.getInt("id_offres"));
            o.setNom_offre(rs.getString("nom_offre"));
            o.setDescription(rs.getString("description"));
            o.setPrix(rs.getFloat("prix"));
            o.setDuree_offre(rs.getInt("duree_offre"));
            Offre.add(o);


        }
        return Offre;
    }

    @Override
    public offres rechercherParId(int id) throws SQLException {
        String query = "SELECT * FROM offres WHERE id_offres=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    offres Offre = new offres();
                    Offre.setId_offres(rs.getInt("id_offres"));
                    Offre.setNom_offre(rs.getString("nom_offre"));
                    Offre.setDescription(rs.getString("description"));
                    Offre.setPrix(rs.getFloat("prix"));
                    Offre.setDuree_offre(rs.getInt("duree_offre"));
                    return Offre;
                }
            }
        }
        return null;
    }
}

