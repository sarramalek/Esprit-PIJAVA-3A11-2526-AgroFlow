package entities;

import java.util.Date;

/**
 * Classe rotation avec tous les champs pour l'affichage dans le TableView
 */
public class rotation {

    // Champs de base (BD)
    private int id_rotation;
    private int id_terrain;
    private int id_plante;
    private Date date_debut_t;
    private Date date_fin_t;
    private int status;

    // ✨ Champs pour l'affichage dans le TableView
    private String nom_terrain;
    private String nom_plante;
    private String variete_plante;

    // --- CONSTRUCTEURS ---

    public rotation() {
    }

    public rotation(int id_rotation, int id_terrain, int id_plante,
                    Date date_debut_t, Date date_fin_t, int status) {
        this.id_rotation = id_rotation;
        this.id_terrain = id_terrain;
        this.id_plante = id_plante;
        this.date_debut_t = date_debut_t;
        this.date_fin_t = date_fin_t;
        this.status = status;
    }

    public rotation(int id_terrain, int id_plante,
                    Date date_debut_t, Date date_fin_t, int status) {
        this.id_terrain = id_terrain;
        this.id_plante = id_plante;
        this.date_debut_t = date_debut_t;
        this.date_fin_t = date_fin_t;
        this.status = status;
    }

    // --- GETTERS ET SETTERS (Champs de base) ---

    public int getId_rotation() {
        return id_rotation;
    }

    public void setId_rotation(int id_rotation) {
        this.id_rotation = id_rotation;
    }

    public int getId_terrain() {
        return id_terrain;
    }

    public void setId_terrain(int id_terrain) {
        this.id_terrain = id_terrain;
    }

    public int getId_plante() {
        return id_plante;
    }

    public void setId_plante(int id_plante) {
        this.id_plante = id_plante;
    }

    public Date getDate_debut_t() {
        return date_debut_t;
    }

    public void setDate_debut_t(Date date_debut_t) {
        this.date_debut_t = date_debut_t;
    }

    public Date getDate_fin_t() {
        return date_fin_t;
    }

    public void setDate_fin_t(Date date_fin_t) {
        this.date_fin_t = date_fin_t;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    // --- GETTERS/SETTERS pour l'affichage ---

    public String getNom_terrain() {
        return nom_terrain;
    }

    public void setNom_terrain(String nom_terrain) {
        this.nom_terrain = nom_terrain;
    }

    public String getNom_plante() {
        return nom_plante;
    }

    public void setNom_plante(String nom_plante) {
        this.nom_plante = nom_plante;
    }

    public String getVariete_plante() {
        return variete_plante;
    }

    public void setVariete_plante(String variete_plante) {
        this.variete_plante = variete_plante;
    }

    /**
     * Pour afficher le statut en texte dans le TableView
     */
    public String getStatusText() {
        return status == 1 ? "En cours" : "Terminée";
    }

    @Override
    public String toString() {
        return "Rotation{" +
                "id=" + id_rotation +
                ", terrain=" + nom_terrain +
                ", plante=" + nom_plante +
                ", variété=" + variete_plante +
                ", statut=" + getStatusText() +
                '}';
    }
}