
import entities.Article;
import services.ArticleService;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ArticleServicesTestTest {

    private static ArticleService service;

    @BeforeAll
    static void setUp() {
        service = new ArticleService();
    }

    @Test
    @Order(1)
    void ajouter() throws SQLException {
        System.out.println("Running ajouter test...");
    Article article = new Article(0, "bobi", 100.0, 10.0, "unite", 1);

    service.ajouter(article);

    List<Article> liste = service.recuperer();
    Assertions.assertFalse(liste.isEmpty());

    // Utilise anyMatch pour chercher l'article partout dans la liste (Style Mr)
    // ATTENTION : Si tu crées "bobi", tu dois chercher "bobi" pour que assertTrue passe.
    boolean trouve = liste.stream()
            .anyMatch(a -> ((Article)a).getNom().equalsIgnoreCase("bobi"));

    Assertions.assertTrue(trouve, "L'article 'bobi' n'a pas été trouvé dans la base !");
}



    @Test
    @Order(2)
    void modifierArticleTest() throws SQLException {
        System.out.println("Test 2 : Modification");
        List<Article> articles = service.recuperer();
        Article dernier = articles.get(articles.size() - 1);

        dernier.setNom("Nom Modifie");
        service.modifier(dernier);

        Article modifie = service.recuperer().get(service.recuperer().size() - 1);
        assertEquals("Nom Modifie", modifie.getNom());
    }



    @Test
    @Order(3)
    void supprimerArticleTest() throws SQLException {
        System.out.println("Test 3 : Suppression");
        List<Article> articles = service.recuperer();
        Article dernier = articles.get(articles.size() - 1);
        int id = dernier.getId();

        service.supprimer(id);

        boolean existeEncore = service.recuperer().stream().anyMatch(a -> a.getId() == id);
        assertFalse(existeEncore, "L'article n'a pas été supprimé");
    }

    @AfterAll
    static void tearDown() throws SQLException {
        System.out.println("Nettoyage terminé.");
    }
}