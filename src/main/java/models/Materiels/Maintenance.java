package models.Materiels;

import java.time.LocalDate;
import java.util.Objects;

public class Maintenance {

    private int idMain;
    private String typePanne;
    private double cout;
    private LocalDate dateMain;
    private String description;

    // Clé étrangère vers Machine
    private int idM;

    public Maintenance() {
    }

    public Maintenance(int idMain, String typePanne, double cout,
                       LocalDate dateMain, String description, int idM) {
        this.idMain = idMain;
        this.typePanne = typePanne;
        this.cout = cout;
        this.dateMain = dateMain;
        this.description = description;
        this.idM = idM;
    }

    public int getIdMain() {
        return idMain;
    }

    public void setIdMain(int idMain) {
        this.idMain = idMain;
    }

    public String getTypePanne() {
        return typePanne;
    }

    public void setTypePanne(String typePanne) {
        this.typePanne = typePanne;
    }

    public double getCout() {
        return cout;
    }

    public void setCout(double cout) {
        this.cout = cout;
    }

    public LocalDate getDateMain() {
        return dateMain;
    }

    public void setDateMain(LocalDate dateMain) {
        this.dateMain = dateMain;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIdM() {
        return idM;
    }

    public void setIdM(int idM) {
        this.idM = idM;
    }

    @Override
    public String toString() {
        return "Maintenance{" +
                "idMain=" + idMain +
                ", typePanne='" + typePanne + '\'' +
                ", cout=" + cout +
                ", dateMain=" + dateMain +
                ", description='" + description + '\'' +
                ", idM=" + idM +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Maintenance that = (Maintenance) o;
        return idMain == that.idMain;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMain);
    }
}
