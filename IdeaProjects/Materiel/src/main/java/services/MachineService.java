package services;

import entities.Machine;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * Classe MachineService
 * ----------------------
 * Cette classe permet de gérer les opérations CRUD
 * (Create, Read, Update, Delete)
 * sur la table "machine" dans la base de données.
 */
public class MachineService {

    // Connexion à la base de données
    private Connection connection;

    // Constructeur : récupère l'instance unique de connexion
    public MachineService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ================= AJOUTER =================
    /*
     * Méthode ajouter()
     * ------------------
     * Permet d'ajouter une nouvelle machine dans la base de données.
     * Elle insère les informations de l'objet Machine dans la table.
     * Après insertion, elle récupère l'id généré automatiquement
     * et l'affecte à l'objet Machine.
     */
    public void ajouter(Machine m) throws SQLException {

        String sql = "INSERT INTO machine(marque, modele, etatM, numeroSerie, dateAchat, nom) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        // PreparedStatement protège contre l'injection SQL
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, m.getMarque());
        ps.setString(2, m.getModele());
        ps.setString(3, m.getEtatM());
        ps.setString(4, m.getNumeroSerie());
        ps.setDate(5, Date.valueOf(m.getDateAchat()));
        ps.setString(6, m.getNom());

        ps.executeUpdate();

        // Récupérer l'id auto généré par la base de données
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            m.setIdM(rs.getInt(1));
        }
    }

    // ================= MODIFIER =================
    /*
     * Méthode modifier()
     * -------------------
     * Permet de modifier une machine existante.
     * La modification se fait selon l'idM.
     * Retourne le nombre de lignes modifiées.
     */
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

        return ps.executeUpdate(); // retourne 1 si modification réussie
    }

    // ================= SUPPRIMER =================
    /*
     * Méthode supprimer(int idM)
     * ---------------------------
     * Supprime une machine à partir de son identifiant.
     * Retourne le nombre de lignes supprimées.
     */
    public int supprimer(int idM) throws SQLException {

        String sql = "DELETE FROM machine WHERE idM = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idM);

        return ps.executeUpdate();
    }

    /*
     * Surcharge de la méthode supprimer()
     * -------------------------------------
     * Permet de supprimer directement un objet Machine
     * sans passer seulement l'id.
     */
    public int supprimer(Machine m) throws SQLException {
        return supprimer(m.getIdM());
    }

    // ================= RECUPERER =================
    /*
     * Méthode recuperer()
     * --------------------
     * Permet de récupérer toutes les machines
     * stockées dans la base de données.
     * Retourne une liste d'objets Machine.
     */
    public List<Machine> recuperer() throws SQLException {

        String sql = "SELECT * FROM machine";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        List<Machine> machines = new ArrayList<>();

        while (rs.next()) {

            // Création d'un objet Machine à partir des données SQL
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
