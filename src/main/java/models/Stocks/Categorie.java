package models.Stocks;

/**
 * Entité Categorie mise à jour avec le support des images (Pixabay/Upload).
 */
public class Categorie {
    private int id;
    private String nom;
    private String nomEn;
    private String nomAr;
    private String description;
    private String imageUrl;   // <--- NOUVEL ATTRIBUT

    // --- CONSTRUCTEURS ---

    // 1. Constructeur vide
    public Categorie() {}

    // 2. Constructeur complet (6 paramètres) - Utilisé pour l'affichage et la modification
    public Categorie(int id, String nom, String nomEn, String nomAr, String description, String imageUrl) {
        this.id = id;
        this.nom = nom;
        this.nomEn = nomEn;
        this.nomAr = nomAr;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // 3. Constructeur pour l'ajout (5 paramètres sans ID)
    public Categorie(String nom, String nomEn, String nomAr, String description, String imageUrl) {
        this.nom = nom;
        this.nomEn = nomEn;
        this.nomAr = nomAr;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // 4. Constructeur de compatibilité (Ancien code sans multilingue/image)
    public Categorie(int id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.nomEn = "";
        this.nomAr = "";
        this.imageUrl = "";
    }

    // --- GETTERS ET SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getNomEn() { return nomEn; }
    public void setNomEn(String nomEn) { this.nomEn = nomEn; }

    public String getNomAr() { return nomAr; }
    public void setNomAr(String nomAr) { this.nomAr = nomAr; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; } // <--- NOUVEAU
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; } // <--- NOUVEAU

    // --- TOSTRING ---
    @Override
    public String toString() {
        return "Categorie{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", nomEn='" + nomEn + '\'' +
                ", nomAr='" + nomAr + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}