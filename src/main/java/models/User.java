package models;

public class User {
    private int id_user;
    private String nom;

    public User(int id_user, String nom) {
        this.id_user = id_user;
        this.nom = nom;
    }

    public User(String nom) {
        this.nom = nom;
    }

    public int getId_user() {
        return id_user;
    }
    public void setId_user(int id_user) {
        this.id_user = id_user;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
}
