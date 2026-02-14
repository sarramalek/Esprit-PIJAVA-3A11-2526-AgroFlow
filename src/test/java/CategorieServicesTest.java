package services;

import entities.Categorie;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategorieServicesTest {

    private static CategorieService service;

    @BeforeAll
    static void setUp() {
        service = new CategorieService();
    }

    @Test
    @Order(1)
    void ajouterCategorieTest() throws SQLException {
        System.out.println("Test 1 : Ajout de catégorie...");
        // On crée une catégorie de test
        Categorie cat = new Categorie(0, "Engrais", "Produits de nutrition végétale");

        service.ajouter(cat);

        List<Categorie> liste = service.recuperer();
        assertFalse(liste.isEmpty(), "La liste ne devrait pas être vide après l'ajout");

        // Vérification par le nom (Style Mr)
        boolean trouve = liste.stream()
                .anyMatch(c -> c.getNom().equalsIgnoreCase("Engrais"));

        assertTrue(trouve, "La catégorie 'Engrais' n'a pas été trouvée dans la base !");
    }

    @Test
    @Order(2)
    void modifierCategorieTest() throws SQLException {
        System.out.println("Test 2 : Modification de catégorie...");
        List<Categorie> categories = service.recuperer();
        // On récupère la dernière catégorie ajoutée pour la modifier
        Categorie derniere = categories.get(categories.size() - 1);

        derniere.setNom("Bio-Engrais");
        derniere.setDescription("Description modifiée");

        service.modifier(derniere);

        // On recharge la liste pour vérifier
        List<Categorie> categoriesModifiees = service.recuperer();
        Categorie modifiee = categoriesModifiees.get(categoriesModifiees.size() - 1);

        assertEquals("Bio-Engrais", modifiee.getNom(), "Le nom n'a pas été modifié correctement");
        assertEquals("Description modifiée", modifiee.getDescription(), "La description n'a pas été modifiée");
    }

    @Test
    @Order(3)
    void supprimerCategorieTest() throws SQLException {
        System.out.println("Test 3 : Suppression de catégorie...");
        List<Categorie> categories = service.recuperer();
        Categorie derniere = categories.get(categories.size() - 1);
        int id = derniere.getId();

        service.supprimer(id);

        // Vérification que l'ID n'existe plus dans la liste
        boolean existeEncore = service.recuperer().stream()
                .anyMatch(c -> c.getId() == id);

        assertFalse(existeEncore, "La catégorie n'a pas été supprimée de la base de données");
    }

    @AfterAll
    static void tearDown() {
        System.out.println("Tests de CategorieService terminés.");
    }
}