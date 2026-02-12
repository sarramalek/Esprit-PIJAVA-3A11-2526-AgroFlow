package services;

import entities.Maintenance;
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

    public void ajouter(Maintenance m) throws SQLException {
        String req = "INSERT INTO maintenance(typePanne, cout, dateMain, description, idM) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps =  connection.prepareStatement(req);
        ps.setString(1, m.getTypePanne());
        ps.setDouble(2, m.getCout());
        ps.setDate(3, java.sql.Date.valueOf(m.getDateMain()));
        ps.setString(4, m.getDescription());
        ps.setInt(5, m.getIdM()); // L'id de la machine
        int result = ps.executeUpdate();
        System.out.println("Nombre de lignes ajoutées : " + result);
    }

    // Modifier une maintenance
    public int modifier(Maintenance maintenance) throws SQLException {
        String sql = "UPDATE maintenance SET typePanne = ?, cout = ?, dateMain = ?, description = ?, idM = ? " +
                "WHERE idMain = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, maintenance.getTypePanne());
        ps.setDouble(2, maintenance.getCout());
        ps.setDate(3, Date.valueOf(maintenance.getDateMain()));
        ps.setString(4, maintenance.getDescription());
        ps.setInt(5, maintenance.getIdM());
        ps.setInt(6, maintenance.getIdMain());

        return ps.executeUpdate();
    }

    // Supprimer une maintenance
    public int supprimer(int idMain) throws SQLException {
        String sql = "DELETE FROM maintenance WHERE idMain = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idMain);
        return ps.executeUpdate();
    }

    // Récupérer toutes les maintenances
    public List<Maintenance> recuperer() throws SQLException {
        String sql = "SELECT * FROM maintenance";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        List<Maintenance> maintenances = new ArrayList<>();

        while (rs.next()) {
            Maintenance m = new Maintenance();
            m.setIdMain(rs.getInt("idMain"));
            m.setTypePanne(rs.getString("typePanne"));
            m.setCout(rs.getDouble("cout"));
            m.setDateMain(rs.getDate("dateMain").toLocalDate());
            m.setDescription(rs.getString("description"));
            m.setIdM(rs.getInt("idM")); // clé étrangère vers Machine

            maintenances.add(m);
        }
        return maintenances;
    }

    // Récupérer toutes les maintenances pour une machine spécifique
    public List<Maintenance> recupererParMachine(int idM) throws SQLException {
        String sql = "SELECT * FROM maintenance WHERE idM = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idM);
        ResultSet rs = ps.executeQuery();
        List<Maintenance> maintenances = new ArrayList<>();

        while (rs.next()) {
            Maintenance m = new Maintenance();
            m.setIdMain(rs.getInt("idMain"));
            m.setTypePanne(rs.getString("typePanne"));
            m.setCout(rs.getDouble("cout"));
            m.setDateMain(rs.getDate("dateMain").toLocalDate());
            m.setDescription(rs.getString("description"));
            m.setIdM(rs.getInt("idM"));

            maintenances.add(m);
        }
        return maintenances;
    }

    // Jointure Maintenance + Machine
    public void afficherMaintenanceAvecMachine() throws SQLException {

        String sql = "SELECT m.idMain, m.typePanne, m.cout, m.dateMain, m.description, " +
                "ma.idM, ma.marque, ma.modele " +
                "FROM maintenance m " +
                "INNER JOIN machine ma ON m.idM = ma.idM";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            System.out.println(
                    "ID Maintenance: " + rs.getInt("idMain") +
                            ", Type: " + rs.getString("typePanne") +
                            ", Coût: " + rs.getDouble("cout") +
                            ", Date: " + rs.getDate("dateMain") +
                            ", Machine ID: " + rs.getInt("idM") +
                            ", Marque: " + rs.getString("marque") +
                            ", Modèle: " + rs.getString("modele")
            );
        }
    }





}
