package models;

import java.sql.Date;
import java.util.Objects;

public class Evenement {

    private int id_evenement;
    private String titre;
    private String description;
    private String typeEvenement;
    private Date dateDebut;
    private Date dateFin;
    private String lieu;
    private String statut;
    private int id_categorie;

    public Evenement() {

    }
    // 🔹 Constructeur
    public Evenement(int id_evenement, String titre, String description, String typeEvenement, Date dateDebut, Date dateFin, String lieu, String statut, int id_categorie) {
        this.id_evenement = id_evenement;
        this.titre = titre;
        this.description = description;
        this.typeEvenement = typeEvenement;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieu = lieu;
        this.statut = statut;
        this.id_categorie = id_categorie;
    }

    // 🔹 Constructeur avec paramètres
    public Evenement(String titre, String description, String typeEvenement, Date dateDebut, Date dateFin, String lieu, String statut, int id_categorie) {
            this.titre = titre;
            this.description = description;
            this.typeEvenement = typeEvenement;
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
            this.lieu = lieu;
            this.statut = statut;
            this.id_categorie = id_categorie;
    }

    // 🔹 Getters & Setters

    public int getIdEvenement() {
        return id_evenement;
    }

    public void setIdEvenement(int id_evenement) {
        this.id_evenement = id_evenement;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeEvenement() {
        return typeEvenement;
    }

    public void setTypeEvenement(String typeEvenement) {
        this.typeEvenement = typeEvenement;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getIdCategorie() {
        return id_categorie;
    }

    public void setIdCategorie(int id_categorie) {
        this.id_categorie = id_categorie;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Evenement evenement = (Evenement) o;
        return id_evenement == evenement.id_evenement && id_categorie == evenement.id_categorie && Objects.equals(titre, evenement.titre) && Objects.equals(description, evenement.description) && Objects.equals(typeEvenement, evenement.typeEvenement) && Objects.equals(dateDebut, evenement.dateDebut) && Objects.equals(dateFin, evenement.dateFin) && Objects.equals(lieu, evenement.lieu) && Objects.equals(statut, evenement.statut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_evenement, titre, description, typeEvenement, dateDebut, dateFin, lieu, statut, id_categorie);
    }

    @Override
    public String toString() {
        return "Evenement{" +
                "id_evenement=" + id_evenement +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", typeEvenement='" + typeEvenement + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", lieu='" + lieu + '\'' +
                ", statut='" + statut + '\'' +
                ", id_categorie=" + id_categorie +
                '}';
    }
}
