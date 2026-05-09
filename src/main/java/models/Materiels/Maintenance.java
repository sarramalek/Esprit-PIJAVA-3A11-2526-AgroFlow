package models.Materiels;

import java.time.LocalDate;
import java.util.Objects;

public class Maintenance {

    private int       idMain;
    private String    typePanne;
    private double    cout;
    private LocalDate dateMain;
    private String    description;
    private int       idM;           // clé étrangère → Machine

    // ── Nouveaux champs (BDD capture) ─────────────────────────────────────────
    private String    statut;        // enum : 'en_cours', 'termine', 'planifie'
    private String    recommandation; // text
    private String    priorite;      // enum : 'faible', 'moyenne', 'haute', 'urgente'
    private int       kilometrage;   // int(11)

    // ── Constructeurs ──────────────────────────────────────────────────────────
    public Maintenance() {}

    public Maintenance(int idMain, String typePanne, double cout,
                       LocalDate dateMain, String description, int idM,
                       String statut, String recommandation,
                       String priorite, int kilometrage) {
        this.idMain        = idMain;
        this.typePanne     = typePanne;
        this.cout          = cout;
        this.dateMain      = dateMain;
        this.description   = description;
        this.idM           = idM;
        this.statut        = statut;
        this.recommandation = recommandation;
        this.priorite      = priorite;
        this.kilometrage   = kilometrage;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────
    public int       getIdMain()          { return idMain; }
    public void      setIdMain(int v)     { this.idMain = v; }

    public String    getTypePanne()       { return typePanne; }
    public void      setTypePanne(String v){ this.typePanne = v; }

    public double    getCout()            { return cout; }
    public void      setCout(double v)    { this.cout = v; }

    public LocalDate getDateMain()        { return dateMain; }
    public void      setDateMain(LocalDate v){ this.dateMain = v; }

    public String    getDescription()     { return description; }
    public void      setDescription(String v){ this.description = v; }

    public int       getIdM()             { return idM; }
    public void      setIdM(int v)        { this.idM = v; }

    public String    getStatut()          { return statut; }
    public void      setStatut(String v)  { this.statut = v; }

    public String    getRecommandation()  { return recommandation; }
    public void      setRecommandation(String v){ this.recommandation = v; }

    public String    getPriorite()        { return priorite; }
    public void      setPriorite(String v){ this.priorite = v; }

    public int       getKilometrage()     { return kilometrage; }
    public void      setKilometrage(int v){ this.kilometrage = v; }

    // ── toString / equals / hashCode ──────────────────────────────────────────
    @Override
    public String toString() {
        return "Maintenance{idMain=" + idMain
                + ", typePanne='" + typePanne + '\''
                + ", cout=" + cout
                + ", dateMain=" + dateMain
                + ", statut='" + statut + '\''
                + ", priorite='" + priorite + '\''
                + ", kilometrage=" + kilometrage
                + ", idM=" + idM + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return idMain == ((Maintenance) o).idMain;
    }

    @Override
    public int hashCode() { return Objects.hash(idMain); }
}