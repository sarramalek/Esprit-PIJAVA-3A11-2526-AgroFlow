package controllers.User;

import com.sun.javafx.charts.Legend;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User.Admin;
import models.User.Employe;
import models.User.Personne;
import models.User.Utilisateur;
import services.User.PersonneService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la page d'inscription (SignUp)
 * Adapté pour utiliser PersonneService
 */
public class SignUp implements Initializable {

    // ═══════════════════════════════════════════════════════
    // FXML ELEMENTS
    // ═══════════════════════════════════════════════════════

    // Toggle buttons pour les rôles
    @FXML private ToggleButton adminToggle;
    @FXML private ToggleButton agricoleToggle;
    @FXML private ToggleButton employeToggle;

    // Champs du formulaire
    @FXML private TextField cinField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField telField;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private TextField adresseField;
    @FXML private TextField villeField;

    // Boutons
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    // Label d'erreur
    @FXML private Label errorLabel;

    // Lien de connexion
    @FXML private Hyperlink signinLink;

    // ═══════════════════════════════════════════════════════
    // SERVICES
    // ═══════════════════════════════════════════════════════

    private PersonneService personneService;
    private ToggleGroup roleToggleGroup;
    private int selectedRole = 1; // 1=Utilisateur (Agricole), 2=Employé, 3=Admin
    @FXML
    private ComboBox<String> gouvernoratComboBox,villeComboBox;

    // ═══════════════════════════════════════════════════════
    // INITIALIZATION
    // ═══════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        personneService = new PersonneService();

        // Données gouvernorats → villes
        Map<String, List<String>> gouvernoratVillesMap = new LinkedHashMap<>();
        gouvernoratVillesMap.put("Ariana",      List.of("Ariana Ville", "La Soukra", "Raoued", "Kalâat el-Andalous", "Sidi Thabet", "Ettadhamen", "Mnihla"));
        gouvernoratVillesMap.put("Béja",        List.of("Béja", "Medjez el-Bab", "Testour", "Nefza", "Thibar", "Goubellat"));
        gouvernoratVillesMap.put("Ben Arous",   List.of("Ben Arous", "Radès", "Mégrine", "Hammam Lif", "Ezzahra", "Boumhal", "Fouchana", "Mohamedia"));
        gouvernoratVillesMap.put("Bizerte",     List.of("Bizerte", "Mateur", "Menzel Bourguiba", "Ras Jebel", "Sejnane", "Ghar El Melh"));
        gouvernoratVillesMap.put("Gabès",       List.of("Gabès", "El Hamma", "Mareth", "Matmata", "Nouvelle Matmata", "Metouia"));
        gouvernoratVillesMap.put("Gafsa",       List.of("Gafsa", "El Ksar", "Métlaoui", "Redeyef", "Moularès", "Om Laârayes"));
        gouvernoratVillesMap.put("Jendouba",    List.of("Jendouba", "Bou Salem", "Tabarka", "Aïn Draham", "Ghardimaou", "Fernana"));
        gouvernoratVillesMap.put("Kairouan",    List.of("Kairouan", "Sbikha", "El Alaa", "Haffouz", "Oueslatia", "Chebika"));
        gouvernoratVillesMap.put("Kasserine",   List.of("Kasserine", "Sbeitla", "Fériana", "Thala", "Hassi El Ferid", "Hidra"));
        gouvernoratVillesMap.put("Kébili",      List.of("Kébili", "Douz", "Souk Lahad", "Faouar"));
        gouvernoratVillesMap.put("Kef",         List.of("Le Kef", "Tajerouine", "Dahmani", "Sers", "Nebeur", "Kalaat Senan"));
        gouvernoratVillesMap.put("Mahdia",      List.of("Mahdia", "Ksour Essaf", "El Djem", "Chebba", "Bou Merdes", "Rejiche"));
        gouvernoratVillesMap.put("Manouba",     List.of("Manouba", "Den Den", "Douar Hicher", "Oued Ellil", "Tebourba", "El Battan"));
        gouvernoratVillesMap.put("Médenine",    List.of("Médenine", "Houmt Souk (Djerba)", "Midoun", "Zarzis", "Ben Gardane", "Beni Khedache"));
        gouvernoratVillesMap.put("Monastir",    List.of("Monastir", "Skanes", "Ksar Hellal", "Jemmal", "Moknine", "Téboulba", "Sayada"));
        gouvernoratVillesMap.put("Nabeul",      List.of("Nabeul", "Hammamet", "Kelibia", "Korba", "Menzel Temime", "Grombalia", "Soliman"));
        gouvernoratVillesMap.put("Sfax",        List.of("Sfax", "Sakiet Ezzit", "El Ain", "Thyna", "Agareb", "Jebeniana", "Mahres"));
        gouvernoratVillesMap.put("Sidi Bouzid", List.of("Sidi Bouzid", "Regueb", "Meknassy", "Jilma", "Bir El Hafey", "Souk Jedid"));
        gouvernoratVillesMap.put("Siliana",     List.of("Siliana", "Bou Arada", "Gaâfour", "Rohia", "Makthar", "Kesra"));
        gouvernoratVillesMap.put("Sousse",      List.of("Sousse", "Hammam Sousse", "Akouda", "Kalaa Kebira", "Msaken", "Enfidha", "Hergla"));
        gouvernoratVillesMap.put("Tataouine",   List.of("Tataouine", "Ghomrassen", "Remada", "Bir Lahmar", "Dehiba"));
        gouvernoratVillesMap.put("Tozeur",      List.of("Tozeur", "Nefta", "Hazoua", "Degache", "Tameghza"));
        gouvernoratVillesMap.put("Tunis",       List.of("Tunis Centre", "La Marsa", "Le Bardo", "Carthage", "Sidi Bou Saïd", "La Goulette", "Le Kram", "L'Ariana"));
        gouvernoratVillesMap.put("Zaghouan",    List.of("Zaghouan", "Bir Mcherga", "El Fahs", "Nadhour", "Zriba", "Saouaf"));

        // Remplir gouvernorats
        gouvernoratComboBox.setItems(FXCollections.observableArrayList(gouvernoratVillesMap.keySet()));

        // Ville désactivée par défaut
        villeComboBox.setDisable(true);
        villeComboBox.setPromptText("Choisir d'abord un gouvernorat");

        // Listener dynamique gouvernorat → villes
        gouvernoratComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            villeComboBox.getSelectionModel().clearSelection();
            villeComboBox.setValue(null);
            if (newVal != null) {
                villeComboBox.setItems(FXCollections.observableArrayList(gouvernoratVillesMap.get(newVal)));
                villeComboBox.setDisable(false);
                villeComboBox.setPromptText("Choisir une ville");
            } else {
                villeComboBox.setItems(FXCollections.emptyObservableList());
                villeComboBox.setDisable(true);
            }
        });

        setupRoleToggles();
        setupValidationListeners();
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    private void updateVilles(String gov) {
        ObservableList<String> villes = FXCollections.observableArrayList();
        switch (gov) {
            case "Tunis": villes.addAll("La Marsa", "Le Bardo", "Carthage", "Sidi Bou Said"); break;
            case "Sousse": villes.addAll("Hammem Sousse", "Akouda", "Kalaa Kebira", "Port El Kantaoui"); break;
            case "Sfax": villes.addAll("Sakiet Ezzit", "Thyna", "Agareb"); break;
            // Ajoutez les autres cas selon vos besoins
            default: villes.add("Autre...");
        }
        villeComboBox.setItems(villes);
    }
    /**
     * Configure les toggle buttons pour la sélection du rôle
     */
    private void setupRoleToggles() {
        roleToggleGroup = new ToggleGroup();

        adminToggle.setToggleGroup(roleToggleGroup);
        agricoleToggle.setToggleGroup(roleToggleGroup);
        employeToggle.setToggleGroup(roleToggleGroup);

        // Agricole (Utilisateur) sélectionné par défaut
        agricoleToggle.setSelected(true);
        selectedRole = 1; // Utilisateur

        // Gérer les changements de style
        roleToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                updateToggleStyles();

                // Mettre à jour le rôle sélectionné
                if (newToggle == adminToggle) {
                    selectedRole = 3; // Admin
                } else if (newToggle == agricoleToggle) {
                    selectedRole = 1; // Utilisateur (Agricole)
                } else if (newToggle == employeToggle) {
                    selectedRole = 2; // Employé
                }
            }
        });

        updateToggleStyles();
    }

    /**
     * Met à jour les styles des toggle buttons
     */
    private void updateToggleStyles() {
        // Style pour bouton non sélectionné
        String unselectedStyle = "-fx-background-color: transparent; " +
                "-fx-text-fill: #558B2F; " +
                "-fx-font-size: 13px; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: transparent;";

        // Style pour bouton sélectionné
        String selectedStyle = "-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: transparent; " +
                "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.45), 10, 0, 0, 2);";

        adminToggle.setStyle(adminToggle.isSelected() ? selectedStyle : unselectedStyle);
        agricoleToggle.setStyle(agricoleToggle.isSelected() ? selectedStyle : unselectedStyle);
        employeToggle.setStyle(employeToggle.isSelected() ? selectedStyle : unselectedStyle);
    }

    /**
     * Configure les listeners de validation en temps réel
     */
    private void setupValidationListeners() {
        // Validation CIN (8 chiffres)
        cinField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                cinField.setText(oldVal);
            }
            if (newVal.length() > 8) {
                cinField.setText(newVal.substring(0, 8));
            }
        });

        // Validation téléphone (nombre uniquement)
        telField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                telField.setText(oldVal);
            }
            if (newVal.length() > 15) {
                telField.setText(newVal.substring(0, 15));
            }
        });

        // Limiter la date de naissance (majeur uniquement)
        dateNaissancePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate maxDate = LocalDate.now().minusYears(18);
                setDisable(empty || date.isAfter(maxDate));
            }
        });
    }

    // ═══════════════════════════════════════════════════════
    // VALIDATION
    // ═══════════════════════════════════════════════════════

    /**
     * Valide tous les champs du formulaire
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // CIN
        if (cinField.getText() == null || cinField.getText().trim().isEmpty()) {
            errors.append("• Le CIN est obligatoire\n");
        } else if (cinField.getText().length() != 8) {
            errors.append("• Le CIN doit contenir exactement 8 chiffres\n");
        } else {
            try {
                int cin = Integer.parseInt(cinField.getText());
                if (personneService.rechercherParId(cin) != null) {
                    errors.append("• Ce CIN est déjà enregistré\n");
                }
            } catch (SQLException e) {
                errors.append("• Erreur lors de la vérification du CIN\n");
                e.printStackTrace();
            } catch (NumberFormatException e) {
                errors.append("• Le CIN doit être un nombre valide\n");
            }
        }

        // Nom
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errors.append("• Le nom est obligatoire\n");
        } else if (nomField.getText().length() < 2) {
            errors.append("• Le nom doit contenir au moins 2 caractères\n");
        }

        // Prénom
        if (prenomField.getText() == null || prenomField.getText().trim().isEmpty()) {
            errors.append("• Le prénom est obligatoire\n");
        } else if (prenomField.getText().length() < 2) {
            errors.append("• Le prénom doit contenir au moins 2 caractères\n");
        }

        // Email
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            errors.append("• L'email est obligatoire\n");
        } else if (!isValidEmail(emailField.getText())) {
            errors.append("• L'email n'est pas valide\n");
        }

        // Mot de passe
        if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
            errors.append("• Le mot de passe est obligatoire\n");
        } else if (passwordField.getText().length() < 6) {
            errors.append("• Le mot de passe doit contenir au moins 6 caractères\n");
        }

        // Téléphone
        if (telField.getText() == null || telField.getText().trim().isEmpty()) {
            errors.append("• Le téléphone est obligatoire\n");
        } else if (telField.getText().length() < 8) {
            errors.append("• Le numéro de téléphone doit contenir au moins 8 chiffres\n");
        }

        // Date de naissance (optionnelle mais si présente, doit être valide)
        if (dateNaissancePicker.getValue() != null) {
            LocalDate maxDate = LocalDate.now().minusYears(18);
            if (dateNaissancePicker.getValue().isAfter(maxDate)) {
                errors.append("• Vous devez avoir au moins 18 ans\n");
            }
        }

        if (errors.length() > 0) {
            showError(errors.toString());
            return false;
        }

        hideError();
        return true;
    }

    /**
     * Valide le format d'un email
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    // ═══════════════════════════════════════════════════════
    // EVENT HANDLERS
    // ═══════════════════════════════════════════════════════

    /**
     * Gère l'enregistrement de l'utilisateur
     */
    @FXML
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            // Créer l'objet Personne approprié selon le rôle
            Personne personne;
            switch (selectedRole) {
                case 3: // Admin
                    personne = new Admin();
                    break;
                case 2: // Employé
                    personne = new Employe();
                    break;
                case 1: // Utilisateur (Agricole)
                default:
                    personne = new Utilisateur();
                    break;
            }

            // Remplir les données
            personne.setCin(Integer.parseInt(cinField.getText().trim()));
            personne.setNom(nomField.getText().trim());
            personne.setPrenom(prenomField.getText().trim());
            personne.setEmail(emailField.getText().trim());
            personne.setMdp(passwordField.getText()); // TODO: Hash le password
            personne.setTel(telField.getText().trim());
            personne.setPhotoUrl("");


            // Date de naissance
            if (dateNaissancePicker.getValue() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                personne.setDate_naiss(dateNaissancePicker.getValue().format(formatter));
            } else {
                personne.setDate_naiss(null);
            }

            personne.setAdresse(gouvernoratComboBox.getValue() != null ? gouvernoratComboBox.getValue() : "");
            personne.setVille(villeComboBox.getValue() != null ? villeComboBox.getValue() : "");

            // Dates de création et dernier changement
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            personne.setDate_creationcpt(now.format(formatter));
            personne.setDate_dernierchg(now.format(formatter));

            // Enregistrer dans la base de données
            personneService.ajouter(personne);

            showSuccess("Inscription réussie ! Vous pouvez maintenant vous connecter.");

            // Attendre 2 secondes puis rediriger vers la page de connexion
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::redirectToLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors de l'inscription : " + e.getMessage());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            showError("Erreur : Le CIN doit être un nombre valide.");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur technique : " + e.getMessage());
        }
    }

    /**
     * Gère l'annulation
     */
    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer l'annulation");
        alert.setHeaderText("Annuler l'inscription ?");
        alert.setContentText("Toutes les informations saisies seront perdues.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                redirectToLogin();
            }
        });
    }

    /**
     * Gère le clic sur "Se connecter"
     */
    @FXML
    private void handleSignup() {
        redirectToLogin();
    }

    // ═══════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════

    /**
     * Redirige vers la page de connexion
     */
    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) saveButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Connexion");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Impossible de charger la page de connexion.");
        }
    }

    // ═══════════════════════════════════════════════════════
    // UI HELPERS
    // ═══════════════════════════════════════════════════════

    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #C62828; " +
                "-fx-font-size: 13px; " +
                "-fx-background-color: #FFEBEE; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 10 14;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Affiche un message de succès
     */
    private void showSuccess(String message) {
        errorLabel.setText("✓ " + message);
        errorLabel.setStyle("-fx-text-fill: #2E7D32; " +
                "-fx-font-size: 13px; " +
                "-fx-background-color: #E8F5E9; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 10 14;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Masque le message d'erreur
     */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Réinitialise le formulaire
     */
    private void clearForm() {
        cinField.clear();
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        passwordField.clear();
        telField.clear();
        dateNaissancePicker.setValue(null);
        adresseField.clear();
        villeField.clear();
        agricoleToggle.setSelected(true);
        selectedRole = 1;
        hideError();
    }
}