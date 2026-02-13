package services;

import entities.Machine;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MachineService {

    private Connection connection;

    public MachineService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ================= AJOUTER =================
    public void ajouter(Machine m) throws SQLException {

        String sql = "INSERT INTO machine(marque, modele, etatM, numeroSerie, dateAchat, nom) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, m.getMarque());
        ps.setString(2, m.getModele());
        ps.setString(3, m.getEtatM());
        ps.setString(4, m.getNumeroSerie());
        ps.setDate(5, Date.valueOf(m.getDateAchat()));
        ps.setString(6, m.getNom());

        ps.executeUpdate();

        // ✅ Récupérer l'id auto généré
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            m.setIdM(rs.getInt(1));
        }
    }

    // ================= MODIFIER =================
    public int modifier(Machine machine) throws SQLException {

        String sql = "UPDATE machine SET marque=?, modele=?, etatM=?, numeroSerie=?, dateAchat=?, nom=? WHERE idM=?";

        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, machine.getMarque());
        ps.setString(2, machine.getModele());
        ps.setString(3, machine.getEtatM());
        ps.setString(4, machine.getNumeroSerie());
        ps.setDate(5, Date.valueOf(machine.getDateAchat()));
        ps.setString(6, machine.getNom());
        ps.setInt(7, machine.getIdM());

        return ps.executeUpdate();
    }

    // ================= SUPPRIMER =================
    public int supprimer(int idM) throws SQLException {

        String sql = "DELETE FROM machine WHERE idM = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idM);

        return ps.executeUpdate();
    }

    // ✅ Surcharge professionnelle
    public int supprimer(Machine m) throws SQLException {
        return supprimer(m.getIdM());
    }

    // ================= RECUPERER =================
    public List<Machine> recuperer() throws SQLException {

        String sql = "SELECT * FROM machine";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        List<Machine> machines = new ArrayList<>();

        while (rs.next()) {

            Machine m = new Machine();
            m.setIdM(rs.getInt("idM"));
            m.setMarque(rs.getString("marque"));
            m.setModele(rs.getString("modele"));
            m.setEtatM(rs.getString("etatM"));
            m.setNumeroSerie(rs.getString("numeroSerie"));
            m.setDateAchat(rs.getDate("dateAchat").toLocalDate());
            m.setNom(rs.getString("nom"));

            machines.add(m);
        }

        return machines;
    }
}
