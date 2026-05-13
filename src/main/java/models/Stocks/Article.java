package models.Stocks;

public class Article {
    private int id;
    private String nom;
    private double quantiteEnStock;
    private double seuilAlerte;
    private String uniteMesure;
    private int idCategorie;
    private String nomCategorie;
    
    // Nouveaux attributs de la base de données
    private int idUser;
    private double prixUnitaire;
    private String devise = "Dinar Tunisien (TND)";
    private float prixAchatDevise;
    private int idAdmin;
    private String nomAgriculteur;

    public Article() {}

    public Article(int id, String nom, double quantiteEnStock, double seuilAlerte, String uniteMesure, int idCategorie) {
        this.id = id;
        this.nom = nom;
        this.quantiteEnStock = quantiteEnStock;
        this.seuilAlerte = seuilAlerte;
        this.uniteMesure = uniteMesure;
        this.idCategorie = idCategorie;
    }

    public Article(String articleDeTest, double v, double v1, String unité, int i) {}

    public Article(String text, double v, int i) {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public double getQuantiteEnStock() { return quantiteEnStock; }
    public void setQuantiteEnStock(double qte) { this.quantiteEnStock = qte; }

    public double getSeuilAlerte() { return seuilAlerte; }
    public void setSeuilAlerte(double seuil) { this.seuilAlerte = seuil; }

    public String getUniteMesure() { return uniteMesure; }
    public void setUniteMesure(String unite) { this.uniteMesure = unite; }

    public int getIdCategorie() { return idCategorie; }
    public void setIdCategorie(int idCategorie) { this.idCategorie = idCategorie; }

    public String getNomCategorie() { return nomCategorie; }
    public void setNomCategorie(String nomCategorie) { this.nomCategorie = nomCategorie; }

    // Getters et Setters pour les nouveaux attributs
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public String getDevise() { return devise; }
    public void setDevise(String devise) { this.devise = devise; }

    public float getPrixAchatDevise() { return prixAchatDevise; }
    public void setPrixAchatDevise(float prixAchatDevise) { this.prixAchatDevise = prixAchatDevise; }

    public int getIdAdmin() { return idAdmin; }
    public void setIdAdmin(int idAdmin) { this.idAdmin = idAdmin; }

    public String getNomAgriculteur() { return nomAgriculteur; }
    public void setNomAgriculteur(String nomAgriculteur) { this.nomAgriculteur = nomAgriculteur; }

    @Override
    public String toString() {
        return "Article{" + "nom='" + nom + '\'' + ", stock=" + quantiteEnStock + ", categorie=" + nomCategorie + '}';
    }
}