package services;

import entities.animaux;
import entities.Sexe;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Implémentation de l'interface IService pour gérer les opérations sur l'entité 'animaux'
public class ServiceAnimal implements IService<animaux> {
    private Connection cnx;

    // Constructeur : récupère l'instance unique de connexion à la base de données
    public ServiceAnimal() {
        cnx = MyDatabase.getInstance().connection;
    }

    @Override
    public void ajouter(animaux a) throws SQLException {
        // Requête SQL paramétrée pour éviter les injections SQL
        String sql = "INSERT INTO animaux (nom, espece, race, date_naissance, sexe, poids) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        // Remplacement des '?' par les vraies valeurs de l'objet animal 'a'
        ps.setString(1, a.getNom());
        ps.setString(2, a.getEspece());
        ps.setString(3, a.getRace());
        ps.setDate(4, new java.sql.Date(a.getDate_naissance().getTime())); // Conversion Date Java -> Date SQL
        ps.setString(5, a.getSexe().name()); // Conversion Enum -> String
        ps.setFloat(6, a.getPoids());

        // Exécution de l'insertion dans la table
        ps.executeUpdate();
    }

    @Override
    public void modifier(animaux a) throws SQLException {
        // Met à jour les colonnes d'un animal spécifique identifié par son ID
        String sql = "UPDATE animaux SET nom=?, espece=?, race=?, date_naissance=?, sexe=?, poids=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, a.getNom());
        ps.setString(2, a.getEspece());
        ps.setString(3, a.getRace());
        ps.setDate(4, new java.sql.Date(a.getDate_naissance().getTime()));
        ps.setString(5, a.getSexe().name());
        ps.setFloat(6, a.getPoids());
        ps.setInt(7, a.getId()); // Condition WHERE pour ne modifier que cet animal

        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Supprime définitivement une ligne de la table grâce à son identifiant unique
        String sql = "DELETE FROM animaux WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<animaux> afficher() throws SQLException {
        List<animaux> liste = new ArrayList<>();
        // Sélectionne toutes les lignes de la table animaux
        String sql = "SELECT * FROM animaux";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql); // Le ResultSet contient le résultat de la requête

        // Parcours du résultat ligne par ligne
        while (rs.next()) {
            // Pour chaque ligne, on crée un nouvel objet 'animaux' et on l'ajoute à la liste
            liste.add(new animaux(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("espece"),
                    rs.getString("race"),
                    rs.getDate("date_naissance"),
                    Sexe.valueOf(rs.getString("sexe")), // Transforme le String SQL en Enum Java
                    rs.getFloat("poids")
            ));
        }
        return liste;
    }
}