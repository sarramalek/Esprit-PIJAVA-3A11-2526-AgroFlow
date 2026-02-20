// Importations pour les entités et les outils de test JUnit 5
import entities.Sexe;
import entities.animaux;
import org.junit.jupiter.api.*;
import services.ServiceAnimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
// Importation statique des méthodes de vérification (assertEquals, assertTrue, etc.)
import static org.junit.jupiter.api.Assertions.*;

// Définit que les tests seront exécutés dans l'ordre choisi via l'annotation @Order
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class animauxserviceTestTest {
    private static ServiceAnimal service;

    // S'exécute une seule fois avant tous les tests pour initialiser le service
    @BeforeAll
    static void setUp() {
        System.out.println("Initialisation du service de test...");
        service = new ServiceAnimal();
    }

    @Test
    @Order(1) // Premier test à s'exécuter
    void ajouter() throws SQLException {
        System.out.println("Test de l'ajout en cours...");
        // Création d'un animal test
        animaux animal = new animaux(0, "luca", "chien", "bichon maltais", new java.util.Date(), Sexe.MALE, 250f);

        // Action : on tente l'ajout en base de données
        service.ajouter(animal);

        // Vérification : la liste ne doit plus être vide
        List<animaux> liste = service.afficher();
        Assertions.assertFalse(liste.isEmpty());

        // Utilisation des Streams pour vérifier si l'animal "luca" existe bien dans la liste
        boolean trouve = liste.stream()
                .anyMatch(a -> a.getNom().equalsIgnoreCase("luca"));

        // Si 'trouve' est faux, le test échoue avec le message indiqué
        Assertions.assertTrue(trouve, "L'animal 'luca' n'a pas été trouvé dans la base !");
    }

    @Test
    @Order(2) // Deuxième test (après l'ajout)
    void modifier() throws SQLException {
        System.out.println("Test de la modification en cours...");

        // 1. Récupération du dernier animal ajouté (le plus récent)
        List<animaux> all = service.afficher();
        animaux animal = all.get(all.size() - 1);

        // 2. Modification des attributs de l'objet local
        animal.setNom("luca_modifie");
        animal.setPoids(500.5f);

        // 3. Action : Mise à jour dans la base de données via le service
        service.modifier(animal);

        // 4. Vérification : On recharge la liste et on vérifie si les changements sont appliqués
        List<animaux> animauxList = service.afficher();
        boolean trouve = animauxList.stream()
                .anyMatch(a -> a.getNom().equals("luca_modifie") && a.getPoids() == 500.5f);

        Assertions.assertTrue(trouve, "La modification n'a pas été enregistrée en base !");
    }
}