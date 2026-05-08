package models.User;

/**
 * Modèle Ouvrier - rôle 1, associé à un terrain et un agriculteur
 */
public class Employe extends Personne {

    private int idTerrain;       // Terrain assigné
    private int cinAgriculteur;  // CIN de l'agriculteur qui l'a créé

    public Employe() {
        super();
    }

        public Employe(int cin, String nom, String prenom, String tel, String date_naiss,
                   String email, String mdp, String adresse, String ville,
                   String date_creationcpt, String date_dernierchg,
                   int idTerrain, int cinAgriculteur) {
        super(cin, nom, prenom, tel, date_naiss, email, mdp, adresse, ville,
                date_creationcpt, date_dernierchg);
        this.idTerrain = idTerrain;
        this.cinAgriculteur = cinAgriculteur;
    }

    @Override
    public int getRole() {
        return 1; // Ouvrier = role 1
    }

    @Override
    public String getRoleNom() {
        return "Ouvrier";
    }

    public int getIdTerrain() { return idTerrain; }
    public void setIdTerrain(int idTerrain) { this.idTerrain = idTerrain; }

    public int getCinAgriculteur() { return cinAgriculteur; }
    public void setCinAgriculteur(int cinAgriculteur) { this.cinAgriculteur = cinAgriculteur; }

    @Override
    public String toString() {
        return getNom() + " " + getPrenom() + " (Terrain: " + idTerrain + ")";
    }
}
