package services;

import entities.Achat;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AchatService {

    private Connection connection;

    public AchatService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ================= AJOUTER UN ACHAT =================
    public void ajouter(Achat achat) throws SQLException {
        String sql = "INSERT INTO achat(dateAchat, idM, cin, quantite) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(achat.getDateAchat()));
            ps.setInt(2, achat.getIdM());
            ps.setInt(3, achat.getCin());
            ps.setInt(4, achat.getQuantite());
            ps.executeUpdate();
        }
    }

    // ================= MODIFIER UN ACHAT =================
    public int modifier(Achat achat) throws SQLException {
        String sql = "UPDATE achat SET dateAchat = ?, idM = ?, cin = ?, quantite = ? WHERE idAchat = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(achat.getDateAchat()));
            ps.setInt(2, achat.getIdM());
            ps.setInt(3, achat.getCin());
            ps.setInt(4, achat.getQuantite());
            ps.setInt(5, achat.getIdAchat());
            return ps.executeUpdate();
        }
    }

    // ================= SUPPRIMER UN ACHAT =================
    public int supprimer(int idAchat) throws SQLException {
        String sql = "DELETE FROM achat WHERE idAchat = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idAchat);
            return ps.executeUpdate();
        }
    }

    // ================= RECUPERER TOUS LES ACHATS =================
    public List<Achat> recuperer() throws SQLException {
        String sql = "SELECT * FROM achat";
        List<Achat> achats = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                Achat achat = new Achat();
                achat.setIdAchat(rs.getInt("idAchat"));
                achat.setDateAchat(rs.getDate("dateAchat").toLocalDate());
                achat.setIdM(rs.getInt("idM"));
                achat.setCin(rs.getInt("cin"));
                achat.setQuantite(rs.getInt("quantite"));
                achats.add(achat);
            }
        }
        return achats;
    }

    // ================= AFFICHER ACHATS AVEC MACHINE =================
    public void afficherAchatAvecMachine() throws SQLException {
        String sql = "SELECT a.idAchat, a.dateAchat, a.quantite, a.cin, " +
                "m.idM, m.marque, m.modele " +
                "FROM achat a " +
                "INNER JOIN machine m ON a.idM = m.idM";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(
                        "ID Achat: " + rs.getInt("idAchat") +
                                ", Date: " + rs.getDate("dateAchat") +
                                ", Quantité: " + rs.getInt("quantite") +
                                ", CIN Client: " + rs.getInt("cin") +
                                ", Machine: " + rs.getString("marque") + " " + rs.getString("modele") +
                                " (ID Machine: " + rs.getInt("idM") + ")"
                );
            }
        }
    }
}
