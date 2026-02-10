package models;

public class Affectation {
    private int id_affect;
    private int cin;
    private int id_tache;

    // Constructeurs
    public Affectation() {
    }

    public Affectation(int id_affect, int cin, int id_tache) {
        this.id_affect = id_affect;
        this.cin = cin;
        this.id_tache = id_tache;
    }

    // Getters et Setters
    public int getId_affect() {
        return id_affect;
    }

    public void setId_affect(int id_affect) {
        this.id_affect = id_affect;
    }

    public int getCin() {
        return cin;
    }

    public void setCin(int cin) {
        this.cin = cin;
    }

    public int getId_tache() {
        return id_tache;
    }

    public void setId_tache(int id_tache) {
        this.id_tache = id_tache;
    }

    @Override
    public String toString() {
        return "Affectation{" +
                "id_affect=" + id_affect +
                ", cin=" + cin +
                ", id_tache=" + id_tache +
                '}';
    }
}