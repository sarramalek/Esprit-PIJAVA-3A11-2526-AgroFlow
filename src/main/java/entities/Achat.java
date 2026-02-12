package entities;

import java.time.LocalDate;
import java.util.Objects;

public class Achat {

    private int idAchat;
    private LocalDate dateAchat;
    private int idM;       // id de la machine
    private int cin;       // identifiant du client
    private int quantite;  // nouvelle propriété

    // Constructeurs
    public Achat() {}

    public Achat(int idAchat, LocalDate dateAchat, int idM, int cin, int quantite) {
        this.idAchat = idAchat;
        this.dateAchat = dateAchat;
        this.idM = idM;
        this.cin = cin;
        this.quantite = quantite;
    }

    // Getters et Setters
    public int getIdAchat() { return idAchat; }
    public void setIdAchat(int idAchat) { this.idAchat = idAchat; }

    public LocalDate getDateAchat() { return dateAchat; }
    public void setDateAchat(LocalDate dateAchat) { this.dateAchat = dateAchat; }

    public int getIdM() { return idM; }
    public void setIdM(int idM) { this.idM = idM; }

    public int getCin() { return cin; }
    public void setCin(int cin) { this.cin = cin; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    @Override
    public String toString() {
        return "Achat{" +
                "idAchat=" + idAchat +
                ", dateAchat=" + dateAchat +
                ", idM=" + idM +
                ", cin=" + cin +
                ", quantite=" + quantite +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Achat achat = (Achat) o;
        return idAchat == achat.idAchat;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAchat);
    }
}
