package controllers.Materiels;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import models.Materiels.Achat;
import models.Materiels.Machine;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.Materiels.AchatService;
import services.Materiels.MachineService;
import utils.MyDatabase;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AfficherAchatsController implements Initializable {
    @FXML private Button logoutBtn,gestionBtn;
    @FXML private VBox gestionSubmenu,gestionContainer;
    // ================= COMPOSANTS FXML =================
    @FXML private TableView<AchatViewModel> tableAchats;
    @FXML private TableColumn<AchatViewModel, String> colDateAchat;
    @FXML private TableColumn<AchatViewModel, Integer> colQuantite;
    @FXML private TableColumn<AchatViewModel, String> colMachine;
    @FXML private TableColumn<AchatViewModel, String> colClient;
    @FXML private TableColumn<AchatViewModel, Integer> colCin;
    @FXML private TableColumn<AchatViewModel, Void> colActions;

    // Champs pour recherche et filtres
    @FXML private TextField champRecherche;
    @FXML private ComboBox<String> comboMachine;
    @FXML private Label lblTotal;
    @FXML private Label lblQuantiteTotal;

    // ================= SERVICES =================
    private AchatService achatService;
    private MachineService machineService;
    private Connection connection;

    // ================= LISTES =================
    private ObservableList<AchatViewModel> achatsObservableList;
    private ObservableList<AchatViewModel> achatsFiltres;
    private List<Machine> machines;
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
        });}
    // ================= CLASSE INTERNE POUR LE TABLEAU =================
    public static class AchatViewModel {
        private final int idAchat;
        private final LocalDate dateAchat;
        private final int quantite;
        private final int idM;
        private final int cin;
        private final String machineInfo;
        private final String nomClient;

        public AchatViewModel(int idAchat, LocalDate dateAchat, int quantite,
                              int idM, int cin, String machineInfo, String nomClient) {
            this.idAchat = idAchat;
            this.dateAchat = dateAchat;
            this.quantite = quantite;
            this.idM = idM;
            this.cin = cin;
            this.machineInfo = machineInfo;
            this.nomClient = nomClient;
        }

        public int getIdAchat() { return idAchat; }
        public LocalDate getDateAchat() { return dateAchat; }
        public int getQuantite() { return quantite; }
        public int getIdM() { return idM; }
        public int getCin() { return cin; }
        public String getMachineInfo() { return machineInfo; }
        public String getNomClient() { return nomClient; }
    }

    // ================= CLASSE INTERNE POUR USER INFO =================
    private static class UserInfo {
        int cin;
        String nom;
        String prenom;

        UserInfo(int cin, String nom, String prenom) {
            this.cin = cin;
            this.nom = nom;
            this.prenom = prenom;
        }

        String getNomComplet() {
            return prenom + " " + nom;
        }

        @Override
        public String toString() {
            return "CIN: " + cin + " - " + prenom + " " + nom;
        }
    }

    // ================= INITIALISATION =================
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        achatService = new AchatService();
        machineService = new MachineService();
        connection = MyDatabase.getInstance().getConnection();

        achatsObservableList = FXCollections.observableArrayList();
        achatsFiltres = FXCollections.observableArrayList();

        configurerTableau();
        chargerDonnees();
        configurerRecherche();
        initialiserComboMachines();
    }

    // ================= CONFIGURATION TABLEAU =================
    private void configurerTableau() {
        colDateAchat.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDateAchat().toString()));
        colQuantite.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getQuantite()).asObject());
        colMachine.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMachineInfo()));
        colClient.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNomClient()));
        colCin.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getCin()).asObject());

        // ================= COLONNE ACTIONS =================
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnModifier = new Button("✏️");
            private final Button btnSupprimer = new Button("🗑️");
            private final HBox hBox = new HBox(8, btnModifier, btnSupprimer);

            {
                // Style bouton Modifier
                btnModifier.setStyle(
                        "-fx-background-color: #3498db; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 14px; " +
                                "-fx-padding: 5 12; " +
                                "-fx-cursor: hand; " +
                                "-fx-background-radius: 5;"
                );
                btnModifier.setOnMouseEntered(e ->
                        btnModifier.setStyle(btnModifier.getStyle() + "-fx-background-color: #2980b9;"));
                btnModifier.setOnMouseExited(e ->
                        btnModifier.setStyle(btnModifier.getStyle().replace("-fx-background-color: #2980b9;", "-fx-background-color: #3498db;")));

                // Style bouton Supprimer
                btnSupprimer.setStyle(
                        "-fx-background-color: #e74c3c; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 14px; " +
                                "-fx-padding: 5 12; " +
                                "-fx-cursor: hand; " +
                                "-fx-background-radius: 5;"
                );
                btnSupprimer.setOnMouseEntered(e ->
                        btnSupprimer.setStyle(btnSupprimer.getStyle() + "-fx-background-color: #c0392b;"));
                btnSupprimer.setOnMouseExited(e ->
                        btnSupprimer.setStyle(btnSupprimer.getStyle().replace("-fx-background-color: #c0392b;", "-fx-background-color: #e74c3c;")));

                hBox.setAlignment(Pos.CENTER);

                // Actions
                btnModifier.setOnAction(event -> {
                    AchatViewModel achat = getTableView().getItems().get(getIndex());
                    afficherDialogAchat(achat);
                });

                btnSupprimer.setOnAction(event -> {
                    AchatViewModel achat = getTableView().getItems().get(getIndex());
                    supprimerAchatDirect(achat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hBox);
            }
        });
    }

    // ================= CHARGER DONNÉES =================
    private void chargerDonnees() {
        try {
            machines = machineService.recuperer();
            chargerAchats();
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement",
                    "Impossible de charger les données: " + e.getMessage());
        }
    }

    // ================= CHARGER ACHATS AVEC JOINTURE SQL =================
    private void chargerAchats() {
        try {
            achatsObservableList.clear();

            String query = "SELECT a.idAchat, a.dateAchat, a.quantite, a.idM, a.cin, " +
                    "u.nom, u.prenom, m.marque, m.modele " +
                    "FROM achat a " +
                    "LEFT JOIN users u ON a.cin = u.cin " +
                    "LEFT JOIN machine m ON a.idM = m.idM " +
                    "ORDER BY a.dateAchat DESC";

            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery(query)) {

                while (rs.next()) {
                    int idAchat = rs.getInt("idAchat");
                    LocalDate dateAchat = rs.getDate("dateAchat").toLocalDate();
                    int quantite = rs.getInt("quantite");
                    int idM = rs.getInt("idM");
                    int cin = rs.getInt("cin");

                    String marque = rs.getString("marque");
                    String modele = rs.getString("modele");
                    String machineInfo = (marque != null && modele != null)
                            ? marque + " " + modele
                            : "Machine #" + idM;

                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    String nomClient = (nom != null && prenom != null)
                            ? prenom + " " + nom
                            : "Client inconnu";

                    achatsObservableList.add(new AchatViewModel(
                            idAchat, dateAchat, quantite, idM, cin,
                            machineInfo, nomClient
                    ));
                }
            }

            achatsFiltres.setAll(achatsObservableList);
            tableAchats.setItems(achatsFiltres);
            mettreAJourStatistiques();

        } catch (SQLException e) {
            afficherErreur("Erreur", "Impossible de charger les achats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ================= RÉCUPÉRER LISTE DES USERS =================
    private ObservableList<UserInfo> recupererListeUsers() {
        ObservableList<UserInfo> users = FXCollections.observableArrayList();
        String query = "SELECT cin, nom, prenom FROM users ORDER BY nom, prenom";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                users.add(new UserInfo(
                        rs.getInt("cin"),
                        rs.getString("nom"),
                        rs.getString("prenom")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération users: " + e.getMessage());
        }

        return users;
    }

    // ================= CONFIGURATION RECHERCHE =================
    private void configurerRecherche() {
        if (champRecherche != null) {
            champRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
                filtrerAchats();
            });
        }
    }

    // ================= INITIALISER COMBO MACHINES =================
    private void initialiserComboMachines() {
        if (comboMachine != null) {
            comboMachine.getItems().clear();
            comboMachine.getItems().add("Toutes les machines");

            for (Machine m : machines) {
                comboMachine.getItems().add(m.getMarque() + " " + m.getModele());
            }

            comboMachine.getSelectionModel().selectFirst();
        }
    }

    // ================= RECHERCHER =================
    @FXML
    private void rechercher() {
        filtrerAchats();
    }

    // ================= FILTRER =================
    @FXML
    private void filtrer() {
        filtrerAchats();
    }

    // ================= FILTRER ACHATS =================
    private void filtrerAchats() {
        String recherche = champRecherche != null ? champRecherche.getText().toLowerCase() : "";
        String machineSelectionnee = comboMachine != null && comboMachine.getValue() != null
                ? comboMachine.getValue() : "Toutes les machines";

        achatsFiltres.clear();

        for (AchatViewModel achat : achatsObservableList) {
            boolean matchRecherche = recherche.isEmpty() ||
                    achat.getMachineInfo().toLowerCase().contains(recherche) ||
                    achat.getNomClient().toLowerCase().contains(recherche) ||
                    String.valueOf(achat.getCin()).contains(recherche) ||
                    achat.getDateAchat().toString().contains(recherche);

            boolean matchMachine = machineSelectionnee.equals("Toutes les machines") ||
                    achat.getMachineInfo().equals(machineSelectionnee);

            if (matchRecherche && matchMachine) {
                achatsFiltres.add(achat);
            }
        }

        mettreAJourStatistiques();
    }

    // ================= ACTUALISER =================
    @FXML
    private void actualiser() {
        chargerDonnees();
        if (champRecherche != null) champRecherche.clear();
        if (comboMachine != null) comboMachine.getSelectionModel().selectFirst();
    }

    // ================= METTRE À JOUR STATISTIQUES =================
    private void mettreAJourStatistiques() {
        if (lblTotal != null) {
            lblTotal.setText(String.valueOf(achatsFiltres.size()));
        }

        if (lblQuantiteTotal != null) {
            int total = achatsFiltres.stream()
                    .mapToInt(AchatViewModel::getQuantite)
                    .sum();
            lblQuantiteTotal.setText(String.valueOf(total));
        }
    }

    // ================= AJOUTER ACHAT =================
    @FXML
    private void ajouterAchat(ActionEvent event) {
        afficherDialogAchat(null);
    }

    @FXML
    private void ouvrirAjout() {
        afficherDialogAchat(null);
    }

    // ================= SUPPRIMER ACHAT DIRECT =================
    private void supprimerAchatDirect(AchatViewModel achat) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("⚠️ Confirmation");
        confirmation.setHeaderText("Supprimer l'achat #" + achat.getIdAchat());
        confirmation.setContentText("Client: " + achat.getNomClient() + "\nMachine: " + achat.getMachineInfo() + "\n\nCette action est irréversible!");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                achatService.supprimer(achat.getIdAchat());
                afficherSucces("✅ Succès", "Achat supprimé avec succès!");
                chargerAchats();
            } catch (SQLException e) {
                afficherErreur("❌ Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    // ================= DIALOG ACHAT =================
    private void afficherDialogAchat(AchatViewModel achatExistant) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle(achatExistant == null ? "➕ Nouvel Achat" : "✏️ Modifier l'Achat");
        dialogStage.setResizable(false);

        VBox dialogVBox = new VBox(20);
        dialogVBox.setPadding(new Insets(30));
        dialogVBox.setStyle("-fx-background-color: white;");

        Label titreLabel = new Label(achatExistant == null ? "📝 Formulaire d'Achat" : "✏️ Modifier l'Achat #" + achatExistant.getIdAchat());
        titreLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(15);
        gridPane.setVgap(15);
        gridPane.setPadding(new Insets(20, 0, 0, 0));

        // Date Achat
        Label lblDate = new Label("📅 Date d'Achat *");
        lblDate.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        DatePicker dateAchatPicker = new DatePicker();
        dateAchatPicker.setPrefWidth(250);
        dateAchatPicker.setPromptText("Sélectionner la date");
        dateAchatPicker.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #cbd5e0; -fx-border-radius: 5;");

        // Quantité
        Label lblQuantite = new Label("📦 Quantité *");
        lblQuantite.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        TextField quantiteField = new TextField();
        quantiteField.setPrefWidth(250);
        quantiteField.setPromptText("Entrer la quantité");
        quantiteField.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #cbd5e0; -fx-border-radius: 5; -fx-padding: 8;");

        // Machine
        Label lblMachine = new Label("⚙️ Machine *");
        lblMachine.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        ComboBox<String> machineCombo = new ComboBox<>();
        machineCombo.setPrefWidth(250);
        machineCombo.setPromptText("Sélectionner une machine");
        machineCombo.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #cbd5e0; -fx-border-radius: 5;");

        for (Machine m : machines) {
            machineCombo.getItems().add("ID: " + m.getIdM() + " - " + m.getMarque() + " " + m.getModele());
        }

        // Client
        Label lblClient = new Label("👤 Client *");
        lblClient.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        ComboBox<UserInfo> clientCombo = new ComboBox<>();
        clientCombo.setPrefWidth(250);
        clientCombo.setPromptText("Sélectionner un client");
        clientCombo.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #cbd5e0; -fx-border-radius: 5;");

        ObservableList<UserInfo> users = recupererListeUsers();
        clientCombo.setItems(users);

        // Remplir si modification
        if (achatExistant != null) {
            dateAchatPicker.setValue(achatExistant.getDateAchat());
            quantiteField.setText(String.valueOf(achatExistant.getQuantite()));

            for (int i = 0; i < machineCombo.getItems().size(); i++) {
                if (machineCombo.getItems().get(i).startsWith("ID: " + achatExistant.getIdM())) {
                    machineCombo.getSelectionModel().select(i);
                    break;
                }
            }

            for (UserInfo user : users) {
                if (user.cin == achatExistant.getCin()) {
                    clientCombo.getSelectionModel().select(user);
                    break;
                }
            }
        }

        gridPane.add(lblDate, 0, 0);
        gridPane.add(dateAchatPicker, 1, 0);
        gridPane.add(lblQuantite, 0, 1);
        gridPane.add(quantiteField, 1, 1);
        gridPane.add(lblMachine, 0, 2);
        gridPane.add(machineCombo, 1, 2);
        gridPane.add(lblClient, 0, 3);
        gridPane.add(clientCombo, 1, 3);

        HBox boutonsBox = new HBox(15);
        boutonsBox.setAlignment(Pos.CENTER);
        boutonsBox.setPadding(new Insets(20, 0, 0, 0));

        Button btnValider = new Button(achatExistant == null ? "✅ Ajouter" : "✅ Modifier");
        btnValider.setPrefWidth(150);
        btnValider.setPrefHeight(40);
        btnValider.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");

        Button btnAnnuler = new Button("❌ Annuler");
        btnAnnuler.setPrefWidth(150);
        btnAnnuler.setPrefHeight(40);
        btnAnnuler.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");

        btnValider.setOnAction(e -> {
            if (validerFormulaire(dateAchatPicker, quantiteField, machineCombo, clientCombo)) {
                try {
                    Achat achat = new Achat();
                    if (achatExistant != null) {
                        achat.setIdAchat(achatExistant.getIdAchat());
                    }
                    achat.setDateAchat(dateAchatPicker.getValue());
                    achat.setQuantite(Integer.parseInt(quantiteField.getText().trim()));
                    achat.setIdM(extraireIdMachine(machineCombo.getValue()));
                    achat.setCin(clientCombo.getValue().cin);

                    if (achatExistant == null) {
                        achatService.ajouter(achat);
                        afficherSucces("✅ Succès", "Achat ajouté avec succès!");
                    } else {
                        achatService.modifier(achat);
                        afficherSucces("✅ Succès", "Achat modifié avec succès!");
                    }

                    chargerAchats();
                    dialogStage.close();

                } catch (SQLException ex) {
                    afficherErreur("❌ Erreur", "Opération impossible: " + ex.getMessage());
                }
            }
        });

        btnAnnuler.setOnAction(e -> dialogStage.close());

        boutonsBox.getChildren().addAll(btnValider, btnAnnuler);
        dialogVBox.getChildren().addAll(titreLabel, gridPane, boutonsBox);

        Scene dialogScene = new Scene(dialogVBox, 550, 450);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    // ================= VALIDATION FORMULAIRE =================
    private boolean validerFormulaire(DatePicker datePicker, TextField quantiteField,
                                      ComboBox<String> machineCombo, ComboBox<UserInfo> clientCombo) {
        if (datePicker.getValue() == null) {
            afficherAvertissement("❌ Champ requis", "Veuillez sélectionner une date.");
            return false;
        }

        LocalDate dateAchat = datePicker.getValue();
        if (dateAchat.isAfter(LocalDate.now())) {
            afficherAvertissement("❌ Date invalide", "La date ne peut pas être dans le futur.");
            return false;
        }

        if (dateAchat.isBefore(LocalDate.of(2000, 1, 1))) {
            afficherAvertissement("❌ Date invalide", "La date ne peut pas être avant l'an 2000.");
            return false;
        }

        if (quantiteField.getText().trim().isEmpty()) {
            afficherAvertissement("❌ Champ requis", "Veuillez entrer une quantité.");
            return false;
        }

        if (!quantiteField.getText().trim().matches("\\d+")) {
            afficherAvertissement("❌ Format invalide", "La quantité doit être un nombre entier.");
            return false;
        }

        int quantite = Integer.parseInt(quantiteField.getText().trim());
        if (quantite < 1 || quantite > 10000) {
            afficherAvertissement("❌ Valeur invalide", "La quantité doit être entre 1 et 10000.");
            return false;
        }

        if (machineCombo.getValue() == null) {
            afficherAvertissement("❌ Champ requis", "Veuillez sélectionner une machine.");
            return false;
        }

        if (clientCombo.getValue() == null) {
            afficherAvertissement("❌ Champ requis", "Veuillez sélectionner un client.");
            return false;
        }

        return true;
    }

    // ================= EXTRAIRE ID MACHINE =================
    private int extraireIdMachine(String texte) {
        String[] parts = texte.split(" - ")[0].split(": ");
        return Integer.parseInt(parts[1].trim());
    }

    // ================= NAVIGATION =================
    @FXML private void naviguerAnimaux( Event event) { naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml",event ); }
    @FXML private void naviguerMateriels(Event event) { naviguerVers("/MaterielsInterface/AccueilMateriel.fxml",event); }
    @FXML private void naviguerStocks(Event event) { naviguerVers("/StocksInterface/afficherarticle.fxml", event); }
    @FXML private void naviguerTerrains(Event event) { naviguerVers("/TerrainsInterface/acceuilterrain.fxml", event ); }
    @FXML private void naviguerEvenements(Event event) { naviguerVers("/EventsInterface/GestionEvenements.fxml" , event); }
    @FXML private void naviguerUsers(Event event ) { naviguerVers("UsersInterface/Acceuil.fxml", event); }

    // Retour vers Gestion Matériels
    @FXML
    private void retourAccueil(Event event) {
        naviguerVers("/MaterielsInterface/AccueilMateriel.fxml",event);
    }

    @FXML
    private void deconnexion(Event event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setContentText("Voulez-vous vous déconnecter?");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            naviguerVers("/UsersInterface/Login.fxml",event);
        }
    }

    private void naviguerVers(String fxmlPath,Event event ) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            boolean etaitMaximise = stage.isMaximized();  // ← SAUVEGARDER AVANT

            stage.setScene(new Scene(root));

            stage.setMaximized(etaitMaximise);  // ← RESTAURER APRÈS

            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement FXML : " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ================= ALERTES =================
    private void afficherSucces(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherErreur(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void afficherAvertissement(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    //naviguer vers les autres modules

    @FXML
    private void handlePersonnes(MouseEvent event )  {
        this.naviguerVers("/UsersInterface/DahboardPersonne.fxml",event);}


    @FXML private void handleTaches(MouseEvent event ) { /* Charger vue Tâches */
        this.naviguerVers("/UsersInterface/GestionTache.fxml",event);}



    @FXML
    private void handleAbonnements(MouseEvent event) { /* Charger vue Abonnements */
        this.naviguerVers("/UsersInterface/GestionAbonnements.fxml",event);}
    @FXML private void handleOffres(MouseEvent event) { /* Charger vue Offres */
        this.naviguerVers("/UsersInterface/GestionOffre.fxml",event);}


    private void showGestionSubmenu() {
        gestionSubmenu.setVisible(true);
        gestionSubmenu.setManaged(true);
    }

    private void hideGestionSubmenu() {
        gestionSubmenu.setVisible(false);
        gestionSubmenu.setManaged(false);
    }

    public void handleDashboard(MouseEvent actionEvent) {
        this.naviguerVers("/UsersInterface/Acceuil.fxml", actionEvent);

    }
    public void handleAnimals(MouseEvent mouseEvent) {
        this.naviguerVers("/AnimalsInterface/AfficherAnimaux.fxml",mouseEvent);

    }




    public void handleStocks(MouseEvent mouseEvent) {
        this.naviguerVers("/StocksInterface/afficherarticle.fxml",mouseEvent);
    }



    public void handleTerrains(MouseEvent mouseEvent) {
        this.naviguerVers("/TerrainsInterface/acceuilterrain.fxml",mouseEvent);
    }


    //
    public void handleEvents(MouseEvent mouseEvent) {
        this.naviguerVers("/G-Evenements/Accueil.fxml",mouseEvent);
    }


    public void handleMateriels(MouseEvent mouseEvent) {
        this.naviguerVers("/MaterielsInterface/AccueilMateriel.fxml",mouseEvent);
    }
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
                Scene scene = new Scene(root, 900, 600);
                stage.setScene(scene);
                stage.setTitle("AgroFlow - Connexion");
                stage.setMaximized(true);

                System.out.println("✓ Déconnexion réussie");

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible de retourner à la page de connexion");
            }
        }
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
    // ================= ALERT METHODS =================
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}