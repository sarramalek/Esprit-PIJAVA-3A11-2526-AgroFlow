package models.Materiels;

import java.time.LocalDate;
import java.util.Objects;

public class Machine {

    private int idM;
    private String nom;
    private String marque;
    private String modele;
    private String numeroSerie;
    private String etatM;
    private LocalDate dateAchat;
    private int kilometrage;
    private LocalDate dateLastVisite;
    private int kmLastVisite;
    private LocalDate prochaineMaintenance;
    private int cin;

    public Machine() {}

    public Machine(int idM, String nom, String marque, String modele,
                   String numeroSerie, String etatM, LocalDate dateAchat,
                   int kilometrage, LocalDate dateLastVisite, int kmLastVisite,
                   LocalDate prochaineMaintenance, int cin) {
        this.idM = idM;
        this.nom = nom;
        this.marque = marque;
        this.modele = modele;
        this.numeroSerie = numeroSerie;
        this.etatM = etatM;
        this.dateAchat = dateAchat;
        this.kilometrage = kilometrage;
        this.dateLastVisite = dateLastVisite;
        this.kmLastVisite = kmLastVisite;
        this.prochaineMaintenance = prochaineMaintenance;
        this.cin = cin;
    }

    public int getIdM() { return idM; }
    public void setIdM(int idM) { this.idM = idM; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public String getEtatM() { return etatM; }
    public void setEtatM(String etatM) { this.etatM = etatM; }

    public LocalDate getDateAchat() { return dateAchat; }
    public void setDateAchat(LocalDate dateAchat) { this.dateAchat = dateAchat; }

    public int getKilometrage() { return kilometrage; }
    public void setKilometrage(int kilometrage) { this.kilometrage = kilometrage; }

    public LocalDate getDateLastVisite() { return dateLastVisite; }
    public void setDateLastVisite(LocalDate dateLastVisite) { this.dateLastVisite = dateLastVisite; }

    public int getKmLastVisite() { return kmLastVisite; }
    public void setKmLastVisite(int kmLastVisite) { this.kmLastVisite = kmLastVisite; }

    public LocalDate getProchaineMaintenance() { return prochaineMaintenance; }
    public void setProchaineMaintenance(LocalDate prochaineMaintenance) { this.prochaineMaintenance = prochaineMaintenance; }

    public int getCin() { return cin; }
    public void setCin(int cin) { this.cin = cin; }

    @Override
    public String toString() {
        return "Machine{" +
                "idM=" + idM +
                ", nom='" + nom + '\'' +
                ", marque='" + marque + '\'' +
                ", modele='" + modele + '\'' +
                ", numeroSerie='" + numeroSerie + '\'' +
                ", etatM='" + etatM + '\'' +
                ", dateAchat=" + dateAchat +
                ", kilometrage=" + kilometrage +
                ", dateLastVisite=" + dateLastVisite +
                ", kmLastVisite=" + kmLastVisite +
                ", prochaineMaintenance=" + prochaineMaintenance +
                ", cin=" + cin +
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