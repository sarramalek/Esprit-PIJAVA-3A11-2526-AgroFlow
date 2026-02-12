package test;

import entities.plante;
import entities.rotation;
import entities.terrain;
import services.PlanteService;
import services.RotationService;
import services.TerrainService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Main_terrain {
    public static void main(String[] args) {
        // 1. INITIALISATION DES SERVICES
        PlanteService ps = new PlanteService();
        TerrainService ts = new TerrainService();
        RotationService rs = new RotationService();

        System.out.println("========== [1] CRUD PLANTE ==========");
        // CREATE
        ps.ajouter(new plante(0, "Tomate", "Cerise", 0.5f, 90));
        ps.ajouter(new plante(0, "Fraise", "Gariguette", 0.3f, 60));

        // READ
        List<plante> plantes = ps.afficherToutes();
        System.out.println("Nombre de plantes : " + plantes.size());

        // UPDATE
        if (!plantes.isEmpty()) {
            plante pModif = plantes.get(plantes.size() - 1);
            pModif.setNom_p("Fraise Royale");
            ps.modifier(pModif);
        }

        // DELETE (Exemple : supprimer la plante avec l'ID 7 si elle existe)
        // ps.supprimer(7);

        System.out.println("\n========== [2] CRUD TERRAIN ==========");
        // CREATE
        ts.ajouter(new terrain(0, "Parcelle Sud", 12.5f, "Sableux", "Ariana", 6.2f));
        ts.ajouter(new terrain(0, "Verger Citrons", 5.0f, "Argileux", "Tunis", 7.1f));

        // READ
        List<terrain> terrains = ts.afficherTous();
        for (terrain t : terrains) {
            System.out.println(" > Terrain: " + t.getNom_terrain() + " (ID: " + t.getId_terrain() + ")");
        }

        // UPDATE
        if (!terrains.isEmpty()) {
            terrain tModif = terrains.get(0);
            tModif.setSurface(15.0f);
            ts.modifier(tModif);
        }

        System.out.println("\n========== [3] CRUD ROTATION ==========");
        // On vérifie qu'on a de quoi créer une rotation
        if (plantes.isEmpty() || terrains.isEmpty()) {
            System.out.println("[!] Impossible de tester la rotation : manque de données.");
        } else {
            // CREATE
            // On prend les derniers IDs insérés pour être sûr qu'ils existent
            int idT = terrains.get(terrains.size() - 1).getId_terrain();
            int idP = plantes.get(plantes.size() - 1).getId_plante();

            Date debut = new Date();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 3);
            Date fin = cal.getTime();

            rotation rot = new rotation(0, idT, idP, debut, fin, 0);
            rs.ajouter(rot);

            // READ
            List<rotation> rotations = rs.afficherToutes();
            for (rotation r : rotations) {
                System.out.println(" > Rotation ID: " + r.getId_rotation() +
                        " [Terrain: " + r.getId_terrain() + " -> Plante: " + r.getId_plante() + "]");
            }

            // UPDATE
            if (!rotations.isEmpty()) {
                rotation rModif = rotations.get(0);
                rModif.setStatus(1); // On passe en statut "Terminé"
                rs.modifier(rModif);
            }

            // DELETE
            // rs.supprimer(1);
        }

        System.out.println("\n===========================================");
        System.out.println("   TOUS LES TESTS CRUD SONT TERMINÉS !");
        System.out.println("===========================================");
    }

    /**
     * Méthode utilitaire pour afficher les terrains proprement
     */
    public static void afficherListeTerrains(List<terrain> list) {
        if (list.isEmpty()) {
            System.out.println("Aucun terrain trouvé.");
        } else {
            for (terrain t : list) {
                System.out.println("ID: " + t.getId_terrain() + " | Nom: " + t.getNom_terrain() + " | pH: " + t.getP_h());
            }
        }
    }
}