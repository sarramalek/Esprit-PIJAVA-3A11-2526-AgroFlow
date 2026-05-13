package models.User;

public abstract class Personne {
    private int cin;
    private String nom;
    private String prenom;
    private String tel;
    private String date_naiss;
    private String email;
    private String mdp;
    private String adresse;
    private String ville;
    private String date_creationcpt;
    private String date_dernierchg;

    public String getPhotoUrl() {
        return photoUrl;
    }

    private String photoUrl;

    // Constructeur par défaut
    public Personne() {
    }

    // Constructeur avec paramètres
    public Personne(int cin, String nom, String prenom, String tel, String date_naiss,
                    String email, String mdp, String adresse, String ville,
                    String date_creationcpt, String date_dernierchg) {
        this.cin = cin;
        this.nom = nom;
        this.prenom = prenom;
        this.tel = tel;
        this.date_naiss = date_naiss;
        this.email = email;
        this.mdp = mdp;
        this.adresse = adresse;
        this.ville = ville;
        this.date_creationcpt = date_creationcpt;
        this.date_dernierchg = date_dernierchg;
        this.photoUrl = "";
    }

    // Méthode abstraite pour obtenir le rôle
    public abstract int getRole();
    public abstract String getRoleNom();

    // Getters et Setters
    public int getCin() {
        return cin;
    }

    public void setCin(int cin) {
        this.cin = cin;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getDate_naiss() {
        return date_naiss;
    }

    public void setDate_naiss(String date_naiss) {
        this.date_naiss = date_naiss;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getDate_creationcpt() {
        return date_creationcpt;
    }

    public void setDate_creationcpt(String date_creationcpt) {
        this.date_creationcpt = date_creationcpt;
    }

    public String getDate_dernierchg() {
        return date_dernierchg;
    }

    public void setDate_dernierchg(String date_dernierchg) {
        this.date_dernierchg = date_dernierchg;
    }
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public String toString() {
        return "Personne{" +
                "cin=" + cin +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", tel='" + tel + '\'' +
                ", ville='" + ville + '\'' +
                ", role='" + getRoleNom() + '\'' +
                '}';
    }
}

