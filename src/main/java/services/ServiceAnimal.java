package services;

import entities.animaux;
import entities.Sexe;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceAnimal implements IService<animaux> {
    private Connection cnx;

    public ServiceAnimal() {
        cnx = MyDatabase.getInstance().connection;
    }

    @Override
    public void ajouter(animaux a) throws SQLException {
        String sql = "INSERT INTO animaux (nom, espece, race, date_naissance, sexe, poids) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, a.getNom());
        ps.setString(2, a.getEspece());
        ps.setString(3, a.getRace());
        ps.setDate(4, new java.sql.Date(a.getDate_naissance().getTime()));
        ps.setString(5, a.getSexe().name());
        ps.setFloat(6, a.getPoids());
        ps.executeUpdate();
    }

    @Override
    public void modifier(animaux a) throws SQLException {
        String sql = "UPDATE animaux SET nom=?, espece=?, race=?, date_naissance=?, sexe=?, poids=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, a.getNom());
        ps.setString(2, a.getEspece());
        ps.setString(3, a.getRace());
        ps.setDate(4, new java.sql.Date(a.getDate_naissance().getTime()));
        ps.setString(5, a.getSexe().name());
        ps.setFloat(6, a.getPoids());
        ps.setInt(7, a.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM animaux WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<animaux> recuperer() throws SQLException {
        List<animaux> liste = new ArrayList<>();
        String sql = "SELECT * FROM animaux";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            liste.add(new animaux(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("espece"),
                    rs.getString("race"),
                    rs.getDate("date_naissance"),
                    Sexe.valueOf(rs.getString("sexe")),
                    rs.getFloat("poids")
            ));
        }
        return liste;
    }

    @Override
    public animaux rechercherParId(int id) throws SQLException {
        String sql = "SELECT * FROM animaux WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new animaux(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("espece"),
                    rs.getString("race"),
                    rs.getDate("date_naissance"),
                    Sexe.valueOf(rs.getString("sexe")),
                    rs.getFloat("poids")
            );
        }
        return null;
    }

    // Méthode afficher() conservée pour compatibilité
    public List<animaux> afficher() throws SQLException {
        return recuperer();
    }
}