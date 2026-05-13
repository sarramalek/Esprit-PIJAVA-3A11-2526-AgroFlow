package models.Animaux;

import java.util.Date;

public class examens {
    private int id;
    private Date date_examen;
    private String type_examen;
    private String diagnostic;
    private String traitement;
    private int id_animal;

    public examens() {}

    public examens(int id, Date date_examen, String type_examen, String diagnostic, String traitement, int id_animal) {
        this.id = id;
        this.date_examen = date_examen;
        this.type_examen = type_examen;
        this.diagnostic = diagnostic;
        this.traitement = traitement;
        this.id_animal = id_animal;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public java.util.Date getDate_examen() { return date_examen; }
    public void setDate_examen(java.util.Date date_examen) { this.date_examen = date_examen; }
    public String getType_examen() { return type_examen; }
    public void setType_examen(String type_examen) { this.type_examen = type_examen; }
    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }
    public String getTraitement() { return traitement; }
    public void setTraitement(String traitement) { this.traitement = traitement; }
    public int getId_animal() { return id_animal; }
    public void setId_animal(int id_animal) { this.id_animal = id_animal; }

    @Override
    public String toString() {
        return "Examen [id=" + id + ", type=" + type_examen + ", animalID=" + id_animal + "]";
    }
}