package test;

import models.Materiels.Machine;
import models.Materiels.Maintenance;
import models.Materiels.Achat;
import services.Materiels.MachineService;
import services.Materiels.MaintenanceService;
import services.Materiels.AchatService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        MachineService machineService = new MachineService();
        MaintenanceService maintenanceService = new MaintenanceService();
        AchatService achatService = new AchatService();

        try {
            // ================= 1️⃣ AJOUTER UNE MACHINE =================
            Machine machine = new Machine(
                    0,
                    "John Deere",
                    "X100",
                    "Neuf",
                    "JD-X100-2026",
                    LocalDate.now(),
                    "Tracteur"
            );
            machineService.ajouter(machine);
            System.out.println("✅ Machine ajoutée");

            // ================= 2️⃣ RÉCUPÉRER TOUTES LES MACHINES =================
            List<Machine> machines = machineService.recuperer();
            System.out.println("\n📋 Liste des machines :");
            for (Machine m : machines) {
                System.out.println(
                        "ID: " + m.getIdM() +
                                ", Marque: " + m.getMarque() +
                                ", Modèle: " + m.getModele() +
                                ", État: " + m.getEtatM() +
                                ", Numéro Série: " + m.getNumeroSerie() +
                                ", Date Achat: " + m.getDateAchat() +
                                ", Nom: " + m.getNom()
                );
            }

            // On prend la dernière machine pour les opérations suivantes
            Machine machineBase = machines.get(machines.size() - 1);
            int idMachine = machineBase.getIdM();
            System.out.println("\n✅ Machine sélectionnée ID : " + idMachine);

            // ================= 3️⃣ AJOUTER UNE MAINTENANCE =================
            Maintenance maintenance = new Maintenance(
                    0,
                    "Moteur",
                    300,
                    LocalDate.now(),
                    "Vidange + filtre",
                    idMachine
            );
            maintenanceService.ajouter(maintenance);
            System.out.println("✅ Maintenance ajoutée");

            // ================= 4️⃣ AJOUTER UN ACHAT =================
            int cinClient = 12345678; // CIN fictif
            Achat achat = new Achat(
                    0,
                    LocalDate.now(),
                    idMachine,
                    cinClient,
                    2
            );
            achatService.ajouter(achat);
            System.out.println("✅ Achat ajouté");

            // ================= 5️⃣ MODIFIER LA MACHINE =================
            Machine machineModif = new Machine(
                    idMachine,
                    "Kubota",
                    "K200",
                    "Occasion",
                    machineBase.getNumeroSerie(),
                    machineBase.getDateAchat(),
                    machineBase.getNom()
            );
            machineService.modifier(machineModif);
            System.out.println("✏️ Machine modifiée");

            // ================= 6️⃣ MODIFIER LA MAINTENANCE =================
            List<Maintenance> maintenances = maintenanceService.recupererParMachine(idMachine);
            if (!maintenances.isEmpty()) {
                Maintenance mModif = new Maintenance(
                        maintenances.get(0).getIdMain(),
                        "Hydraulique",
                        450,
                        LocalDate.now(),
                        "Remplacement flexible",
                        idMachine
                );
                maintenanceService.modifier(mModif);
                System.out.println("✏️ Maintenance modifiée");
            }

            // ================= 7️⃣ MODIFIER L'ACHAT =================
            List<Achat> achats = achatService.recuperer();
            if (!achats.isEmpty()) {
                Achat achatModif = new Achat(
                        achats.get(0).getIdAchat(),
                        LocalDate.now(),
                        idMachine,
                        cinClient,
                        3 // nouvelle quantité
                );
                achatService.modifier(achatModif);
                System.out.println("✏️ Achat modifié");
            }

            // ================= 8️⃣ AFFICHER MAINTENANCES AVEC MACHINE =================
            System.out.println("\n📋 Maintenances avec Machine :");
            maintenanceService.afficherMaintenanceAvecMachine();

            // ================= 9️⃣ AFFICHER ACHATS AVEC MACHINE =================
            System.out.println("\n📋 Achats avec Machine :");
            achatService.afficherAchatAvecMachine();

            // ================= 🔟 SUPPRIMER L'ACHAT =================
            if (!achats.isEmpty()) {
                int idASupprimer = achats.get(0).getIdAchat();
                achatService.supprimer(idASupprimer);
                System.out.println("🗑️ Achat supprimé ID: " + idASupprimer);
            }

            // ================= 1️⃣1️⃣ SUPPRIMER LA MAINTENANCE =================
            if (!maintenances.isEmpty()) {
                int idMSupprimer = maintenances.get(0).getIdMain();
                maintenanceService.supprimer(idMSupprimer);
                System.out.println("🗑️ Maintenance supprimée ID: " + idMSupprimer);
            }

            // ================= 1️⃣2️⃣ SUPPRIMER LA MACHINE =================
            machineService.supprimer(idMachine);
            System.out.println("🗑️ Machine supprimée ID: " + idMachine);

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL : " + e.getMessage());
        }
    }
}
