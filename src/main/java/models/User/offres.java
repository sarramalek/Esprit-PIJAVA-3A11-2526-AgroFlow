package models.User;

import java.util.Objects;

public class offres {
    private int id_offres;
    private static String nom_offre;
    private String description;
    private float prix;
    private int duree_offre;

    public offres() {
    }

    public offres(int id_offres, String nom_offre, String description, float prix, int duree_offre) {
        this.id_offres = id_offres;
        this.nom_offre = nom_offre;
        this.description = description;
        this.prix = prix;
        this.duree_offre = duree_offre;
    }

    public int getId_offres() {
        return id_offres;
    }

    public void setId_offres(int id_offres) {
        this.id_offres = id_offres;
    }

    public static String getNom_offre() {
        return nom_offre;
    }

    public void setNom_offre(String nom_offre) {
        this.nom_offre = nom_offre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public int getDuree_offre() {
        return duree_offre;
    }

    public void setDuree_offre(int duree_offre) {
        this.duree_offre = duree_offre;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        offres offres = (offres) o;
        return id_offres == offres.id_offres && Float.compare(prix, offres.prix) == 0 && duree_offre == offres.duree_offre && Objects.equals(nom_offre, offres.nom_offre) && Objects.equals(description, offres.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_offres, nom_offre, description, prix, duree_offre);
    }

    @Override
    public String toString() {
        return "offres{" +
                "id_offres=" + id_offres +
                ", nom_offre='" + nom_offre + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", duree_offre=" + duree_offre +
                '}';
    }


}
