package models;

import java.util.Objects;

public class CategorieEvenement {
    private int id_categorie;
    private String nom_categorie;
    private String description_categorie;

    public CategorieEvenement()  {}

    public CategorieEvenement(int id_categorie, String nom_categorie, String description_categorie) {
        this.id_categorie = id_categorie;
        this.nom_categorie = nom_categorie;
        this.description_categorie = description_categorie;
    }

    public CategorieEvenement(String nom_categorie, String description_categorie) {
        this.nom_categorie = nom_categorie;
        this.description_categorie = description_categorie;
    }

    public int getId_categorie() {
        return id_categorie;
    }

    public void setId_categorie(int id_categorie) {
        this.id_categorie = id_categorie;
    }

    public String getNom_categorie() {
        return nom_categorie;
    }

    public void setNom_categorie(String nom_categorie) {
        this.nom_categorie = nom_categorie;
    }

    public String getDescription_categorie() {
        return description_categorie;
    }

    public void setDescription_categorie(String description_categorie) {
        this.description_categorie = description_categorie;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CategorieEvenement that = (CategorieEvenement) o;
        return id_categorie == that.id_categorie && Objects.equals(nom_categorie, that.nom_categorie) && Objects.equals(description_categorie, that.description_categorie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_categorie, nom_categorie, description_categorie);
    }

    @Override
    public String toString() {
        return "CategorieEvenement{" +
                "id_categorie=" + id_categorie +
                ", nom_categorie='" + nom_categorie + '\'' +
                ", description_categorie='" + description_categorie + '\'' +
                '}';
    }
}
