package entities;

public class Admin extends Personne {

    // Constructeur par défaut
    public Admin() {
        super();
    }

    // Constructeur avec paramètres
    public Admin(int cin, String nom, String prenom, String tel, String date_naiss,
                 String email, String mdp, String adresse, String ville,
                 String date_creationcpt, String date_dernierchg) {
        super(cin, nom, prenom, tel, date_naiss, email, mdp, adresse, ville,
                date_creationcpt, date_dernierchg);
    }

    @Override
    public int getRole() {
        return 3; // Rôle Admin
    }

    @Override
    public String getRoleNom() {
        return "Administrateur";
    }

    @Override
    public String toString() {
        return "Admin{" +
                "cin=" + getCin() +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", tel='" + getTel() + '\'' +
                ", ville='" + getVille() + '\'' +
                '}';
    }
}