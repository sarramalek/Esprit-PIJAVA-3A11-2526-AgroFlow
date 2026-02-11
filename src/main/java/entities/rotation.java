package entities;

import java.util.Date;
import java.util.Objects;

public class rotation {
    private int id_rotation;
    private int id_terrain;
    private int id_plante;
    private Date date_debut_t;
    private Date date_fin_t;
    private int status;

    public rotation(int id_rotation, int id_terrain, int id_plante, Date date_debut_t, Date date_fin_t, int status) {
        this.id_rotation = id_rotation;
        this.id_terrain = id_terrain;
        this.id_plante = id_plante;
        this.date_debut_t = date_debut_t;
        this.date_fin_t = date_fin_t;
        this.status = status;
    }

    public int getId_plante() {
        return id_plante;
    }

    public void setId_plante(int id_plante) {
        this.id_plante = id_plante;
    }

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        rotation rotation = (rotation) o;
        return id_rotation == rotation.id_rotation && id_terrain == rotation.id_terrain && id_plante == rotation.id_plante && status == rotation.status && Objects.equals(date_debut_t, rotation.date_debut_t) && Objects.equals(date_fin_t, rotation.date_fin_t);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_rotation, id_terrain, id_plante, date_debut_t, date_fin_t, status);
    }
}
