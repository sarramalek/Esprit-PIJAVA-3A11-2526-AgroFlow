package entities;

public class Abonnements {
    private int id_abonn;
    private int cin;
    private int id_offre;
    private String date_inscription;
    private String date_expiration;
    private String situation;

    // Constructeurs
    public Abonnements() {
    }

    public Abonnements(int id_abonn, int cin, int id_offre, String date_inscription,
                      String date_expiration, String situation) {
        this.id_abonn = id_abonn;
        this.cin = cin;
        this.id_offre = id_offre;
        this.date_inscription = date_inscription;
        this.date_expiration = date_expiration;
        this.situation = situation;
    }

    // Getters et Setters
    public int getId_abonn() {
        return id_abonn;
    }

    public void setId_abonn(int id_abonn) {
        this.id_abonn = id_abonn;
    }

    public int getCin() {
        return cin;
    }

    public void setCin(int cin) {
        this.cin = cin;
    }

    public int getId_offre() {
        return id_offre;
    }

    public void setId_offre(int id_offre) {
        this.id_offre = id_offre;
    }

    public String getDate_inscription() {
        return date_inscription;
    }

    public void setDate_inscription(String date_inscription) {
        this.date_inscription = date_inscription;
    }

    public String getDate_expiration() {
        return date_expiration;
    }

    public void setDate_expiration(String date_expiration) {
        this.date_expiration = date_expiration;
    }

    public String getSituation() {
        return situation;
    }

    public void setSituation(String situation) {
        this.situation = situation;
    }

    @Override
    public String toString() {
        return "Abonnement{" +
                "id_abonn=" + id_abonn +
                ", cin=" + cin +
                ", id_offre=" + id_offre +
                ", date_inscription='" + date_inscription + '\'' +
                ", date_expiration='" + date_expiration + '\'' +
                ", situation='" + situation + '\'' +
                '}';
    }

}
