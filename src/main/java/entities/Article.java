package entities;

public class Article {
    private int id;
    private String nom;
    private double quantiteEnStock;
    private double seuilAlerte;
    private String uniteMesure;
    private int idCategorie; // L'attribut qui manquait

    // Constructeur vide (Indispensable pour charger les données de la DB)
    public Article() {}

    // Constructeur complet
    public Article(int id, String nom, double quantiteEnStock, double seuilAlerte, String uniteMesure, int idCategorie) {
        this.id = id;
        this.nom = nom;
        this.quantiteEnStock = quantiteEnStock;
        this.seuilAlerte = seuilAlerte;
        this.uniteMesure = uniteMesure;
        this.idCategorie = idCategorie;
    }

    public Article(String articleDeTest, double v, double v1, String unité, int i) {
    }

    public Article(String text, double v, int i) {
    }

    // Getters et Setters (Vérifie bien l'orthographe exacte)
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

    // Cette méthode va supprimer l'erreur dans ArticleService
    public int getIdCategorie() { return idCategorie; }
    public void setIdCategorie(int idCategorie) { this.idCategorie = idCategorie; }

    @Override
    public String toString() {
        return "Article{" + "nom='" + nom + '\'' + ", stock=" + quantiteEnStock + '}';
    }
}