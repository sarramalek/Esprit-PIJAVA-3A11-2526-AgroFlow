package test;

import entities.Article;
import entities.Categorie;
import services.ArticleService;
import services.CategorieService;
import java.sql.SQLException;
import java.util.List;

public class Main_stocks {
    public static void main(String[] args) {
        CategorieService cs = new CategorieService();
        ArticleService as = new ArticleService();

        try {
            System.out.println("✅ Connexion réussie !");

            // --- 1. AJOUT DE PLUSIEURS CATÉGORIES ---
            System.out.println("\n--- Ajout des catégories ---");
            cs.ajouter(new Categorie(0, "Semences", "Graines et semences agricoles"));
            cs.ajouter(new Categorie(0, "Outils", "Matériel de jardinage et champs"));

            // Récupérons les catégories pour avoir leurs vrais IDs (auto-incrémentés)
            List<Categorie> cats = cs.recuperer();
            int idSemence = cats.get(0).getId();
            int idOutil = cats.get(1).getId();

            // --- 2. AJOUT DE PLUSIEURS ARTICLES ---
            System.out.println("\n--- Ajout des articles ---");
            // Articles pour la catégorie Semences
            as.ajouter(new Article(0, "Blé tendre", 1200.0, 100.0, "Kg", idSemence));
            as.ajouter(new Article(0, "Tomate Cerise", 50.0, 5.0, "Sachet", idSemence));

            // Article pour la catégorie Outils
            as.ajouter(new Article(0, "Pelle en acier", 15.0, 2.0, "Pièce", idOutil));

            // --- 3. VÉRIFICATION DE LA LISTE TOTALE ---
            System.out.println("\n--- Inventaire complet du stock AgroFlow ---");
            List<Article> inventaire = as.recuperer();

            for (Article a : inventaire) {
                String alerte = (a.getQuantiteEnStock() <= a.getSeuilAlerte()) ? " ⚠️ REAPPROVISIONNER !" : " OK";
                System.out.println("- " + a.getNom() + " | Stock: " + a.getQuantiteEnStock() + " " + a.getUniteMesure() + " | Statut:" + alerte);
            }

            // --- 4. TEST DE SUPPRESSION D'UN SEUL ARTICLE ---
            // On supprime par exemple le deuxième article de la liste
            if (inventaire.size() > 1) {
                int idASupprimer = inventaire.get(1).getId();
                as.supprimer(idASupprimer);
                System.out.println("\n🗑️ Article ID " + idASupprimer + " supprimé.");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur : " + e.getMessage());
        }
    }
}