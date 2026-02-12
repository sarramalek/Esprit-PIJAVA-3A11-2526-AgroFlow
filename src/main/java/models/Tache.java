package models;

public class Tache {
    private int id_tache;
    private String nom_tache;
    private String description;

    // Constructeurs
    public Tache() {
    }

    public Tache(int id_tache, String nom_tache, String description) {
        this.id_tache = id_tache;
        this.nom_tache = nom_tache;
        this.description = description;
    }

    // Getters et Setters
    public int getId_tache() {
        return id_tache;
    }

    public void setId_tache(int id_tache) {
        this.id_tache = id_tache;
    }

    public String getNom_tache() {
        return nom_tache;
    }

    public void setNom_tache(String nom_tache) {
        this.nom_tache = nom_tache;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Tache{" +
                "id_tache=" + id_tache +
                ", nom_tache=" + nom_tache +
                ", description=" + description +
                '}';
    }
}