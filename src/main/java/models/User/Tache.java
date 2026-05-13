package models.User;

import java.time.LocalDate;

/**
 * Modèle Tâche - correspond exactement à la table taches :
 * id_tache | nom_tache | description | etat | priorite | date_echeancee | assignee (cin ouvrier)
 */
public class Tache {

    private int id;                  // id_tache
    private String nomTache;         // nom_tache
    private String description;      // description
    private String etat;             // etat
    private String priorite;         // priorite
    private LocalDate dateEcheance;  // date_echeancee
    private int assignee;            // assignee = cin ouvrier (FK → users.cin)

    // Champs utiles côté Java uniquement (non stockés dans taches)
    private int cinAgriculteur;
    private int idTerrain;

    public Tache() {}

    public Tache(String nomTache, String description, String etat, String priorite,
                 LocalDate dateEcheance, int assignee) {
        this.nomTache    = nomTache;
        this.description = description;
        this.etat        = etat;
        this.priorite    = priorite;
        this.dateEcheance = dateEcheance;
        this.assignee    = assignee;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomTache() { return nomTache; }
    public void setNomTache(String nomTache) { this.nomTache = nomTache; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }

    public int getAssignee() { return assignee; }
    public void setAssignee(int assignee) { this.assignee = assignee; }

    public int getCinAgriculteur() { return cinAgriculteur; }
    public void setCinAgriculteur(int cinAgriculteur) { this.cinAgriculteur = cinAgriculteur; }

    public int getIdTerrain() { return idTerrain; }
    public void setIdTerrain(int idTerrain) { this.idTerrain = idTerrain; }

    @Override
    public String toString() {
        return "Tache{id=" + id + ", nom='" + nomTache + "', etat='" + etat
                + "', assignee=" + assignee + "}";
    }
}
