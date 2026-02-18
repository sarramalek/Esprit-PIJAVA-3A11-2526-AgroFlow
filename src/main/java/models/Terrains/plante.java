package models.Terrains;

import java.util.Objects;

public class plante {
    private int id_plante;
    private String nom_p;
    private String variete;
    private float besoin_eau;
    private int cycle_jours;

    public plante(int id_plante, String nom_p, String variete, float besoin_eau, int cycle_jours) {
        this.id_plante = id_plante;
        this.nom_p = nom_p;
        this.variete = variete;
        this.besoin_eau = besoin_eau;
        this.cycle_jours = cycle_jours;
    }

    public int getId_plante() {
        return id_plante;
    }

    public void setId_plante(int id_plante) {
        this.id_plante = id_plante;
    }

    public String getNom_p() {
        return nom_p;
    }

    public void setNom_p(String nom_p) {
        this.nom_p = nom_p;
    }

    public String getVariete() {
        return variete;
    }

    public void setVariete(String variete) {
        this.variete = variete;
    }

    public float getBesoin_eau() {
        return besoin_eau;
    }

    public void setBesoin_eau(float besoin_eau) {
        this.besoin_eau = besoin_eau;
    }

    public int getCycle_jours() {
        return cycle_jours;
    }

    public void setCycle_jours(int cycle_jours) {
        this.cycle_jours = cycle_jours;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        plante plante = (plante) o;
        return id_plante == plante.id_plante && Float.compare(besoin_eau, plante.besoin_eau) == 0 && cycle_jours == plante.cycle_jours && Objects.equals(nom_p, plante.nom_p) && Objects.equals(variete, plante.variete);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_plante, nom_p, variete, besoin_eau, cycle_jours);
    }
}
