package models.User;

public class Tache {
    private int id_tache;
    private String nom_tache;
    private String description;
    private int assignee;
    private String etat;
    private String priorite;
    private String date_echeancee;

    public Tache() {}

    public Tache(int id_tache, String nom_tache, String description,
                 int assignee, String etat, String priorite, String date_echeancee) {
        this.id_tache = id_tache;
        this.nom_tache = nom_tache;
        this.description = description;
        this.assignee = assignee;
        this.etat = etat;
        this.priorite = priorite;
        this.date_echeancee = date_echeancee;
    }

    public int getId_tache() { return id_tache; }
    public void setId_tache(int id_tache) { this.id_tache = id_tache; }

    public String getNom_tache() { return nom_tache; }
    public void setNom_tache(String nom_tache) { this.nom_tache = nom_tache; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getAssignee() { return assignee; }
    public void setAssignee(int assignee) { this.assignee = assignee; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getDate_echeancee() { return date_echeancee; }
    public void setDate_echeancee(String date_echeancee) { this.date_echeancee = date_echeancee; }

    @Override
    public String toString() {
        return "Tache{" +
                "id_tache=" + id_tache +
                ", nom_tache=" + nom_tache +
                ", description=" + description +
                ", assignee=" + assignee +
                ", etat=" + etat +
                ", priorite=" + priorite +
                ", date_echeancee=" + date_echeancee +
                '}';
    }
}