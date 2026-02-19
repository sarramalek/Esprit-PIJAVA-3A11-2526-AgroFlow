package models.Events;

import java.time.LocalDate;
import java.util.Objects;

public class Participation {
    private int id_participation;
    private String statut_participation;
    private LocalDate date_inscription;
    private boolean presence;
    private int id_evenement;

    public Participation() {}

    public Participation(int id_participation, String statut_participation, LocalDate date_inscription, boolean presence, int id_evenement) {
        this.id_participation = id_participation;
        this.statut_participation = statut_participation;
        this.date_inscription = date_inscription;
        this.presence = presence;
        this.id_evenement = id_evenement;
    }

    public Participation(String statut_participation, LocalDate date_inscription, boolean presence, int id_evenement) {
        this.statut_participation = statut_participation;
        this.date_inscription = date_inscription;
        this.presence = presence;
        this.id_evenement = id_evenement;
    }

    public int getId_participation() {
        return id_participation;
    }

    public void setId_participation(int id_participation) {
        this.id_participation = id_participation;
    }

    public String getStatut_participation() {
        return statut_participation;
    }

    public void setStatut_participation(String statut_participation) {
        this.statut_participation = statut_participation;
    }

    public LocalDate getDate_inscription() {
        return date_inscription;
    }

    public void setDate_inscription(LocalDate date_inscription) {
        this.date_inscription = date_inscription;
    }

    public boolean isPresence() {
        return presence;
    }

    public void setPresence(boolean presence) {
        this.presence = presence;
    }

    public int getId_evenement() {
        return id_evenement;
    }

    public void setId_evenement(int id_evenement) {
        this.id_evenement = id_evenement;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Participation that = (Participation) o;
        return id_participation == that.id_participation && presence == that.presence && id_evenement == that.id_evenement && Objects.equals(statut_participation, that.statut_participation) && Objects.equals(date_inscription, that.date_inscription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_participation, statut_participation, date_inscription, presence, id_evenement);
    }

    @Override
    public String toString() {
        return "Participation{" +
                "id_participation=" + id_participation +
                ", statut_participation='" + statut_participation + '\'' +
                ", date_inscription=" + date_inscription +
                ", presence=" + presence +
                ", id_evenement=" + id_evenement +
                '}';
    }
}
