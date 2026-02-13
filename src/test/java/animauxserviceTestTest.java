

import entities.Sexe;
import entities.animaux;
import org.junit.jupiter.api.*;
import services.ServiceAnimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class animauxserviceTestTest {
    private static ServiceAnimal service;

    @BeforeAll
     static void setUp() {
        System.out.println("setUp");
        service = new ServiceAnimal();

    }

    /*@AfterAll
    static void tearDown() throws SQLException {
        System.out.println("Cleaning up after all tests...");
        List<animaux> liste = service.afficher();
        if (!liste.isEmpty()) {
            animaux animal = liste.get(liste.size() - 1);
            service.supprimer(animal.getId());
            assertFalse(service.afficher().stream().anyMatch(a -> a.getId() == animal.getId()));
        }
    }*/
    @Test
    @Order(1)
    void ajouter() throws SQLException {
        System.out.println("Running ajouter test...");
        animaux animal = new animaux(0, "luca", "chien", "bichon maltais", new java.util.Date(), Sexe.MALE, 250f);

        service.ajouter(animal);

        List<animaux> liste = service.afficher();
        Assertions.assertFalse(liste.isEmpty());

        // Utilise anyMatch pour chercher l'animal partout dans la liste (Style Mr)
        boolean trouve = liste.stream()
                .anyMatch(a -> ((animaux)a).getNom().equalsIgnoreCase("bobo"));

        Assertions.assertTrue(trouve, "L'animal 'bobo' n'a pas été trouvé dans la base !");
    }
    @Test
    @Order(2)
    void modifier() throws SQLException {
        System.out.println("Running modifier test...");

        // 1. Récupérer le dernier animal de la liste avec le cast (animaux)
        animaux animal = (animaux) service.afficher().get(service.afficher().size() - 1);

        // 2. Modifier le nom ET le poids (comme demandé)
        animal.setNom("luca");
        animal.setPoids(500.5f); // On change le poids à 500.5 (le 'f' est pour float)

        // 3. Appeler la méthode de service pour mettre à jour la base de données
        service.modifier(animal);

        // 4. Vérification selon le style de ton prof (Stream)
        List<animaux> animauxList = service.afficher();

        // On vérifie si un animal possède à la fois le nouveau nom ET le nouveau poids
        boolean trouve = animauxList.stream()
                .anyMatch(a -> a.getNom().equals("NomModifie") && a.getPoids() == 500.5f);

        Assertions.assertTrue(trouve);
    }
    /*@Test
    @Order(3)
    void supprimer() throws SQLException { // <--- Ajouté throws pour corriger l'erreur rouge
        System.out.println("Running supprimer test...");

        // Récupération du dernier animal pour avoir son ID
        animaux animal = (animaux) service.afficher().get(service.afficher().size() - 1);
        int idASupprimer = animal.getId();

        // CORRECTION : On passe l'ID (int) et non l'objet animal entier
        service.supprimer(idASupprimer);

        List<animaux> animauxList = service.afficher();
        boolean existe = animauxList.stream()
                .anyMatch(a -> a.getId() == idASupprimer);

        Assertions.assertFalse(existe);
    }*/
    }

