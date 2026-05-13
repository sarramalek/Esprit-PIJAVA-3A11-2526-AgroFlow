package models.Stocks;

/**
 * Entité Categorie simplifiée.
 */
public class Categorie {
    private int id;
    private String nom;
    private String description;
    private int idUser;          // ID de l'agriculteur propriétaire
    private Integer idAdmin;     // ID de l'admin (si créé par admin)
    private String nomAgriculteur; // Nom complet (pour affichage Admin)
    private int nbArticles;      // Nombre d'articles liés

    // --- CONSTRUCTEURS ---

    public Categorie() {}

    public Categorie(int id, String nom, String description, int idUser, Integer idAdmin) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.idUser = idUser;
        this.idAdmin = idAdmin;
    }

    public Categorie(int id, String nom, String description) {
        this(id, nom, description, 0, null);
    }

    public Categorie(String nom, String description, int idUser) {
        this.nom = nom;
        this.description = description;
        this.idUser = idUser;
    }

    // --- GETTERS ET SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public Integer getIdAdmin() { return idAdmin; }
    public void setIdAdmin(Integer idAdmin) { this.idAdmin = idAdmin; }

    public String getNomAgriculteur() { return nomAgriculteur; }
    public void setNomAgriculteur(String nomAgriculteur) { this.nomAgriculteur = nomAgriculteur; }

    public int getNbArticles() { return nbArticles; }
    public void setNbArticles(int nbArticles) { this.nbArticles = nbArticles; }

    @Override
    public String toString() {
        return "Categorie{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", idUser=" + idUser +
                ", nbArticles=" + nbArticles +
                '}';
    }
}