package models.User;

public class Employe extends Personne {

    // Constructeur par défaut
    public Employe() {
        super();
    }

    // Constructeur avec paramètres
    public Employe(int cin, String nom, String prenom, String tel, String date_naiss,
                   String email, String mdp, String adresse, String ville,
                   String date_creationcpt, String date_dernierchg) {
        super(cin, nom, prenom, tel, date_naiss, email, mdp, adresse, ville,
                date_creationcpt, date_dernierchg);
    }

    @Override
    public int getRole() {
        return 2; // Rôle Employé
    }

    @Override
    public String getRoleNom() {
        return "Employé";
    }

    @Override
    public String toString() {
        return "Employe{" +
                "cin=" + getCin() +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", tel='" + getTel() + '\'' +
                ", ville='" + getVille() + '\'' +
                '}';
    }
}