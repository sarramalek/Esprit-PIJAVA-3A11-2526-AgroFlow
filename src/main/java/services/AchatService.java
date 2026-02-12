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
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setDate(1, Date.valueOf(achat.getDateAchat()));
        ps.setInt(2, achat.getIdM());
        ps.setInt(3, achat.getCin());
        ps.setInt(4, achat.getQuantite()); // ✅ ajout de la quantité
        ps.executeUpdate();
    }

    // ================= MODIFIER UN ACHAT =================
    public int modifier(Achat achat) throws SQLException {
        String sql = "UPDATE achat SET dateAchat = ?, idM = ?, cin = ?, quantite = ? WHERE idAchat = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setDate(1, Date.valueOf(achat.getDateAchat()));
        ps.setInt(2, achat.getIdM());
        ps.setInt(3, achat.getCin());
        ps.setInt(4, achat.getQuantite()); // ✅ ajout de la quantité
        ps.setInt(5, achat.getIdAchat());
        return ps.executeUpdate();
    }

    // ================= SUPPRIMER UN ACHAT =================
    public int supprimer(int idAchat) throws SQLException {
        String sql = "DELETE FROM achat WHERE idAchat = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idAchat);
        return ps.executeUpdate();
    }

    // ================= RECUPERER TOUS LES ACHATS =================
    public List<Achat> recuperer() throws SQLException {
        String sql = "SELECT * FROM achat";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        List<Achat> achats = new ArrayList<>();
        while (rs.next()) {
            Achat achat = new Achat();
            achat.setIdAchat(rs.getInt("idAchat"));
            achat.setDateAchat(rs.getDate("dateAchat").toLocalDate());
            achat.setIdM(rs.getInt("idM"));
            achat.setCin(rs.getInt("cin"));
            achat.setQuantite(rs.getInt("quantite")); // ✅ récupération de la quantité
            achats.add(achat);
        }

        return achats;
    }
}
