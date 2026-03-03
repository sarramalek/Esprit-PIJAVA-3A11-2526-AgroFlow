package controllers.Materiels;

import controllers.User.MesTaches;
import controllers.User.ProfilEmploye;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import models.Materiels.Maintenance;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.User.Personne;
import services.Materiels.MaintenanceService;
import utils.MyDatabase;
import utils.SessionManager;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MaintenanceFrontController {

    // ══════════════════════════════════════════════════════
    //  FXML — tous les fx:id présents dans le FXML
    // ══════════════════════════════════════════════════════
    @FXML private VBox gestionSubmenu;
    @FXML private VBox gestionContainer;
    @FXML private Button gestionBtn;

    @FXML private ImageView avatarImageView;
    @FXML private Label     avatarDefaultLabel;
    @FXML private Circle    avatarBg;
    // Recherche unique
    @FXML private TextField            champRecherche;

    // Filtres
    @FXML private ComboBox<String>     comboMachine;
    @FXML private ComboBox<String>     comboTypePanne;

    // Tableau
    @FXML private TableView<Maintenance>              tableMaintenances;
    @FXML private TableColumn<Maintenance, String>    colMachine;
    @FXML private TableColumn<Maintenance, String>    colTypePanne;
    @FXML private TableColumn<Maintenance, LocalDate> colDate;
    @FXML private TableColumn<Maintenance, Double>    colCout;
    @FXML private TableColumn<Maintenance, String>    colDescription;

    // Statistiques
    @FXML private Label lblTotal;
    @FXML private Label lblCoutTotal;
    @FXML private Label lblCoutMoyen;
    @FXML private Label lblMachineCouteuse;
    @FXML private Label lblTypeDominant;
    @FXML private Label lblFiltres;

    // Side Bar and Profile User
    @FXML private Button dashboardBtn,logoutBtn;

    @FXML private Label welcomeNameLabel;
    @FXML private Hyperlink aproposLink;

    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    private Personne currentUser;
    //image useer
    @FXML private ImageView sidebarAvatarImageView;
    @FXML private Label     sidebarAvatarDefault;
    @FXML private Circle sidebarAvatarBg;
    // ══════════════════════════════════════════════════════
    //  DATA
    // ══════════════════════════════════════════════════════

    private final MaintenanceService           maintenanceService = new MaintenanceService();
    private final Map<Integer, String>         nomMachineMap      = new HashMap<>();
    private final ObservableList<Maintenance>  toutesMaintenances  = FXCollections.observableArrayList();
    private final ObservableList<Maintenance>  maintenancesFiltrees = FXCollections.observableArrayList();

    // ══════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        chargerNomsMachines();
        configurerColonnes();
        chargerDonnees();
        configurerRechercheDynamique();
        chargerAvatarSidebar(SessionManager.getCurrentUser());
    }
    private void chargerAvatarSidebar(Personne user) {
        if (user == null) return;

        String photoUrl = user.getPhotoUrl();

        if (photoUrl == null || photoUrl.isBlank()
                || photoUrl.equals("0") || photoUrl.equals("null")) {
            // Pas de photo → emoji par défaut, rien à faire
            return;
        }

        // Clip circulaire appliqué en Java (pas possible en FXML)
        Circle clip = new Circle(32, 32, 32);
        avatarImageView.setClip(clip);

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 64, 64, false, true, true);
                Platform.runLater(() -> {
                    if (!image.isError()) {
                        avatarImageView.setImage(image);
                        avatarImageView.setVisible(true);
                        avatarImageView.setManaged(true);
                        avatarDefaultLabel.setVisible(false);
                        avatarDefaultLabel.setManaged(false);
                        if (avatarBg != null) avatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("⚠️ Erreur chargement avatar : " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    // ──────────────────────────────────────────────────────
    //  Charger la map idM → nom  (connexion partagée)
    // ──────────────────────────────────────────────────────
    private void chargerNomsMachines() {
        nomMachineMap.clear();
        Connection conn = MyDatabase.getInstance().getConnection();
        String sql = "SELECT idM, nom FROM machine";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int    idM = rs.getInt("idM");
                String nom = rs.getString("nom");
                nomMachineMap.put(idM, (nom != null && !nom.isBlank()) ? nom : "Machine #" + idM);
            }
            System.out.println("✓ " + nomMachineMap.size() + " machine(s) chargée(s).");
        } catch (SQLException e) {
            System.err.println("⚠ Erreur chargement noms machines : " + e.getMessage());
        }
    }

    private String getNomMachine(int idM) {
        return nomMachineMap.getOrDefault(idM, "Machine #" + idM);
    }

    // ──────────────────────────────────────────────────────
    //  Configuration des colonnes
    // ──────────────────────────────────────────────────────
    private void configurerColonnes() {

        colMachine.setCellValueFactory(cellData ->
                new SimpleStringProperty(getNomMachine(cellData.getValue().getIdM()))
        );

        colTypePanne.setCellValueFactory(new PropertyValueFactory<>("typePanne"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateMain"));

        colCout.setCellValueFactory(new PropertyValueFactory<>("cout"));
        colCout.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null); setStyle("");
                } else {
                    setText(String.format("%.2f DT", val));
                    if      (val >= 500) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    else if (val >= 200) setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                    else                 setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });

        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        tableMaintenances.setEditable(false);
    }

    // ──────────────────────────────────────────────────────
    //  Chargement des maintenances
    // ──────────────────────────────────────────────────────
    private void chargerDonnees() {
        try {
            List<Maintenance> liste = maintenanceService.recuperer();
            toutesMaintenances.setAll(liste);
            maintenancesFiltrees.setAll(liste);
            tableMaintenances.setItems(maintenancesFiltrees);
            remplirComboMachine(liste);
            remplirComboTypePanne(liste);
            mettreAJourStatistiques(liste);
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement",
                    "Impossible de récupérer les maintenances : " + e.getMessage());
        }
    }


    private void remplirComboMachine(List<Maintenance> liste) {
        List<String> noms = liste.stream()
                .map(m -> getNomMachine(m.getIdM()))
                .distinct().sorted().collect(Collectors.toList());
        noms.add(0, "Toutes les machines");
        comboMachine.setItems(FXCollections.observableArrayList(noms));
        comboMachine.setValue("Toutes les machines");
    }

    private void remplirComboTypePanne(List<Maintenance> liste) {
        List<String> types = liste.stream()
                .map(Maintenance::getTypePanne)
                .filter(t -> t != null && !t.isBlank())
                .distinct().sorted().collect(Collectors.toList());
        types.add(0, "Tous les types");
        comboTypePanne.setItems(FXCollections.observableArrayList(types));
        comboTypePanne.setValue("Tous les types");
    }

    // ──────────────────────────────────────────────────────
    //  Listener dynamique (frappe au clavier)
    // ──────────────────────────────────────────────────────
    private void configurerRechercheDynamique() {
        champRecherche.textProperty().addListener((obs, o, n) -> appliquerFiltres());
    }

    // ══════════════════════════════════════════════════════
    //  FILTRAGE COMBINÉ  (texte + combobox)
    // ══════════════════════════════════════════════════════

    /** Appelé par le KeyReleased du TextField ET les onAction des ComboBox */
    @FXML
    private void appliquerFiltres() {
        String texte   = champRecherche.getText() == null ? "" : champRecherche.getText().toLowerCase().trim();
        String machine = comboMachine.getValue();
        String type    = comboTypePanne.getValue();

        List<Maintenance> res = toutesMaintenances.stream()
                .filter(m -> {
                    // ── Filtre texte libre ────────────────────────
                    if (!texte.isEmpty() && !correspond(m, texte)) return false;

                    // ── Filtre machine par NOM ────────────────────
                    if (machine != null && !machine.equals("Toutes les machines")) {
                        if (!getNomMachine(m.getIdM()).equals(machine)) return false;
                    }

                    // ── Filtre type de panne ──────────────────────
                    if (type != null && !type.equals("Tous les types")) {
                        if (!type.equals(m.getTypePanne())) return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        maintenancesFiltrees.setAll(res);
        mettreAJourStatistiques(res);
    }

    /** Vérifie si une maintenance correspond au texte libre */
    private boolean correspond(Maintenance m, String texte) {
        String nomMachine = getNomMachine(m.getIdM()).toLowerCase();
        String type       = m.getTypePanne()   != null ? m.getTypePanne().toLowerCase()   : "";
        String desc       = m.getDescription() != null ? m.getDescription().toLowerCase() : "";
        String date       = m.getDateMain()    != null ? m.getDateMain().toString()        : "";
        String cout       = String.valueOf(m.getCout());
        return nomMachine.contains(texte)
                || type.contains(texte)
                || desc.contains(texte)
                || date.contains(texte)
                || cout.contains(texte);
    }

    // ──────────────────────────────────────────────────────
    //  Boutons Effacer / Réinit / Actualiser
    // ──────────────────────────────────────────────────────

    @FXML
    private void effacerRecherche() {
        champRecherche.clear();
        appliquerFiltres();
    }

    @FXML
    private void reinitialiserFiltres() {
        champRecherche.clear();
        if (comboMachine.getItems()   != null && !comboMachine.getItems().isEmpty())
            comboMachine.setValue("Toutes les machines");
        if (comboTypePanne.getItems() != null && !comboTypePanne.getItems().isEmpty())
            comboTypePanne.setValue("Tous les types");
        maintenancesFiltrees.setAll(toutesMaintenances);
        tableMaintenances.setItems(maintenancesFiltrees);
        mettreAJourStatistiques(toutesMaintenances);
    }

    @FXML
    private void actualiser() {
        chargerNomsMachines();
        chargerDonnees();
    }

    // ══════════════════════════════════════════════════════
    //  TRIS
    // ══════════════════════════════════════════════════════

    @FXML
    private void trierPlusRecent() {
        maintenancesFiltrees.sort(Comparator.comparing(
                Maintenance::getDateMain, Comparator.nullsLast(Comparator.reverseOrder())));
        tableMaintenances.refresh();
    }

    @FXML
    private void trierPlusAncien() {
        maintenancesFiltrees.sort(Comparator.comparing(
                Maintenance::getDateMain, Comparator.nullsLast(Comparator.naturalOrder())));
        tableMaintenances.refresh();
    }

    @FXML
    private void trierCoutDesc() {
        maintenancesFiltrees.sort(Comparator.comparingDouble(Maintenance::getCout).reversed());
        tableMaintenances.refresh();
    }

    // ══════════════════════════════════════════════════════
    //  STATISTIQUES
    // ══════════════════════════════════════════════════════

    private void mettreAJourStatistiques(List<? extends Maintenance> liste) {
        int total = liste.size();
        lblTotal.setText(String.valueOf(total));
        lblFiltres.setText(String.valueOf(total));

        if (total == 0) {
            lblCoutTotal.setText("0,00 DT");
            lblCoutMoyen.setText("0,00 DT");
            lblMachineCouteuse.setText("—");
            lblTypeDominant.setText("—");
            return;
        }

        double coutTotal = liste.stream().mapToDouble(Maintenance::getCout).sum();
        lblCoutTotal.setText(String.format("%.2f DT", coutTotal));
        lblCoutMoyen.setText(String.format("%.2f DT", coutTotal / total));

        liste.stream()
                .collect(Collectors.groupingBy(
                        m -> getNomMachine(m.getIdM()),
                        Collectors.summingDouble(Maintenance::getCout)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> lblMachineCouteuse.setText(
                        e.getKey() + " (" + String.format("%.0f DT", e.getValue()) + ")"));

        liste.stream()
                .filter(m -> m.getTypePanne() != null && !m.getTypePanne().isBlank())
                .collect(Collectors.groupingBy(Maintenance::getTypePanne, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(e -> lblTypeDominant.setText(
                        e.getKey() + " (" + e.getValue() + "x)"));
    }

    // ══════════════════════════════════════════════════════
    //  NAVIGATION SIDEBAR
    // ══════════════════════════════════════════════════════


    private void naviguerVers(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) tableMaintenances.getScene().getWindow();
            // On récupère le Stage et la Scene ACTUELLE
            Scene scene = stage.getScene();

            // SOLUTION MIRACLE : On change la racine, pas la scène !
            scene.setRoot(root);

            // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
            stage.show();
        } catch (IOException e) {
            System.err.println("⚠ Impossible de charger : " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════
    //  UTILITAIRES
    // ══════════════════════════════════════════════════════

    private void afficherErreur(String titre, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titre);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // navigation Front Office Agricole
    private void chargerSidebarAvatar(Personne user) {
        if (user == null) return;

        // Nom et rôle
        if (userNameLabel != null)
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());

        // Clip circulaire appliqué en Java (radius=35, centre=35,35 pour fitWidth/Height=70)
        if (sidebarAvatarImageView != null) {
            Circle clip = new Circle(35, 35, 35);
            sidebarAvatarImageView.setClip(clip);
        }

        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) return;

        Thread thread = new Thread(() -> {
            try {
                Image image = new Image(photoUrl, 70, 70, false, true, true);
                Platform.runLater(() -> {
                    if (!image.isError()) {
                        sidebarAvatarImageView.setImage(image);
                        sidebarAvatarImageView.setVisible(true);
                        sidebarAvatarImageView.setManaged(true);
                        sidebarAvatarDefault.setVisible(false);
                        if (sidebarAvatarBg != null) sidebarAvatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) {
                System.err.println("⚠️ Avatar sidebar : " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }


    private void chargerPage(MouseEvent event, String fxmlPath, String titre) {
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
    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void handleMonProfil(MouseEvent event )    {navigateTo(event,"/UsersInterface/ProfilEmplye.fxml","MonProfil");  }

    // ✓ CORRECT
    @FXML
    private void handleMonAbonnement(MouseEvent event) {
        System.out.println("💳 Ouverture Mon Abonnement...");
        navigateTo(event,"/UsersInterface/MesAbonnements.fxml","Mes Abonnements");
    }





    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public void setCurrentUser(Personne user) {
        user = SessionManager.getCurrentUser() ;
        if (user != null) {
            System.out.println("✓ setCurrentUser appelé pour: " + user.getNom());

            if (userNameLabel != null)
                userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            else
                System.err.println("✗ userNameLabel est NULL !");

            if (welcomeNameLabel != null)
                welcomeNameLabel.setText(user.getPrenom() + " !");
            else
                System.err.println("✗ welcomeNameLabel est NULL !");

            if (userRoleLabel != null)
                userRoleLabel.setText("🌾 AGRICULTEUR");


        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL !");
        }
    }

    // Ajouter cette méthode handleAPropos()
    @FXML
    private void handleAPropos(MouseEvent event) {
        navigateTo(event,"/UsersInterface/ProfilAgricole.fxml","ddd");
    }
    private void navigateTo(MouseEvent event, String fxmlPath,String title) {
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

    /**
     * Transfère l'utilisateur courant au contrôleur cible via réflexion
     */
    private void transferUserToController(Object controller) {
        try {
            controller.getClass()
                    .getMethod("setCurrentUser", Personne.class)
                    .invoke(controller, currentUser);
            System.out.println("✓ Utilisateur transféré au contrôleur");
        } catch (NoSuchMethodException e) {
            System.out.println("ℹ Le contrôleur n'a pas de méthode setCurrentUser()");
        } catch (Exception e) {
            System.err.println("✗ Erreur lors du transfert utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gère les erreurs de navigation de manière appropriée
     */
    private void handleNavigationError(String fxmlPath, String title, IOException e) {
        e.printStackTrace();

        // Vérifier si c'est un fichier manquant ou une autre erreur
        if (e.getMessage() != null && e.getMessage().contains("Location is not set")) {
            showInfo("Module à venir",
                    "Le module \"" + title + "\" sera disponible prochainement.");
        } else if (fxmlPath.contains("MesTerrains") ||
                fxmlPath.contains("MesAnimaux") ||
                fxmlPath.contains("MesStocks") ||
                fxmlPath.contains("MonMateriel")) {
            // Modules pas encore implémentés
            showInfo("Fonctionnalité à venir",
                    "Cette fonctionnalité est en cours de développement.");
        } else {
            // Erreur réelle
            showError("Erreur de chargement\n\n" +
                    "Impossible de charger " + title + ".\n" +
                    "Détails: " + e.getMessage());
        }
    }
        @FXML
        private void handleDashboard(MouseEvent event ) {
           navigateTo(event,"/UsersInterface/AcceuilEmp.fxml","Acceuil");        }

        @FXML
        private void handleMesTaches(MouseEvent event) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/MesTaches.fxml"));
                Parent root = loader.load();

                MesTaches ctrl = loader.getController();
                ctrl.setCurrentUser(currentUser); // ← c'est ce qui manque !
                // On récupère le Stage et la Scene ACTUELLE
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = stage.getScene();

                // SOLUTION MIRACLE : On change la racine, pas la scène !
                scene.setRoot(root);

                // Plus besoin de gérer "etaitMaximise", la fenêtre ne bougera pas d'un pixel
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }    }

        @FXML
        private void handleMonProfil() {
            System.out.println("👤 Ouverture Mon Profil...");

            if (currentUser == null) {
                showError("Erreur", "Session expirée. Veuillez vous reconnecter.");
                return;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
                Parent root = loader.load();

                ProfilEmploye controller = loader.getController();
                if (controller != null) {
                    controller.setCurrentUser(currentUser);
                    System.out.println("✓ Utilisateur passé au profil");
                }

                Stage stage = new Stage();
                stage.setTitle("Mon Profil - Employé");
                stage.setScene(new Scene(root, 900, 700));
                stage.setResizable(true);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.centerOnScreen();
                stage.showAndWait();

                System.out.println("✓ Modal profil fermée");

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
            }
        }

        @FXML
        private void handleRapports() {
            System.out.println("📊 Ouverture Rapports...");
            showInfo("À venir", "Le module Rapports sera disponible prochainement.");
        }

        @FXML
        private void handleLogout(MouseEvent event) {
            System.out.println("🚪 Déconnexion Employé...");
            navigateTo(event,"/UsersInterface/login.fxml", "Login");
        }

        // ══════════════════════════════════════════════════════════════
        // Navigation Utility
        // ══════════════════════════════════════════════════════════════


        // ══════════════════════════════════════════════════════════════
        // UI Helpers
        // ══════════════════════════════════════════════════════════════

        private void showGestionSubmenu() {
            if (gestionSubmenu != null) {
                gestionSubmenu.setVisible(true);
                gestionSubmenu.setManaged(true);
            }
        }

        private void hideGestionSubmenu() {
            if (gestionSubmenu != null) {
                gestionSubmenu.setVisible(false);
                gestionSubmenu.setManaged(false);
            }
        }

        Stage getStage() {
            if (logoutBtn != null && logoutBtn.getScene() != null) {
                return (Stage) logoutBtn.getScene().getWindow();
            }
            if (dashboardBtn != null && dashboardBtn.getScene() != null) {
                return (Stage) dashboardBtn.getScene().getWindow();
            }
            if (userNameLabel != null && userNameLabel.getScene() != null) {
                return (Stage) userNameLabel.getScene().getWindow();
            }
            return null;
        }

        // ══════════════════════════════════════════════════════════════
        // Alert Helpers
        // ══════════════════════════════════════════════════════════════

        private void showError(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }

        private void showInfo(String title, String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }



        @FXML
        void ouvrirRotations(MouseEvent mouseEvent) {
            navigateTo(mouseEvent, "/TerrainsInterface/EmployeRotation.fxml", "EmployeRotation");
        }
@FXML
    public void handleEvenements(MouseEvent mouseEvent) {
        navigateTo(mouseEvent,"/G-Evenements/AfficherEvenementsEmp.fxml", "EvenementsAfficher");
    }
@FXML
    public void handleMateriel(MouseEvent mouseEvent) {
        navigateTo(mouseEvent, "/MaterielsInterface/MMaintenanceFront.fxml", "EmployeMaintenance");

    }
}


