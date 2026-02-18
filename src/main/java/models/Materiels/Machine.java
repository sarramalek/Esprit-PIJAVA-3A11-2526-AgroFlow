package models.Materiels;

import java.time.LocalDate;
import java.util.Objects;

public class Machine {

    private int idM;
    private String marque;
    private String modele;
    private String etatM;
    private String numeroSerie;
    private LocalDate dateAchat;

    private String nom; // remplacement de cin par nom

    public Machine() {
    }

    public Machine(int idM, String marque, String modele, String etatM,
                   String numeroSerie, LocalDate dateAchat, String nom) {
        this.idM = idM;
        this.marque = marque;
        this.modele = modele;
        this.etatM = etatM;
        this.numeroSerie = numeroSerie;
        this.dateAchat = dateAchat;
        this.nom = nom;
    }

    // Getters et Setters
    public int getIdM() {
        return idM;
    }

    public void setIdM(int idM) {
        this.idM = idM;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getEtatM() {
        return etatM;
    }

    public void setEtatM(String etatM) {
        this.etatM = etatM;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public LocalDate getDateAchat() {
        return dateAchat;
    }

    public void setDateAchat(LocalDate dateAchat) {
        this.dateAchat = dateAchat;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return "Machine{" +
                "idM=" + idM +
                ", marque='" + marque + '\'' +
                ", modele='" + modele + '\'' +
                ", etatM='" + etatM + '\'' +
                ", numeroSerie='" + numeroSerie + '\'' +
                ", dateAchat=" + dateAchat +
                ", nom='" + nom + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Machine machine = (Machine) o;
        return idM == machine.idM;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idM);
    }
}
