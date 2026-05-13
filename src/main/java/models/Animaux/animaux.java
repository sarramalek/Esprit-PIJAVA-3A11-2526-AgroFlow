package models.Animaux;

import java.util.Date;
import java.util.Objects;

public class animaux {
    private int id;
    private String nom;
    private String espece;
    private String race;
    private Date date_naissance;
    private Sexe sexe;
    private float poids;

    public animaux() {}

    public animaux(int id, String nom, String espece, String race, Date date_naissance, Sexe sexe, float poids) {
        this.id = id;
        this.nom = nom;
        this.espece = espece;
        this.race = race;
        this.date_naissance = date_naissance;
        this.sexe = sexe;
        this.poids = poids;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEspece() { return espece; }
    public void setEspece(String espece) { this.espece = espece; }
    public String getRace() { return race; }
    public void setRace(String race) { this.race = race; }
    public Date getDate_naissance() { return date_naissance; }
    public void setDate_naissance(Date date_naissance) { this.date_naissance = date_naissance; }
    public Sexe getSexe() { return sexe; }
    public void setSexe(Sexe sexe) { this.sexe = sexe; }
    public float getPoids() { return poids; }
    public void setPoids(float poids) { this.poids = poids; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        animaux animaux = (animaux) o;
        return getId() == animaux.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, espece, race, date_naissance, sexe, poids);
    }

    @Override
    public String toString() {
        return "Animal [ nom=" + nom + ", espece=" + espece + ", sexe=" + sexe + "]";
    }
}