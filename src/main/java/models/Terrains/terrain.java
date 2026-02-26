package models.Terrains;

import java.util.Objects;

public class terrain {
    private int id_terrain;
    private String nom_terrain;
    private float surface;
    private String type_sol;
    private String localisation;
    private float p_h;
    private float proprietaire;

    public terrain(int id_terrain, String nom_terrain, float surface, String type_sol, String localisation, float p_h) {
        this.id_terrain = id_terrain;
        this.nom_terrain = nom_terrain;
        this.surface = surface;
        this.type_sol = type_sol;
        this.localisation = localisation;
        this.p_h = p_h;
    }

    public int getId_terrain() {
        return id_terrain;
    }

    public void setId_terrain(int id_terrain) {
        this.id_terrain = id_terrain;
    }

    public String getNom_terrain() {
        return nom_terrain;
    }

    public void setNom_terrain(String nom_terrain) {
        this.nom_terrain = nom_terrain;
    }

    public float getSurface() {
        return surface;
    }

    public void setSurface(float surface) {
        this.surface = surface;
    }

    public String getType_sol() {
        return type_sol;
    }

    public void setType_sol(String type_sol) {
        this.type_sol = type_sol;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public float getP_h() {
        return p_h;
    }

    public void setP_h(float p_h) {
        this.p_h = p_h;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        terrain terrain = (terrain) o;
        return id_terrain == terrain.id_terrain && Float.compare(surface, terrain.surface) == 0 && Float.compare(p_h, terrain.p_h) == 0 && Objects.equals(nom_terrain, terrain.nom_terrain) && Objects.equals(type_sol, terrain.type_sol) && Objects.equals(localisation, terrain.localisation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_terrain, nom_terrain, surface, type_sol, localisation, p_h);
    }
}