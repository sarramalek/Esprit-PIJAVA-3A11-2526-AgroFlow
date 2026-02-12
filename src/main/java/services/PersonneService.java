package services;

import entities.Admin;
import entities.Employe;
import entities.Personne;
import entities.Utilisateur;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonneService implements IService<Personne> {
    private Connection connection;

    public PersonneService() {
        connection = MyDatabase.getInstance().getConnection();
        if (connection != null) {
            System.out.println("PersonneService: Connection initialized successfully!");
        } else {
            System.err.println("PersonneService: Connection is NULL!");
        }
    }

    @Override
    public void ajouter(Personne personne) throws SQLException {
        String query = "INSERT INTO users (cin, nom, prenom, tel, date_naiss, email, mdp, adresse, ville, role, date_creationcpt, date_dernierchg) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, personne.getCin());
            pst.setString(2, personne.getNom());
            pst.setString(3, personne.getPrenom());
            pst.setString(4, personne.getTel());
            pst.setString(5, personne.getDate_naiss());
            pst.setString(6, personne.getEmail());
            pst.setString(7, personne.getMdp());
            pst.setString(8, personne.getAdresse());
            pst.setString(9, personne.getVille());
            pst.setInt(10, personne.getRole()); // Le rôle est déduit automatiquement
            pst.setString(11, personne.getDate_creationcpt());
            pst.setString(12, personne.getDate_dernierchg());
            pst.executeUpdate();
        }
    }

    @Override
    public void modifier(Personne personne) throws SQLException {
        String query = "UPDATE users SET nom=?, prenom=?, tel=?, date_naiss=?, email=?, mdp=?, " +
                "adresse=?, ville=?, role=?, date_creationcpt=?, date_dernierchg=? WHERE cin=?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, personne.getNom());
            pst.setString(2, personne.getPrenom());
            pst.setString(3, personne.getTel());
            pst.setString(4, personne.getDate_naiss());
            pst.setString(5, personne.getEmail());
            pst.setString(6, personne.getMdp());
            pst.setString(7, personne.getAdresse());
            pst.setString(8, personne.getVille());
            pst.setInt(9, personne.getRole()); // Le rôle est déduit automatiquement
            pst.setString(10, personne.getDate_creationcpt());
            pst.setString(11, personne.getDate_dernierchg());
            pst.setInt(12, personne.getCin());
            pst.executeUpdate();
        }
    }

    @Override
    public void supprimer(int cin) throws SQLException {
        String query = "DELETE FROM users WHERE cin=?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, cin);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Personne> recuperer() throws SQLException {
        List<Personne> personnes = new ArrayList<>();
        String query = "SELECT * FROM users";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Personne personne = createPersonneFromResultSet(rs);
                personnes.add(personne);
            }
        }
        return personnes;
    }


    public Personne rechercherParId(int cin) throws SQLException {
        String query = "SELECT * FROM users WHERE cin=?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, cin);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return createPersonneFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Méthode helper pour créer la bonne instance selon le rôle
    private Personne createPersonneFromResultSet(ResultSet rs) throws SQLException {
        int role = rs.getInt("role");
        Personne personne;

        // Créer la bonne instance selon le rôle
        switch (role) {
            case 1:
                personne = new Utilisateur();
                break;
            case 2:
                personne = new Employe();
                break;
            case 3:
                personne = new Admin();
                break;
            default:
                personne = new Utilisateur(); // Par défaut
        }

        // Remplir les données
        personne.setCin(rs.getInt("cin"));
        personne.setNom(rs.getString("nom"));
        personne.setPrenom(rs.getString("prenom"));
        personne.setTel(rs.getString("tel"));
        personne.setDate_naiss(rs.getString("date_naiss"));
        personne.setEmail(rs.getString("email"));
        personne.setMdp(rs.getString("mdp"));
        personne.setAdresse(rs.getString("adresse"));
        personne.setVille(rs.getString("ville"));
        personne.setDate_creationcpt(rs.getString("date_creationcpt"));
        personne.setDate_dernierchg(rs.getString("date_dernierchg"));

        return personne;
    }

    // Méthodes supplémentaires utiles
    public List<Utilisateur> getUtilisateurs() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String query = "SELECT * FROM users WHERE role=1";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                utilisateurs.add((Utilisateur) createPersonneFromResultSet(rs));
            }
        }
        return utilisateurs;
    }

    public List<Employe> getEmployes() throws SQLException {
        List<Employe> employes = new ArrayList<>();
        String query = "SELECT * FROM users WHERE role=2";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                employes.add((Employe) createPersonneFromResultSet(rs));
            }
        }
        return employes;
    }

    public List<Admin> getAdmins() throws SQLException {
        List<Admin> admins = new ArrayList<>();
        String query = "SELECT * FROM users WHERE role=3";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                admins.add((Admin) createPersonneFromResultSet(rs));
            }
        }
        return admins;
    }
}