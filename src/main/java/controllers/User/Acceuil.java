package controllers.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.User.Abonnements;
import models.User.Personne;
import models.User.offres;
import services.User.AbonnementService;
import services.User.OffresServicees;
import services.User.PersonneService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Acceuil {
    //sub menu
    @FXML private VBox gestionSubmenu, operationsSubmenu,gestionContainer;


    @FXML private Button gestionToggle, operationsToggle;
    @FXML private Button gestionBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button logoutBtn;

    // ── KPI Labels ──────────────────────────────────────────────
    @FXML private Label lblTotalUsers;
    @FXML private Label lblNbEmployes;
    @FXML private Label lblAbonnementsActifs;
    @FXML private Label lblTauxActifs;
    @FXML private Label lblTotalOffres;
    @FXML private Label lblRevenuMensuel;
    @FXML private Label lblAbonnementsExpires;
    @FXML private Label lblTotalAbonnements;

    // ── Progress bars ───────────────────────────────────────────
    @FXML private ProgressBar progressEmployes;
    @FXML private ProgressBar progressActifs;
    @FXML private ProgressBar progressExpires;

    // ── Graphiques ───────────────────────────────────────────────
    @FXML private PieChart pieAbonnements;
    @FXML private PieChart pieUsers;
    @FXML private BarChart<String, Number> barOffres;

    // ── Tableau offres ───────────────────────────────────────────
    @FXML private TableView<OffreRow> tableOffres;
    @FXML private TableColumn<OffreRow, String>  colNomOffre;
    @FXML private TableColumn<OffreRow, String>  colPrix;
    @FXML private TableColumn<OffreRow, Integer> colDuree;
    @FXML private TableColumn<OffreRow, Integer> colNbAbonnes;

    private static Personne currentUser;
    private final PersonneService personneService    = new PersonneService();
    private final AbonnementService abonnementService  = new AbonnementService();
    private final OffresServicees    offresService      = new OffresServicees();

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        // Cacher submenu par défaut
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);

        // 1. Hover sur le bouton Gestion → Ouvre submenu
        gestionBtn.setOnMouseEntered(e -> {
            showGestionSubmenu();
        });

        // 2. Hover sur TOUT le container Gestion → Garde submenu ouvert
        gestionContainer.setOnMouseEntered(e -> {
            showGestionSubmenu();
        });

        // 3. SOURIS SORT DU CONTAINER ENTIER → Ferme submenu
        gestionContainer.setOnMouseExited(e -> {
            hideGestionSubmenu();
        });
        configurerTableau();
        chargerToutesLesStats();
        System.out.println("✓ AccueilController initialisé");
    }

    /**
     * Définir l'utilisateur connecté
     */
    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("✓ Utilisateur défini: " + user.getNom());
        }
    }

    /**
     * Ouvrir le module Personnes
     */

    /**
     * Gérer la déconnexion
     */
    @FXML
    private void handleLogout() {
        System.out.println("🚪 Déconnexion...");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                // On récupère le Stage et la Scene ACTUELLE
                Scene scene = stage.getScene();

                // SOLUTION MIRACLE : On change la racine, pas la scène !
                scene.setRoot(root);

                // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                stage.show();

                System.out.println("✓ Déconnexion réussie");

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
    }

    /**
     * Afficher une erreur
     */
    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher une information
     */
    private static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML private void handleGestionToggle() {
        gestionSubmenu.setVisible(!gestionSubmenu.isVisible());
        String arrow = gestionSubmenu.isVisible() ? "▼" : "▶";
        gestionToggle.setText("⚙️  Gestion " + arrow);
    }

    @FXML private void handleOperationsToggle() {
        operationsSubmenu.setVisible(!operationsSubmenu.isVisible());
        String arrow = operationsSubmenu.isVisible() ? "▼" : "▶";
        operationsToggle.setText("🚜  Opérations " + arrow);
    }
    //-----------------------------------------
    private void chargerToutesLesStats() {
        try {
            List<Personne>     personnes    = personneService.recuperer();
            List<Abonnements>  abonnements  = abonnementService.recuperer();
            List<offres>       offres       = offresService.recuperer();

            mettreAJourKPIs(personnes, abonnements, offres);
            mettreAJourPieAbonnements(abonnements);
            mettreAJourPieUsers(personnes);
            mettreAJourBarOffres(offres, abonnements);
            mettreAJourTableau(offres, abonnements);

        } catch (SQLException e) {
            afficherErreur("Erreur lors du chargement des statistiques : " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    private void mettreAJourKPIs(List<Personne> personnes,
                                 List<Abonnements> abonnements,
                                 List<offres> offres) {

        // ── Utilisateurs ──
        int total    = personnes.size();
        long employes = personnes.stream().filter(p -> p.getRole() == 2).count();
        long admins   = personnes.stream().filter(p -> p.getRole() == 3).count();

        lblTotalUsers.setText(String.valueOf(total));
        lblNbEmployes.setText(employes + " (+" + admins + " admins)");
        progressEmployes.setProgress(total == 0 ? 0 : (double) employes / total);

        // ── Abonnements ──
        long actifs   = abonnements.stream()
                .filter(a -> "actif".equalsIgnoreCase(a.getSituation())
                        || "active".equalsIgnoreCase(a.getSituation()))
                .count();
        long expires  = abonnements.stream()
                .filter(a -> "expiré".equalsIgnoreCase(a.getSituation())
                        || "expire".equalsIgnoreCase(a.getSituation())
                        || "inactif".equalsIgnoreCase(a.getSituation()))
                .count();
        int totalAbonn = abonnements.size();

        lblAbonnementsActifs.setText(String.valueOf(actifs));
        lblTotalAbonnements.setText("/ " + totalAbonn + " total");
        lblAbonnementsExpires.setText(String.valueOf(expires));

        double tauxActifs = totalAbonn == 0 ? 0 : (double) actifs / totalAbonn;
        lblTauxActifs.setText(String.format("%.0f%%", tauxActifs * 100));
        progressActifs.setProgress(tauxActifs);

        double tauxExpires = totalAbonn == 0 ? 0 : (double) expires / totalAbonn;
        progressExpires.setProgress(tauxExpires);

        // ── Offres ──
        lblTotalOffres.setText(String.valueOf(offres.size()));

        // Revenu mensuel estimé = Σ(prix_offre × nb_abonnés_actifs_offre)
        double revenu = 0;
        for (offres o : offres) {
            long nbAbonnesActifs = abonnements.stream()
                    .filter(a -> a.getId_offre() == o.getId_offres()
                            && ("actif".equalsIgnoreCase(a.getSituation())
                            || "active".equalsIgnoreCase(a.getSituation())))
                    .count();
            revenu += o.getPrix() * nbAbonnesActifs;
        }
        lblRevenuMensuel.setText(String.format("%.1f TND", revenu));
    }
    //------------------------------------------------
    private void mettreAJourPieAbonnements(List<Abonnements> abonnements) {
        Map<String, Integer> compteurStatuts = new HashMap<>();
        for (Abonnements a : abonnements) {
            String statut = a.getSituation() == null ? "Inconnu" : a.getSituation();
            // Normaliser pour regrouper
            statut = normaliserStatut(statut);
            compteurStatuts.merge(statut, 1, Integer::sum);
        }

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        compteurStatuts.forEach((statut, count) ->
                data.add(new PieChart.Data(statut + " (" + count + ")", count)));

        pieAbonnements.setData(data);
        pieAbonnements.setStartAngle(90);
    }
    // ─────────────────────────────────────────────────────────────
    private void mettreAJourPieUsers(List<Personne> personnes) {
        long utilisateurs = personnes.stream().filter(p -> p.getRole() == 1).count();
        long employes     = personnes.stream().filter(p -> p.getRole() == 2).count();
        long admins       = personnes.stream().filter(p -> p.getRole() == 3).count();

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        if (utilisateurs > 0) data.add(new PieChart.Data("Utilisateurs (" + utilisateurs + ")", utilisateurs));
        if (employes     > 0) data.add(new PieChart.Data("Employés ("     + employes     + ")", employes));
        if (admins       > 0) data.add(new PieChart.Data("Admins ("       + admins       + ")", admins));

        if (data.isEmpty()) data.add(new PieChart.Data("Aucun utilisateur", 1));

        pieUsers.setData(data);
        pieUsers.setStartAngle(90);
    }

    //--------------------------------------------------------
private void mettreAJourBarOffres(List<offres> offres,
                                  List<Abonnements> abonnements) {
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("Abonnés");

    for (offres o : offres) {
        long nbAbonnes = abonnements.stream()
                .filter(a -> a.getId_offre() == o.getId_offres())
                .count();
        String nomOffre = o.getNom_offre() != null ? o.getNom_offre() : "Offre #" + o.getId_offres();
        // Tronquer si trop long
        if (nomOffre.length() > 15) nomOffre = nomOffre.substring(0, 13) + "…";
        series.getData().add(new XYChart.Data<>(nomOffre, nbAbonnes));
    }

    barOffres.getData().clear();
    barOffres.getData().add(series);
}

    //----------------------------------------
    private void configurerTableau() {
        colNomOffre .setCellValueFactory(new PropertyValueFactory<>("nomOffre"));
        colPrix     .setCellValueFactory(new PropertyValueFactory<>("prix"));
        colDuree    .setCellValueFactory(new PropertyValueFactory<>("duree"));
        colNbAbonnes.setCellValueFactory(new PropertyValueFactory<>("nbAbonnes"));
    }

    private void mettreAJourTableau(List<offres> offres,
                                    List<Abonnements> abonnements) {
        ObservableList<OffreRow> rows = FXCollections.observableArrayList();
        for (offres o : offres) {
            long nb = abonnements.stream()
                    .filter(a -> a.getId_offre() == o.getId_offres())
                    .count();
            rows.add(new OffreRow(
                    o.getNom_offre() != null ? o.getNom_offre() : "—",
                    String.format("%.2f TND", o.getPrix()),
                    o.getDuree_offre(),
                    (int) nb
            ));
        }
        tableOffres.setItems(rows);
    }


    //--------------------------------------------
    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.navigateTo(event,"/UsersInterface/DahboardPersonne.fxml","Personnes - Agroflow");}


    @FXML private void handleTaches(Event event ) { /* Charger vue Tâches */
        this.navigateTo(event,"/UsersInterface/GestionTache.fxml","Taches - Agroflow");}

    @FXML
    private void handleRefresh() {
        chargerToutesLesStats();
    }

    @FXML private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
    this.navigateTo(event,"/UsersInterface/GestionAbonnements.fxml","Abonnement - Agroflow");}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.navigateTo(event,"/UsersInterface/GestionOffre.fxml","Offres - Agroflow");}

    @FXML private void handleGestion(MouseEvent event) { /* Vue principale Gestion */
    }

    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(ActionEvent actionEvent) {
        System.out.println("Dashboard cliqué");
    }

    public void handleAnimals(MouseEvent mouseEvent) {
        this.navigateTo(mouseEvent,"/AnimalsInterface/AfficherAnimaux.fxml","Gestion Animaux - AgroFlow ");

    }




    public void handleStocks(MouseEvent mouseEvent) {
        this.navigateTo(mouseEvent,"/StocksInterface/afficherarticle.fxml","Gestion Stocks - Agroflow ");
    }



    public void handleTerrains(MouseEvent mouseEvent) {
        this.navigateTo(mouseEvent,"/TerrainsInterface/acceuilterrain.fxml","gestion Terrains - AgroFlow ");
    }


   //
   public void handleEvents(MouseEvent mouseEvent) {
       this.navigateTo(mouseEvent,"/G-Evenements/Accueil.fxml","gestion Evenements - AgroFlow ");
   }


    public void handleMateriels(MouseEvent mouseEvent) {
        this.navigateTo(mouseEvent,"/MaterielsInterface/AccueilMateriel.fxml","gestion Materiels - AgroFlow ");
    }
    private void navigateTo(Event event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            // On récupère le Stage et la Scene ACTUELLE
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
        }
    }
    @FXML
    private void openDashboard(MouseEvent event ) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/UsersInterface/StatsDashboard.fxml"));
        Parent root = loader.load();
        // On récupère le Stage et la Scene ACTUELLE
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = stage.getScene();

        // SOLUTION MIRACLE : On change la racine, pas la scène !
        scene.setRoot(root);

        // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
        stage.show();
    }
    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────
    private String normaliserStatut(String statut) {
        if (statut == null) return "Inconnu";
        switch (statut.toLowerCase()) {
            case "actif":
            case "active":   return "Actif";
            case "expiré":
            case "expire":
            case "inactif":  return "Expiré";
            case "suspendu": return "Suspendu";
            default:         return statut;
        }
    }

    private void navigateTo(MouseEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage;
            if (event != null) {
                stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            } else {
                // Cas logout (pas d'event source)
                stage = (Stage) lblTotalUsers.getScene().getWindow();
            }
            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            afficherErreur("Impossible de charger la vue : " + fxmlPath);
        }
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ─────────────────────────────────────────────────────────────
    // CLASSE INTERNE : ligne du tableau offres
    // ─────────────────────────────────────────────────────────────
    public static class OffreRow {
        private final String  nomOffre;
        private final String  prix;
        private final int     duree;
        private final int     nbAbonnes;

        public OffreRow(String nomOffre, String prix, int duree, int nbAbonnes) {
            this.nomOffre  = nomOffre;
            this.prix      = prix;
            this.duree     = duree;
            this.nbAbonnes = nbAbonnes;
        }

        public String  getNomOffre()  { return nomOffre;  }
        public String  getPrix()      { return prix;      }
        public int     getDuree()     { return duree;     }
        public int     getNbAbonnes() { return nbAbonnes; }
    }


}