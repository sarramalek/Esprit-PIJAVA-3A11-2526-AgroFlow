package services;

import entities.examens;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceExamen implements IService<examens> {
    private Connection cnx;

    public ServiceExamen() {
        cnx = MyDatabase.getInstance().connection;
    }

    @Override
    public void ajouter(examens e) throws SQLException {
        String sql = "INSERT INTO examens_sante (date_examen, type_examen, diagnostic, traitement, id_animal) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDate(1, new java.sql.Date(e.getDate_examen().getTime()));
        ps.setString(2, e.getType_examen());
        ps.setString(3, e.getDiagnostic());
        ps.setString(4, e.getTraitement());
        ps.setInt(5, e.getId_animal());
        ps.executeUpdate();
    }

    @Override
    public void modifier(examens e) throws SQLException {
        String sql = "UPDATE examens_sante SET date_examen=?, type_examen=?, diagnostic=?, traitement=?, id_animal=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDate(1, new java.sql.Date(e.getDate_examen().getTime()));
        ps.setString(2, e.getType_examen());
        ps.setString(3, e.getDiagnostic());
        ps.setString(4, e.getTraitement());
        ps.setInt(5, e.getId_animal());
        ps.setInt(6, e.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM examens_sante WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<examens> afficher() throws SQLException {
        List<examens> liste = new ArrayList<>();
        String sql = "SELECT * FROM examens_sante";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            liste.add(new examens(rs.getInt("id"), rs.getDate("date_examen"), rs.getString("type_examen"),
                    rs.getString("diagnostic"), rs.getString("traitement"), rs.getInt("id_animal")));
        }
        return liste;
    }
}