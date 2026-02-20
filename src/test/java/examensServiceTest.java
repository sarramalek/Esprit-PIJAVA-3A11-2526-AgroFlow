// Importations nécessaires pour les entités, le service et les outils de test JUnit 5
import entities.examens;
import org.junit.jupiter.api.*;
import services.ServiceExamen;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

// Importation statique pour utiliser directement les méthodes de validation (ex: assertTrue)
import static org.junit.jupiter.api.Assertions.*;

// Définit l'ordre d'exécution des tests selon les chiffres indiqués dans @Order
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class examensServiceTest {
    private static ServiceExamen service;

    // S'exécute une seule fois avant le lancement du tout premier test
    @BeforeAll
    static void setUp() {
        System.out.println("Initialisation du service Examen...");
        service = new ServiceExamen();
    }

    @Test
    @Order(1) // Étape 1 : Vérifier que la récupération des données ne plante pas
    void afficher() throws SQLException {
        System.out.println("Running afficher examen test...");
        List<examens> liste = service.afficher();

        // On s'assure que l'objet liste est bien créé par le service
        assertNotNull(liste, "La liste ne doit pas être null.");

        // On vérifie que le contenu de la liste correspond bien à la classe 'examens'
        if (!liste.isEmpty()) {
            assertTrue(liste.get(0) instanceof examens);
        }
    }

    @Test
    @Order(2) // Étape 2 : Tester l'insertion d'un nouvel examen
    void ajouter() throws SQLException {
        System.out.println("Running ajouter examen test...");

        // Création d'un objet de test (ID 0 car auto-incrémenté, id_animal 1 doit exister en BDD)
        examens examen = new examens(0, new java.util.Date(), "Radio", "Suspicion fracture", "Repos", 1);

        // Action : Appel de la méthode du service
        service.ajouter(examen);

        // Vérification : La liste doit contenir au moins un élément maintenant
        List<examens> liste = service.afficher();
        assertFalse(liste.isEmpty());

        // Recherche via Stream pour confirmer que le type "Radio" est bien présent
        boolean trouve = liste.stream()
                .anyMatch(e -> e.getType_examen().equalsIgnoreCase("Radio"));

        assertTrue(trouve, "L'examen 'Radio' n'a pas été ajouté !");
    }

    @Test
    @Order(3) // Étape 3 : Tester la mise à jour des données
    void modifier() throws SQLException {
        System.out.println("Running modifier examen test...");

        // 1. On récupère le dernier examen de la liste (celui qu'on vient d'ajouter)
        List<examens> liste = service.afficher();
        examens examen = liste.get(liste.size() - 1);

        // 2. On modifie ses valeurs localement
        examen.setType_examen("Scanner");
        examen.setDiagnostic("OK");

        // 3. Action : Enregistrement des modifications en BDD
        service.modifier(examen);

        // 4. Vérification : On cherche si un examen possède les nouvelles valeurs "Scanner" et "OK"
        boolean trouve = service.afficher().stream()
                .anyMatch(e -> e.getType_examen().equals("Scanner") && e.getDiagnostic().equals("OK"));

        assertTrue(trouve, "La modification n'a pas été prise en compte.");
    }

    @Test
    @Order(4) // Étape 4 : Tester la suppression
    void supprimer() throws SQLException {
        System.out.println("Running supprimer examen test...");

        // 1. On récupère l'ID du dernier examen présent dans la liste
        List<examens> liste = service.afficher();
        int idASupprimer = liste.get(liste.size() - 1).getId();

        // 2. Action : On demande la suppression par ID
        service.supprimer(idASupprimer);

        // 3. Vérification : On cherche si cet ID existe encore dans la liste
        boolean existe = service.afficher().stream()
                .anyMatch(e -> e.getId() == idASupprimer);

        // Le test réussit si 'existe' est faux (donc assertFalse)
        assertFalse(existe, "L'examen n'a pas été supprimé de la base de données.");
    }
}