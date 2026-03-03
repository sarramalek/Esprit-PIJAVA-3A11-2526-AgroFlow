package controllers.Events;

import controllers.User.ProfilEmploye;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Events.CategorieEvenement;
import models.Events.Evenement;
import models.Events.Participation;
import models.User.Personne;
import services.Events.CategorieEvenementService;
import services.Events.EvenementService;
import services.Events.ParticipationService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MesEvenementsParticipations {

    // ══════════════════════════════════════════════════════════════
    // FXML Components
    // ══════════════════════════════════════════════════════════════

    @FXML private AnchorPane rootPane;
    @FXML private Label      userNameLabel, userNameLabel1, welcomeNameLabel, userRoleLabel;
    @FXML private Button     logoutBtn;

    @FXML private ImageView sidebarAvatarImageView;
    @FXML private Label     sidebarAvatarDefault;
    @FXML private Circle    sidebarAvatarBg;

    // Containers
    @FXML private VBox  evenementsContainer;
    @FXML private VBox  participationsContainer;
    @FXML private Label noEvenementLabel;
    @FXML private Label noParticipationLabel;

    // Recherche / filtre — Événements
    @FXML private TextField        searchEvenementField;
    @FXML private ComboBox<String> filterStatutEvenementCombo;
    @FXML private ComboBox<String> sortEvenementCombo;
    @FXML private Label            countEvenementLabel;

    // Recherche / filtre — Participations
    @FXML private TextField        searchParticipationField;
    @FXML private ComboBox<String> filterStatutParticipationCombo;
    @FXML private Label            countParticipationLabel;

    // Stats participations
    @FXML private Label totalPartLabel;
    @FXML private Label confirmePartLabel;
    @FXML private Label inscritPartLabel;
    @FXML private Label annulePartLabel;

    // Dialog inscription
    @FXML private StackPane        dialogOverlay;
    @FXML private Label            dialogTitreLabel, dialogLieuLabel, dialogDateLabel, dialogStatutLabel;
    @FXML private DatePicker       dialogDateInscription;
    @FXML private ComboBox<String> dialogPresenceCombo;

    // ══════════════════════════════════════════════════════════════
    // État interne
    // ══════════════════════════════════════════════════════════════

    private final EvenementService           evenementService   = new EvenementService();
    private final ParticipationService       participationService = new ParticipationService();
    private final CategorieEvenementService  categorieService   = new CategorieEvenementService();

    private Personne   currentUser;
    private Evenement  selectedEvenement;

    private List<Evenement>    allEvenements    = new ArrayList<>();
    private List<Participation> allParticipations = new ArrayList<>();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ══════════════════════════════════════════════════════════════
    // INITIALISATION
    // ══════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        this.currentUser = SessionManager.getCurrentUser();
        if (this.currentUser != null) {
            System.out.println("✓ MesEvenementsParticipations — user: " + currentUser.getNom()
                    + " | role: " + currentUser.getRole());
            updateUserLabels(currentUser);
        } else {
            System.err.println("✗ SessionManager.getCurrentUser() est NULL !");
        }

        chargerSidebarAvatar(currentUser);
        setupSearchAndFilters();

        if (dialogOverlay != null)       { dialogOverlay.setVisible(false); dialogOverlay.setManaged(false); }
        if (dialogPresenceCombo != null)   dialogPresenceCombo.getItems().addAll("Oui", "Non");
        if (dialogDateInscription != null) dialogDateInscription.setValue(LocalDate.now());

        if (currentUser != null) {
            loadEvenements();
            loadParticipations();
        }
    }

    // ══════════════════════════════════════════════════════════════
    // SETUP FILTRES
    // ══════════════════════════════════════════════════════════════

    private void setupSearchAndFilters() {
        if (filterStatutEvenementCombo != null) {
            filterStatutEvenementCombo.setItems(FXCollections.observableArrayList(
                    "Tous les statuts", "Planifie", "Annule", "Termine"));
            filterStatutEvenementCombo.setValue("Tous les statuts");
            filterStatutEvenementCombo.setOnAction(e -> applyEvenementFilters());
        }
        if (sortEvenementCombo != null) {
            sortEvenementCombo.setItems(FXCollections.observableArrayList(
                    "Date début ↓", "Date début ↑", "Titre A→Z", "Titre Z→A"));
            sortEvenementCombo.setValue("Date début ↓");
            sortEvenementCombo.setOnAction(e -> applyEvenementFilters());
        }
        if (searchEvenementField != null)
            searchEvenementField.textProperty().addListener((obs, o, n) -> applyEvenementFilters());

        if (filterStatutParticipationCombo != null) {
            filterStatutParticipationCombo.setItems(FXCollections.observableArrayList(
                    "Tous", "Inscrit", "Confirme", "Annule"));
            filterStatutParticipationCombo.setValue("Tous");
            filterStatutParticipationCombo.setOnAction(e -> applyParticipationFilters());
        }
        if (searchParticipationField != null)
            searchParticipationField.textProperty().addListener((obs, o, n) -> applyParticipationFilters());
    }

    // ══════════════════════════════════════════════════════════════
    // CHARGEMENT
    // ══════════════════════════════════════════════════════════════

    private void loadEvenements() {
        try {
            int userRole = currentUser.getRole();

            Set<Integer> allowedCategoryIds = categorieService.recuperer().stream()
                    .filter(cat -> {
                        String desc = cat.getDescription_categorie();
                        if (desc == null) return false;
                        String d = desc.toLowerCase();
                        return switch (userRole) {
                            case 1 -> d.contains("agricole");
                            case 2 -> d.contains("employe") || d.contains("employé");
                            default -> true;
                        };
                    })
                    .map(CategorieEvenement::getId_categorie)
                    .collect(Collectors.toSet());

            System.out.println("✓ Role: " + userRole + " | Allowed categories: " + allowedCategoryIds);

            allEvenements = evenementService.recuperer().stream()
                    .filter(ev -> allowedCategoryIds.contains(ev.getIdCategorie()))
                    .collect(Collectors.toList());

            applyEvenementFilters();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les événements : " + e.getMessage());
        }
    }

    private void loadParticipations() {
        try {
            int cin = currentUser.getCin();
            allParticipations = participationService.recuperer().stream()
                    .filter(p -> p.getId_user() == cin)
                    .collect(Collectors.toList());
            applyParticipationFilters();
            updateParticipationStats();
        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les participations : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // FILTRES & TRI
    // ══════════════════════════════════════════════════════════════

    private void applyEvenementFilters() {
        String search = searchEvenementField != null ? searchEvenementField.getText().toLowerCase().trim() : "";
        String statut = filterStatutEvenementCombo != null ? filterStatutEvenementCombo.getValue() : "Tous les statuts";
        String sort   = sortEvenementCombo != null ? sortEvenementCombo.getValue() : "Date début ↓";

        List<Evenement> result = allEvenements.stream()
                .filter(ev -> {
                    boolean ms = search.isEmpty()
                            || ev.getTitre().toLowerCase().contains(search)
                            || (ev.getLieu() != null && ev.getLieu().toLowerCase().contains(search));
                    boolean mf = statut == null || statut.equals("Tous les statuts")
                            || ev.getStatut().equalsIgnoreCase(statut);
                    return ms && mf;
                })
                .collect(Collectors.toList());

        Comparator<Evenement> cmp = switch (sort != null ? sort : "") {
            case "Date début ↑" -> Comparator.comparing(ev -> ev.getDateDebut().toLocalDate());
            case "Titre A→Z"    -> Comparator.comparing(ev -> ev.getTitre().toLowerCase());
            case "Titre Z→A"    -> Comparator.comparing((Evenement ev) -> ev.getTitre().toLowerCase()).reversed();
            default             -> Comparator.comparing((Evenement ev) -> ev.getDateDebut().toLocalDate()).reversed();
        };
        result.sort(cmp);
        renderEvenements(result);
    }

    private void applyParticipationFilters() {
        String search = searchParticipationField != null ? searchParticipationField.getText().toLowerCase().trim() : "";
        String statut = filterStatutParticipationCombo != null ? filterStatutParticipationCombo.getValue() : "Tous";

        List<Participation> result = allParticipations.stream()
                .filter(p -> {
                    boolean ms = search.isEmpty()
                            || (p.getStatut_participation() != null && p.getStatut_participation().toLowerCase().contains(search));
                    boolean mf = statut == null || statut.equals("Tous")
                            || p.getStatut_participation().equalsIgnoreCase(statut);
                    return ms && mf;
                })
                .collect(Collectors.toList());

        renderParticipations(result);
    }

    // ══════════════════════════════════════════════════════════════
    // RENDU — ÉVÉNEMENTS
    // ══════════════════════════════════════════════════════════════

    private void renderEvenements(List<Evenement> list) {
        if (evenementsContainer == null) return;
        evenementsContainer.getChildren().clear();
        if (countEvenementLabel != null) countEvenementLabel.setText(list.size() + " événement(s)");
        boolean empty = list.isEmpty();
        if (noEvenementLabel != null) { noEvenementLabel.setVisible(empty); noEvenementLabel.setManaged(empty); }
        list.forEach(ev -> evenementsContainer.getChildren().add(buildEvenementCard(ev)));
    }

    private VBox buildEvenementCard(Evenement ev) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 16;"
                + "-fx-border-color: #c8e6c9; -fx-border-width: 2; -fx-border-radius: 10;"
                + "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");

        // Title row
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titre = new Label("📅 " + ev.getTitre());
        titre.setFont(Font.font("System", FontWeight.BOLD, 16));
        titre.setTextFill(Color.web("#2e7d32"));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        titleRow.getChildren().addAll(titre, spacer, statutBadge(ev.getStatut()));

        // Info row
        HBox infoRow = new HBox(20);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        infoRow.getChildren().addAll(
                infoLabel("📍", ev.getLieu()),
                infoLabel("🗓", DATE_FMT.format(ev.getDateDebut().toLocalDate())
                        + " → " + DATE_FMT.format(ev.getDateFin().toLocalDate()))
        );

        // Action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        boolean dejaInscrit = allParticipations.stream()
                .anyMatch(p -> p.getId_evenement() == ev.getIdEvenement());
        String s = ev.getStatut() != null ? ev.getStatut().toLowerCase() : "";
        boolean inscriptible = !dejaInscrit
                && !s.equals("annule") && !s.equals("annulé")
                && !s.equals("termine") && !s.equals("terminé");

        if (dejaInscrit) {
            Label l = new Label("✅ Déjà inscrit");
            l.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
            actions.getChildren().add(l);
        } else if (inscriptible) {
            Button btn = new Button("📝 S'inscrire");
            btn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-cursor: hand;"
                    + "-fx-padding: 7 18; -fx-background-radius: 8; -fx-font-weight: bold;");
            btn.setOnAction(e -> showInscriptionDialog(ev));
            actions.getChildren().add(btn);
        } else {
            Label l = new Label("🚫 Inscription fermée");
            l.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
            actions.getChildren().add(l);
        }

        card.getChildren().addAll(titleRow, infoRow, actions);
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    // RENDU — PARTICIPATIONS
    // ══════════════════════════════════════════════════════════════

    private void renderParticipations(List<Participation> list) {
        if (participationsContainer == null) return;
        participationsContainer.getChildren().clear();
        if (countParticipationLabel != null) countParticipationLabel.setText(list.size() + " participation(s)");
        boolean empty = list.isEmpty();
        if (noParticipationLabel != null) { noParticipationLabel.setVisible(empty); noParticipationLabel.setManaged(empty); }
        list.forEach(p -> participationsContainer.getChildren().add(buildParticipationCard(p)));
    }

    private VBox buildParticipationCard(Participation p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f1f8e9; -fx-background-radius: 8; -fx-padding: 14;"
                + "-fx-border-color: #aed581; -fx-border-width: 2; -fx-border-radius: 8;");

        String evNom = "Événement #" + p.getId_evenement();
        try { evNom = evenementService.getNomEvenementById(p.getId_evenement()); }
        catch (SQLException ignored) {}

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label titre = new Label("🎟 " + evNom);
        titre.setFont(Font.font("System", FontWeight.BOLD, 15));
        titre.setTextFill(Color.web("#1b5e20"));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        titleRow.getChildren().addAll(titre, spacer, participationStatutBadge(p.getStatut_participation()));

        HBox infoRow = new HBox(20);
        infoRow.getChildren().addAll(
                infoLabel("📅", "Inscrit le : " + (p.getDate_inscription() != null
                        ? DATE_FMT.format(p.getDate_inscription()) : "—")),
                infoLabel(p.isPresence() ? "✅" : "❌", p.isPresence() ? "Présent" : "Absent")
        );

        boolean annulee = "annule".equalsIgnoreCase(p.getStatut_participation())
                || "annulé".equalsIgnoreCase(p.getStatut_participation());

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button modBtn = new Button("✏️ Modifier");
        modBtn.setStyle("-fx-background-color: #546e7a; -fx-text-fill: white; -fx-cursor: hand;"
                + "-fx-padding: 6 14; -fx-background-radius: 7;");
        modBtn.setDisable(annulee);
        modBtn.setOnAction(e -> ouvrirModification(p));

        Button annBtn = new Button("🗑 Annuler");
        annBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-cursor: hand;"
                + "-fx-padding: 6 14; -fx-background-radius: 7;");
        annBtn.setDisable(annulee);
        annBtn.setOnAction(e -> annulerParticipation(p));

        actions.getChildren().addAll(modBtn, annBtn);
        card.getChildren().addAll(titleRow, infoRow, actions);
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    // ACTIONS
    // ══════════════════════════════════════════════════════════════

    private void showInscriptionDialog(Evenement ev) {
        this.selectedEvenement = ev;
        if (dialogTitreLabel  != null) dialogTitreLabel.setText(ev.getTitre());
        if (dialogLieuLabel   != null) dialogLieuLabel.setText("📍 " + ev.getLieu());
        if (dialogDateLabel   != null) dialogDateLabel.setText("🗓 "
                + DATE_FMT.format(ev.getDateDebut().toLocalDate())
                + " → " + DATE_FMT.format(ev.getDateFin().toLocalDate()));
        if (dialogStatutLabel != null) dialogStatutLabel.setText(ev.getStatut());
        if (dialogDateInscription != null) dialogDateInscription.setValue(LocalDate.now());
        if (dialogPresenceCombo   != null) dialogPresenceCombo.setValue("Oui");
        if (dialogOverlay != null) { dialogOverlay.setVisible(true); dialogOverlay.setManaged(true); }
    }

    @FXML
    private void onCloseDialog() {
        if (dialogOverlay != null) { dialogOverlay.setVisible(false); dialogOverlay.setManaged(false); }
        selectedEvenement = null;
    }

    @FXML
    private void onSInscrire() {
        if (currentUser == null || selectedEvenement == null) return;
        if (dialogDateInscription == null || dialogDateInscription.getValue() == null) {
            showError("Erreur", "Sélectionnez une date d'inscription."); return;
        }
        Participation p = new Participation();
        p.setId_evenement(selectedEvenement.getIdEvenement());
        p.setId_user(currentUser.getCin());
        p.setDate_inscription(dialogDateInscription.getValue());
        p.setStatut_participation("Inscrit");
        p.setPresence(dialogPresenceCombo != null && "Oui".equals(dialogPresenceCombo.getValue()));

        try {
            participationService.ajouter(p);
            onCloseDialog();
            loadParticipations();
            loadEvenements();
            showSuccess("Succès", "Inscription confirmée pour \"" + selectedEvenement.getTitre() + "\" !");
        } catch (SQLException e) {
            showError("Erreur", "Impossible de s'inscrire : " + e.getMessage());
        }
    }

    private void annulerParticipation(Participation p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer"); confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment annuler cette participation ?");
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            try {
                p.setStatut_participation("Annule");
                participationService.modifier(p);
                loadParticipations();
                loadEvenements();
                showSuccess("Annulé", "Participation annulée.");
            } catch (SQLException e) { showError("Erreur", "Impossible d'annuler : " + e.getMessage()); }
        });
    }

    private void ouvrirModification(Participation p) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/G-Evenements/ModifierParticipationUser.fxml"));
            Parent root = loader.load();
            ModifierParticipationUserController ctrl = loader.getController();
            ctrl.setParticipation(p);
            Stage popup = new Stage();
            popup.initModality(Modality.WINDOW_MODAL);
            popup.initOwner(getStage());
            popup.setTitle("Modifier ma participation");
            popup.setScene(new Scene(root));
            popup.showAndWait();
            loadParticipations();
        } catch (IOException e) { showError("Erreur", e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════════════
    // STATS
    // ══════════════════════════════════════════════════════════════

    private void updateParticipationStats() {
        if (totalPartLabel == null) return;
        long total    = allParticipations.size();
        long confirme = allParticipations.stream().filter(p -> "confirme".equalsIgnoreCase(p.getStatut_participation()) || "confirmé".equalsIgnoreCase(p.getStatut_participation())).count();
        long inscrit  = allParticipations.stream().filter(p -> "inscrit".equalsIgnoreCase(p.getStatut_participation())).count();
        long annule   = allParticipations.stream().filter(p -> "annule".equalsIgnoreCase(p.getStatut_participation()) || "annulé".equalsIgnoreCase(p.getStatut_participation())).count();
        totalPartLabel.setText(String.valueOf(total));
        if (confirmePartLabel != null) confirmePartLabel.setText(String.valueOf(confirme));
        if (inscritPartLabel  != null) inscritPartLabel.setText(String.valueOf(inscrit));
        if (annulePartLabel   != null) annulePartLabel.setText(String.valueOf(annule));
    }

    @FXML private void onRefresh() { loadEvenements(); loadParticipations(); }

    // ══════════════════════════════════════════════════════════════
    // UI HELPERS
    // ══════════════════════════════════════════════════════════════

    private Label statutBadge(String statut) {
        if (statut == null) statut = "inconnu";
        Label b = new Label(statut.toUpperCase());
        b.setFont(Font.font("System", FontWeight.BOLD, 11));
        b.setPadding(new Insets(3, 10, 3, 10));
        String bg = switch (statut.toLowerCase()) {
            case "planifie", "planifié" -> "#4caf50";
            case "annule",  "annulé"   -> "#f44336";
            case "termine", "terminé"  -> "#1976d2";
            default -> "#ff9800";
        };
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-background-radius:15;");
        return b;
    }

    private Label participationStatutBadge(String statut) {
        if (statut == null) statut = "inconnu";
        Label b = new Label(statut.toUpperCase());
        b.setFont(Font.font("System", FontWeight.BOLD, 11));
        b.setPadding(new Insets(3, 10, 3, 10));
        String bg = switch (statut.toLowerCase()) {
            case "confirme", "confirmé" -> "#388e3c";
            case "inscrit"              -> "#f57f17";
            case "annule",  "annulé"   -> "#c62828";
            default -> "#ff9800";
        };
        b.setStyle("-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-background-radius:15;");
        return b;
    }

    private HBox infoLabel(String icon, String text) {
        Label l = new Label(icon + " " + text);
        l.setFont(Font.font(12)); l.setTextFill(Color.web("#555"));
        return new HBox(l);
    }

    // ══════════════════════════════════════════════════════════════
    // USER & AVATAR
    // ══════════════════════════════════════════════════════════════

    public void setCurrentUser(Personne user) {
        this.currentUser = user;
        if (user != null) {
            SessionManager.setCurrentUser(user);
            updateUserLabels(user);
            loadEvenements();
            loadParticipations();
        }
    }

    private void updateUserLabels(Personne user) {
        String full = user.getPrenom() + " " + user.getNom();
        if (userNameLabel    != null) userNameLabel.setText(full);
        if (userNameLabel1   != null) userNameLabel1.setText(full);
        if (welcomeNameLabel != null) welcomeNameLabel.setText(user.getPrenom() + " !");
        if (userRoleLabel    != null) userRoleLabel.setText(user.getRole() == 2 ? "👔 EMPLOYÉ" : "🌾 AGRICULTEUR");
    }

    private void chargerSidebarAvatar(Personne user) {
        if (user == null) return;
        if (userNameLabel != null) userNameLabel.setText(user.getPrenom() + " " + user.getNom());
        if (sidebarAvatarImageView != null) sidebarAvatarImageView.setClip(new Circle(35, 35, 35));
        String photoUrl = user.getPhotoUrl();
        if (photoUrl == null || photoUrl.isBlank()) return;
        Thread t = new Thread(() -> {
            try {
                Image img = new Image(photoUrl, 70, 70, false, true, true);
                Platform.runLater(() -> {
                    if (!img.isError()) {
                        sidebarAvatarImageView.setImage(img);
                        sidebarAvatarImageView.setVisible(true); sidebarAvatarImageView.setManaged(true);
                        sidebarAvatarDefault.setVisible(false);
                        if (sidebarAvatarBg != null) sidebarAvatarBg.setVisible(false);
                    }
                });
            } catch (Exception e) { System.err.println("⚠️ Avatar: " + e.getMessage()); }
        });
        t.setDaemon(true); t.start();
    }

    // ══════════════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════════════



    @FXML private void handleMonProfil(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/ProfilEmplye.fxml"));
            Parent root = loader.load();
            ProfilEmploye ctrl = loader.getController();
            if (ctrl != null && currentUser != null) ctrl.setCurrentUser(currentUser);
            Stage s = new Stage(); s.setTitle("Mon Profil"); s.setScene(new Scene(root));
            s.setResizable(true); s.initModality(Modality.APPLICATION_MODAL);
            s.centerOnScreen(); s.showAndWait();
        } catch (IOException e) { showError("Erreur", e.getMessage()); }
    }

    @FXML private void handleLogout() {
        new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment vous déconnecter ?")
                .showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
                    SessionManager.setCurrentUser(null);
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) logoutBtn.getScene().getWindow();
                        stage.getScene().setRoot(root); stage.show();
                    } catch (IOException e) { showError("Erreur", e.getMessage()); }
                });
    }

    private void nav(MouseEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            try {
                loader.getController().getClass().getMethod("setCurrentUser", Personne.class)
                        .invoke(loader.getController(), this.currentUser);
            } catch (NoSuchMethodException ignored) { } catch (Exception ex) { ex.printStackTrace(); }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root); stage.show();
        } catch (IOException e) { System.err.println("Erreur navigation: " + fxmlPath); }
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    private Stage getStage() {
        if (rootPane  != null && rootPane.getScene()  != null) return (Stage) rootPane.getScene().getWindow();
        if (logoutBtn != null && logoutBtn.getScene() != null) return (Stage) logoutBtn.getScene().getWindow();
        return null;
    }

    private void showError(String t, String m)   { Alert a = new Alert(Alert.AlertType.ERROR);       a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private void showSuccess(String t, String m) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    public void handleMateriel(MouseEvent mouseEvent) {
        nav(mouseEvent, "/MaterielsInterface/MaintenanceFront.fxml", "EmployeMaintenance");

    }

    public void ouvrirRotations(MouseEvent mouseEvent) {
        nav(mouseEvent,"/TerrainsInterface/EmployeRotation.fxml", "EmployeRotations");
    }

    public void handleDashboard(MouseEvent mouseEvent) {nav(mouseEvent,"/UsersInterface/AcceuilEmp.fxml", " Dashboard Employe");
    }

    public void handleMesTaches(MouseEvent mouseEvent) {
        nav(mouseEvent,"/UsersInterface/MesTaches.fxml", " Mes-Taches");
    }



}