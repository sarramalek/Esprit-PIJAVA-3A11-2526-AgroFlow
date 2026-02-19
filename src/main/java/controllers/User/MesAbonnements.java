package controllers.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User.Abonnements;
import models.User.offres;
import models.User.Personne;
import services.User.AbonnementService;
import services.User.OffresServicees;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Contrôleur pour la gestion des abonnements d'un utilisateur agricole
 */
public class MesAbonnements {

    // ══════════════════════════════════════════════════════════════
    // FXML Components - JAMAIS statiques !
    // ══════════════════════════════════════════════════════════════

    @FXML private AnchorPane rootPane;
    @FXML private Label userNameLabel;
    @FXML private Label userNameLabel1;
    @FXML private Label welcomeNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Hyperlink aproposLink;
    @FXML private Button logoutBtn;

    // Containers
    @FXML private VBox mesAbonnementsContainer;
    @FXML private VBox offresContainer;
    @FXML private Label noAbonnementLabel;

    // Dialog elements
    @FXML private StackPane dialogOverlay;
    @FXML private Label dialogTitleLabel;
    @FXML private Label dialogDescriptionLabel;
    @FXML private Label dialogPrixLabel;
    @FXML private Label dialogDureeLabel;
    @FXML private DatePicker dateDebutPicker;
    @FXML private Button souscrireButton;
    @FXML private VBox dialogContent;

    // Menu gestion (si présent)
    @FXML private VBox gestionSubmenu;
    @FXML private VBox gestionContainer;
    @FXML private Button gestionBtn;
    @FXML private VBox abonnementsContainer;

    // ══════════════════════════════════════════════════════════════
    // Instance Variables
    // ══════════════════════════════════════════════════════════════

    private AbonnementService abonnementService;
    private OffresServicees offreService;
    private Personne currentUser;
    private offres selectedOffre;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ══════════════════════════════════════════════════════════════
    // Initialization
    // ══════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        System.out.println("✓ MesAbonnements Controller initialisé");

        try {
            abonnementService = new AbonnementService();
            offreService = new OffresServicees();

            // Initialiser le DatePicker avec la date d'aujourd'hui
            if (dateDebutPicker != null) {
                dateDebutPicker.setValue(LocalDate.now());
            }

            // Initialiser le menu gestion si présent
            if (gestionSubmenu != null) {
                gestionSubmenu.setVisible(false);
                gestionSubmenu.setManaged(false);
            }

            if (gestionBtn != null && gestionContainer != null) {
                gestionBtn.setOnMouseEntered(e -> showGestionSubmenu());
                gestionContainer.setOnMouseEntered(e -> showGestionSubmenu());
                gestionContainer.setOnMouseExited(e -> hideGestionSubmenu());
            }

        } catch (Exception e) {
            System.err.println("✗ Erreur initialisation MesAbonnements");
            e.printStackTrace();
            showError("Erreur d'initialisation", "Impossible de charger le module Abonnements: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // User Management
    // ══════════════════════════════════════════════════════════════

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            System.out.println("✓ setCurrentUser appelé pour: " + user.getNom());

            // Mettre à jour tous les labels utilisateur
            updateUserLabels(user);

            // Charger les données
            loadMesAbonnements();
            loadOffresDisponibles();
        } else {
            System.err.println("✗ setCurrentUser appelé avec user NULL !");
        }
    }

    /**
     * Mettre à jour les labels utilisateur
     */
    private void updateUserLabels(Personne user) {
        String fullName = user.getPrenom() + " " + user.getNom();

        if (userNameLabel != null) {
            userNameLabel.setText("Utilisateur: " + fullName);
        }

        if (userNameLabel1 != null) {
            userNameLabel1.setText(fullName);
        }

        if (welcomeNameLabel != null) {
            welcomeNameLabel.setText(user.getPrenom() + " !");
        }

        if (userRoleLabel != null) {
            userRoleLabel.setText("🌾 AGRICULTEUR");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Load Data Methods
    // ══════════════════════════════════════════════════════════════

    /**
     * Charger les abonnements de l'utilisateur
     */
    private void loadMesAbonnements() {
        if (currentUser == null) {
            System.err.println("⚠️ Aucun utilisateur connecté");
            return;
        }

        if (mesAbonnementsContainer == null) {
            System.err.println("⚠️ mesAbonnementsContainer est NULL");
            return;
        }

        try {
            List<Abonnements> abonnements = abonnementService.getAbonnementsByUser(currentUser.getCin());

            mesAbonnementsContainer.getChildren().clear();

            if (abonnements.isEmpty()) {
                if (noAbonnementLabel != null) {
                    noAbonnementLabel.setVisible(true);
                    noAbonnementLabel.setManaged(true);
                }
            } else {
                if (noAbonnementLabel != null) {
                    noAbonnementLabel.setVisible(false);
                    noAbonnementLabel.setManaged(false);
                }

                for (Abonnements abonnement : abonnements) {
                    VBox abonnementCard = createAbonnementCard(abonnement);
                    mesAbonnementsContainer.getChildren().add(abonnementCard);
                }
            }

            System.out.println("✓ " + abonnements.size() + " abonnements chargés");

        } catch (SQLException e) {
            System.err.println("✗ Erreur chargement abonnements");
            e.printStackTrace();
            showError("Erreur", "Impossible de charger vos abonnements: " + e.getMessage());
        }
    }

    /**
     * Charger les offres disponibles
     */
    private void loadOffresDisponibles() {
        if (offresContainer == null) {
            System.err.println("⚠️ offresContainer est NULL");
            return;
        }

        try {
            List<offres> offres = offreService.recuperer();

            offresContainer.getChildren().clear();

            for (offres offre : offres) {
                VBox offreCard = createOffreCard(offre);
                offresContainer.getChildren().add(offreCard);
            }

            System.out.println("✓ " + offres.size() + " offres chargées");

        } catch (SQLException e) {
            System.err.println("✗ Erreur chargement offres");
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les offres: " + e.getMessage());
        }
    }

    /**
     * Rafraîchir les offres
     */
    @FXML
    private void onRefreshOffres() {
        System.out.println("🔄 Rafraîchissement des offres...");
        loadOffresDisponibles();
        loadMesAbonnements(); // Rafraîchir aussi les abonnements
    }

    // ══════════════════════════════════════════════════════════════
    // Card Creation Methods
    // ══════════════════════════════════════════════════════════════

    /**
     * Créer une carte pour un abonnement
     */
    private VBox createAbonnementCard(Abonnements abonnement) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f1f8e9; -fx-background-radius: 8; " +
                "-fx-padding: 15; -fx-border-color: #aed581; -fx-border-width: 2; " +
                "-fx-border-radius: 8;");

        try {
            offres offre = offreService.rechercherParId(abonnement.getId_offre());

            if (offre != null) {
                addAbonnementCardContent(card, abonnement, offre);
            } else {
                Label errorLabel = new Label("Offre non trouvée (ID: " + abonnement.getId_offre() + ")");
                errorLabel.setTextFill(Color.RED);
                card.getChildren().add(errorLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur de chargement");
            errorLabel.setTextFill(Color.RED);
            card.getChildren().add(errorLabel);
        }

        return card;
    }

    /**
     * Ajouter le contenu à une carte d'abonnement
     */
    private void addAbonnementCardContent(VBox card, Abonnements abonnement, offres offre) {
        // Nom de l'offre
        Label nomLabel = new Label(offre.getNom_offre());
        nomLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nomLabel.setTextFill(Color.web("#2e7d32"));

        // Description
        Label descLabel = new Label(offre.getDescription());
        descLabel.setWrapText(true);
        descLabel.setFont(Font.font(12));

        // Prix
        Label prixLabel = new Label("Prix: " + offre.getPrix() + " DT");
        prixLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        prixLabel.setTextFill(Color.web("#1b5e20"));

        // Dates
        HBox datesBox = new HBox(20);
        Label dateInscLabel = new Label("📅 Début: " + abonnement.getDate_inscription());
        Label dateExpLabel = new Label("⏰ Fin: " + abonnement.getDate_expiration());
        datesBox.getChildren().addAll(dateInscLabel, dateExpLabel);

        // Badge situation
        Label situationLabel = createSituationBadge(abonnement.getSituation());

        card.getChildren().addAll(nomLabel, descLabel, prixLabel, datesBox, situationLabel);

        // Ajouter jours restants si actif
        if ("actif".equalsIgnoreCase(abonnement.getSituation())) {
            addJoursRestants(card, abonnement.getDate_expiration());
        }

        // Bouton renouveler si expiré
        if ("expiré".equalsIgnoreCase(abonnement.getSituation())) {
            Button renouvelerBtn = new Button("🔄 Renouveler");
            renouvelerBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; " +
                    "-fx-cursor: hand; -fx-padding: 8 15; -fx-background-radius: 5;");
            renouvelerBtn.setOnAction(e -> showOffreDialog(offre));
            card.getChildren().add(renouvelerBtn);
        }
    }

    /**
     * Créer un badge de situation
     */
    private Label createSituationBadge(String situation) {
        Label badge = new Label(situation.toUpperCase());
        badge.setFont(Font.font("System", FontWeight.BOLD, 12));
        badge.setPadding(new Insets(5, 10, 5, 10));
        badge.setStyle("-fx-background-radius: 15;");

        if ("actif".equalsIgnoreCase(situation)) {
            badge.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-background-radius: 15;");
        } else if ("expiré".equalsIgnoreCase(situation)) {
            badge.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 15;");
        } else {
            badge.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-background-radius: 15;");
        }

        return badge;
    }

    /**
     * Ajouter l'affichage des jours restants
     */
    private void addJoursRestants(VBox card, String dateExpirationStr) {
        try {
            LocalDate dateExp = LocalDate.parse(dateExpirationStr, dateFormatter);
            long joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), dateExp);

            Label joursLabel = new Label("⏳ Jours restants: " + joursRestants);
            joursLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

            if (joursRestants < 7) {
                joursLabel.setTextFill(Color.web("#d32f2f"));
            } else {
                joursLabel.setTextFill(Color.web("#388e3c"));
            }

            card.getChildren().add(joursLabel);
        } catch (Exception e) {
            System.err.println("⚠️ Erreur calcul jours restants: " + e.getMessage());
        }
    }

    /**
     * Créer une carte pour une offre
     */
    private VBox createOffreCard(offres offre) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-padding: 20; -fx-border-color: #e0e0e0; -fx-border-width: 1; " +
                "-fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Nom de l'offre
        Label nomLabel = new Label(offre.getNom_offre());
        nomLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        nomLabel.setTextFill(Color.web("#2e7d32"));

        // Description
        Label descLabel = new Label(offre.getDescription());
        descLabel.setWrapText(true);
        descLabel.setFont(Font.font(13));
        descLabel.setTextFill(Color.web("#666"));
        descLabel.setMaxHeight(60);

        // Prix
        Label prixLabel = new Label(offre.getPrix() + " DT");
        prixLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        prixLabel.setTextFill(Color.web("#1b5e20"));

        // Durée
        Label dureeLabel = new Label("⏱️ Durée: " + offre.getDuree_offre());
        dureeLabel.setFont(Font.font(12));

        // Bouton souscrire
        Button souscrireBtn = new Button("📝 Souscrire à cette offre");
        souscrireBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; " +
                "-fx-cursor: hand; -fx-padding: 10 20; -fx-font-weight: bold; " +
                "-fx-background-radius: 8;");
        souscrireBtn.setMaxWidth(Double.MAX_VALUE);
        souscrireBtn.setOnAction(e -> showOffreDialog(offre));

        card.getChildren().addAll(nomLabel, descLabel, prixLabel, dureeLabel, souscrireBtn);

        return card;
    }

    // ══════════════════════════════════════════════════════════════
    // Dialog Management
    // ══════════════════════════════════════════════════════════════

    /**
     * Afficher le dialog de souscription
     */
    private void showOffreDialog(offres offre) {
        if (dialogOverlay == null) {
            System.err.println("⚠️ dialogOverlay est NULL");
            return;
        }

        this.selectedOffre = offre;

        dialogTitleLabel.setText(offre.getNom_offre());
        dialogDescriptionLabel.setText(offre.getDescription());
        dialogPrixLabel.setText(offre.getPrix() + " DT");
        dialogDureeLabel.setText(String.valueOf(offre.getDuree_offre()));
        dateDebutPicker.setValue(LocalDate.now());

        dialogOverlay.setVisible(true);
        dialogOverlay.setManaged(true);
    }

    /**
     * Fermer le dialog
     */
    @FXML
    private void onCloseDialog() {
        if (dialogOverlay != null) {
            dialogOverlay.setVisible(false);
            dialogOverlay.setManaged(false);
        }
        selectedOffre = null;
    }

    /**
     * Souscrire à une offre
     */
    @FXML
    private void onSouscrire() {
        if (currentUser == null || selectedOffre == null) {
            showError("Erreur", "Utilisateur ou offre non sélectionné");
            return;
        }

        LocalDate dateDebut = dateDebutPicker.getValue();
        if (dateDebut == null) {
            showError("Erreur", "Veuillez sélectionner une date de début");
            return;
        }

        try {
            // Calculer la date d'expiration
            LocalDate dateExpiration = calculateDateExpiration(dateDebut,
                    String.valueOf(selectedOffre.getDuree_offre()));

            // Créer l'abonnement
            Abonnements nouvelAbonnement = new Abonnements();
            nouvelAbonnement.setCin(currentUser.getCin());
            nouvelAbonnement.setId_offre(selectedOffre.getId_offres());
            nouvelAbonnement.setDate_inscription(dateDebut.format(dateFormatter));
            nouvelAbonnement.setDate_expiration(dateExpiration.format(dateFormatter));
            nouvelAbonnement.setSituation("actif");

            // Ajouter à la base de données
            abonnementService.ajouter(nouvelAbonnement);

            System.out.println("✓ Abonnement créé avec succès");

            // Fermer le dialog
            onCloseDialog();

            // Rafraîchir la liste
            loadMesAbonnements();

            // Message de succès
            showSuccess("Succès", "Vous êtes maintenant abonné à " + selectedOffre.getNom_offre() + " !");

        } catch (SQLException e) {
            System.err.println("✗ Erreur création abonnement");
            e.printStackTrace();
            showError("Erreur", "Impossible de créer l'abonnement: " + e.getMessage());
        }
    }

    /**
     * Calculer la date d'expiration
     */
    private LocalDate calculateDateExpiration(LocalDate dateDebut, String duree) {
        String[] parts = duree.toLowerCase().trim().split("\\s+");
        int nombre = 1;
        String unite = "mois";

        try {
            nombre = Integer.parseInt(parts[0]);
            if (parts.length > 1) {
                unite = parts[1];
            }
        } catch (NumberFormatException e) {
            System.err.println("⚠️ Format de durée invalide, utilisation par défaut: 1 mois");
        }

        if (unite.contains("an") || unite.contains("année")) {
            return dateDebut.plusYears(nombre);
        } else if (unite.contains("mois")) {
            return dateDebut.plusMonths(nombre);
        } else if (unite.contains("jour")) {
            return dateDebut.plusDays(nombre);
        } else {
            // Par défaut: mois
            return dateDebut.plusMonths(nombre);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Navigation Handlers
    // ══════════════════════════════════════════════════════════════

    @FXML
    private void handleDashboard() {
        System.out.println("📊 Retour au dashboard...");
        navigateTo("/UsersInterface/AcceuillAgr.fxml", "AgroFlow - Dashboard Agricole", 1200, 700);
    }

    @FXML
    private void handleMesTerrains() {
        navigateTo("/MesTerrains.fxml", "Mes Terrains", 1200, 700);
    }

    @FXML
    private void handleMesAnimaux() {
        navigateTo("/MesAnimaux.fxml", "Mes Animaux", 1200, 700);
    }

    @FXML
    private void handleMesStocks() {
        navigateTo("/MesStocks.fxml", "Mes Stocks", 1200, 700);
    }

    @FXML
    private void handleMonMateriel() {
        navigateTo("/MonMateriel.fxml", "Mon Matériel", 1200, 700);
    }

    @FXML
    private void handleMonAbonnement() {
        System.out.println("💳 Déjà sur la page Abonnements");
    }

    @FXML
    private void handleAPropos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilAgricole.fxml"));
            Parent root = loader.load();

            ProfilAgricole controller = loader.getController();
            if (controller != null && currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = new Stage();
            stage.setTitle("Mon Profil");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.centerOnScreen();
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le profil: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                    Parent root = loader.load();

                    Stage stage = getStage();
                    if (stage != null) {
                        stage.setScene(new Scene(root, 1500, 700));
                        stage.setTitle("AgroFlow - Connexion");
                        stage.setMaximized(true);

                        System.out.println("✓ Déconnexion réussie");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Erreur", "Impossible de se déconnecter: " + e.getMessage());
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // Navigation Utility
    // ══════════════════════════════════════════════════════════════

    /**
     * Navigation générique vers une autre vue
     */
    private void navigateTo(String fxmlPath, String title, int width, int height) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Transférer l'utilisateur
            Object controller = loader.getController();
            if (controller != null && currentUser != null) {
                try {
                    controller.getClass()
                            .getMethod("setCurrentUser", Personne.class)
                            .invoke(controller, currentUser);
                } catch (Exception e) {
                    System.err.println("⚠️ Le contrôleur ne supporte pas setCurrentUser()");
                }
            }

            Stage stage = getStage();
            if (stage != null) {
                stage.setScene(new Scene(root, width, height));
                stage.setTitle(title);
                stage.setMaximized(true);

            } else {
                showError("Erreur", "Impossible d'obtenir la fenêtre principale");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation",
                    "Impossible de charger " + title + ": " + e.getMessage());
        }
    }

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

    private Stage getStage() {
        if (rootPane != null && rootPane.getScene() != null) {
            return (Stage) rootPane.getScene().getWindow();
        }
        if (logoutBtn != null && logoutBtn.getScene() != null) {
            return (Stage) logoutBtn.getScene().getWindow();
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

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}