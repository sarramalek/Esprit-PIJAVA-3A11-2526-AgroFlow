package models.Stocks;

import java.time.LocalDateTime;

public class Mouvement {
    private int id;
    private int articleId;
    private String type; // 'ENTREE' or 'SORTIE'
    private double quantite;
    private LocalDateTime dateMouvement;
    private String motif;
    private int idUser;
    private Integer idAdmin;

    public Mouvement() {
    }

    public Mouvement(int articleId, String type, double quantite, LocalDateTime dateMouvement, String motif, int idUser, Integer idAdmin) {
        this.articleId = articleId;
        this.type = type;
        this.quantite = quantite;
        this.dateMouvement = dateMouvement;
        this.motif = motif;
        this.idUser = idUser;
        this.idAdmin = idAdmin;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getArticleId() { return articleId; }
    public void setArticleId(int articleId) { this.articleId = articleId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getQuantite() { return quantite; }
    public void setQuantite(double quantite) { this.quantite = quantite; }

    public LocalDateTime getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement = dateMouvement; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }

    public Integer getIdAdmin() { return idAdmin; }
    public void setIdAdmin(Integer idAdmin) { this.idAdmin = idAdmin; }
}
