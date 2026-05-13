package services.Materiels;

import models.Materiels.Maintenance;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceService {

    private Connection connection;

    public MaintenanceService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // AJOUTER
    public void ajouter(Maintenance m) throws SQLException {
        String sql = "INSERT INTO maintenance (typePanne, cout, dateMain, description, idM, statut, recommandation, priorite, kilometrage) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, m.getTypePanne());
        pst.setDouble(2, m.getCout());
        pst.setDate(3, Date.valueOf(m.getDateMain()));
        pst.setString(4, m.getDescription());
        pst.setInt(5, m.getIdM());
        pst.setString(6, m.getStatut());
        pst.setString(7, m.getRecommandation());
        pst.setString(8, m.getPriorite());
        pst.setInt(9, m.getKilometrage());
        pst.executeUpdate();
    }

    // MODIFIER
    public void modifier(Maintenance m) throws SQLException {
        String sql = "UPDATE maintenance SET typePanne=?, cout=?, dateMain=?, description=?, idM=?, statut=?, recommandation=?, priorite=?, kilometrage=? WHERE idMain=?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setString(1, m.getTypePanne());
        pst.setDouble(2, m.getCout());
        pst.setDate(3, Date.valueOf(m.getDateMain()));
        pst.setString(4, m.getDescription());
        pst.setInt(5, m.getIdM());
        pst.setString(6, m.getStatut());
        pst.setString(7, m.getRecommandation());
        pst.setString(8, m.getPriorite());
        pst.setInt(9, m.getKilometrage());
        pst.setInt(10, m.getIdMain());
        pst.executeUpdate();
    }

    // SUPPRIMER
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM maintenance WHERE idMain=?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    // RÉCUPÉRER TOUS
    public List<Maintenance> recuperer() throws SQLException {
        List<Maintenance> list = new ArrayList<>();
        String sql = "SELECT * FROM maintenance ORDER BY dateMain DESC";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Maintenance m = new Maintenance();
            m.setIdMain(rs.getInt("idMain"));
            m.setTypePanne(rs.getString("typePanne"));
            m.setCout(rs.getDouble("cout"));
            m.setDateMain(rs.getDate("dateMain") != null ? rs.getDate("dateMain").toLocalDate() : null);
            m.setDescription(rs.getString("description"));
            m.setIdM(rs.getInt("idM"));
            m.setStatut(rs.getString("statut"));
            m.setRecommandation(rs.getString("recommandation"));
            m.setPriorite(rs.getString("priorite"));
            m.setKilometrage(rs.getInt("kilometrage"));
            list.add(m);
        }
        return list;
    }

    // RÉCUPÉRER PAR ID
    public Maintenance getById(int id) throws SQLException {
        String sql = "SELECT * FROM maintenance WHERE idMain=?";
        PreparedStatement pst = connection.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            Maintenance m = new Maintenance();
            m.setIdMain(rs.getInt("idMain"));
            m.setTypePanne(rs.getString("typePanne"));
            m.setCout(rs.getDouble("cout"));
            m.setDateMain(rs.getDate("dateMain") != null ? rs.getDate("dateMain").toLocalDate() : null);
            m.setDescription(rs.getString("description"));
            m.setIdM(rs.getInt("idM"));
            m.setStatut(rs.getString("statut"));
            m.setRecommandation(rs.getString("recommandation"));
            m.setPriorite(rs.getString("priorite"));
            m.setKilometrage(rs.getInt("kilometrage"));
            return m;
        }
        return null;
    }

    // STATISTIQUES
    public int getTotalCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM maintenance";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        return rs.next() ? rs.getInt(1) : 0;
    }

    public double getTotalCout() throws SQLException {
        String sql = "SELECT SUM(cout) FROM maintenance";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        return rs.next() ? rs.getDouble(1) : 0;
    }

    public double getMoyenneCout() throws SQLException {
        String sql = "SELECT AVG(cout) FROM maintenance";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);
        return rs.next() ? rs.getDouble(1) : 0;
    }
}