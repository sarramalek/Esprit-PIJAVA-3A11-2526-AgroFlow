package controllers;

import javafx.scene.layout.VBox;
import models.Personne;
import services.PersonneService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class Authentification {
    //sub menu
    @FXML private VBox gestionSubmenu, operationsSubmenu,gestionContainer;
    @FXML private Button gestionToggle, operationsToggle;
    @FXML private Button gestionBtn;

    @FXML private ToggleButton adminToggle;
    @FXML private ToggleButton agricoleToggle;
    @FXML private ToggleButton employeToggle;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Hyperlink signupLink;
    @FXML private Label errorLabel;

    private ToggleGroup userTypeGroup;
    private String currentUserType = "Agricole";
    private PersonneService personneService;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        // Initialiser le service Personne
        try {
            personneService = new PersonneService();
            System.out.println("✓ PersonneService initialisé");
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'initialisation du PersonneService");
            e.printStackTrace();
            showError("Erreur de connexion à la base de données");
        }

        // Créer un ToggleGroup pour les boutons Admin/Agricole/Employé
        userTypeGroup = new ToggleGroup();
        adminToggle.setToggleGroup(userTypeGroup);
        agricoleToggle.setToggleGroup(userTypeGroup);
        if (employeToggle != null) {
            employeToggle.setToggleGroup(userTypeGroup);
        }

        // Par défaut, sélectionner Agricole
        agricoleToggle.setSelected(true);

        // Gérer le changement de sélection
        userTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == adminToggle) {
                currentUserType = "Admin";
                updateToggleStyles();
            } else if (newValue == agricoleToggle) {
                currentUserType = "Agricole";
                updateToggleStyles();
            } else if (employeToggle != null && newValue == employeToggle) {
                currentUserType = "Employé";
                updateToggleStyles();
            }
        });

        // Ajouter un effet hover sur le bouton de connexion
        loginButton.setOnMouseEntered(e ->
                loginButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;")
        );
        loginButton.setOnMouseExited(e ->
                loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;")
        );

        // Permettre la connexion avec la touche Entrée
        passwordField.setOnAction(event -> handleLogin(event));
    }

    /**
     * Mettre à jour les styles des boutons toggle
     */
    private void updateToggleStyles() {
        String selectedStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;";
        String unselectedStyle = "-fx-background-color: #dcedc8; -fx-text-fill: #666666; -fx-font-size: 14px; -fx-cursor: hand;";

        adminToggle.setStyle(currentUserType.equals("Admin") ? selectedStyle : unselectedStyle);
        agricoleToggle.setStyle(currentUserType.equals("Agricole") ? selectedStyle : unselectedStyle);
        if (employeToggle != null) {
            employeToggle.setStyle(currentUserType.equals("Employé") ? selectedStyle : unselectedStyle);
        }
    }

    /**
     * Gérer la connexion
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation des champs
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        // Validation du format email
        if (!isValidEmail(email)) {
            showError("Format d'email invalide");
            return;
        }

        // Désactiver le bouton pendant la connexion
        loginButton.setDisable(true);
        loginButton.setText("Connexion...");

        try {
            // Authentifier l'utilisateur
            Personne authenticatedUser = authenticateUser(email, password, currentUserType);

            if (authenticatedUser != null) {
                hideError();
                showSuccess("Connexion réussie !");

                // Afficher les informations de l'utilisateur connecté
                System.out.println("========================================");
                System.out.println("UTILISATEUR CONNECTÉ:");
                System.out.println("CIN: " + authenticatedUser.getCin());
                System.out.println("Nom: " + authenticatedUser.getNom());
                System.out.println("Prénom: " + authenticatedUser.getPrenom());
                System.out.println("Email: " + authenticatedUser.getEmail());
                System.out.println("Rôle: " + authenticatedUser.getRole());
                System.out.println("Type: " + authenticatedUser.getClass().getSimpleName());
                System.out.println("========================================");

                // NAVIGATION vers le dashboard approprié selon le rôle
                navigateToDashboard(authenticatedUser);

            } else {
                showError("Email, mot de passe ou type d'utilisateur incorrect");
            }
        } catch (SQLException e) {
            System.err.println("✗ Erreur SQL lors de l'authentification:");
            e.printStackTrace();
            showError("Erreur de connexion à la base de données");
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'authentification:");
            e.printStackTrace();
            showError("Une erreur est survenue");
        } finally {
            // Réactiver le bouton
            loginButton.setDisable(false);
            loginButton.setText("Se connecter");
        }
    }

    /**
     * Naviguer vers le dashboard approprié selon le rôle
     */
    /**
     * Naviguer vers le dashboard approprié selon le rôle
     */
    private void navigateToDashboard(Personne user) {
        try {
            String fxmlPath;
            String title;

            switch (user.getRole()) {
                case 3 -> {
                    fxmlPath = "/Acceuil.fxml";
                    title = "AgroFlow - Dashboard Admin";
                }
                case 2 -> {
                    fxmlPath = "/AcceuilEmp.fxml";
                    title = "AgroFlow - Dashboard Employé";
                }
                case 1 -> {
                    fxmlPath = "/AcceuillAgr.fxml";
                    title = "AgroFlow - Dashboard Agricole";
                }
                default -> {
                    fxmlPath = "/Acceuill.fxml";
                    title = "AgroFlow - Dashboard";
                }
            }

            System.out.println("🚀 Navigation vers: " + fxmlPath);

            // Vérifier que le fichier existe
            var resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("✗ FXML introuvable: " + fxmlPath);
                showError("Fichier " + fxmlPath + " introuvable");
                return;
            }

            System.out.println("✓ Fichier FXML trouvé");

            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            System.out.println("✓ FXML chargé");

            // CRITIQUE: Récupérer le contrôleur et passer l'utilisateur
            Object controller = loader.getController();

            if (controller == null) {
                System.err.println("✗ ERREUR CRITIQUE: controller est NULL après load() !");
                showError("Erreur de chargement du contrôleur");
                return;
            }

            System.out.println("✓ Contrôleur récupéré: " + controller.getClass().getSimpleName());

            // Passer l'utilisateur au contrôleur approprié
            System.out.println("📤 Transfert de l'utilisateur...");

            if (controller instanceof AcceuilEmploye) {
                System.out.println("  → Contrôleur: AcceuilEmploye");
                ((AcceuilEmploye) controller).setCurrentUser(user);
                System.out.println("✓ Utilisateur passé à AcceuilEmploye");

            } else if (controller instanceof AcceuilAgricole) {
                System.out.println("  → Contrôleur: AcceuilAgricole");
                ((AcceuilAgricole) controller).setCurrentUser(user);
                System.out.println("✓ Utilisateur passé à AcceuilAgricole");

            } else if (controller instanceof Acceuil) {
                System.out.println("  → Contrôleur: Acceuil (Admin)");
                ((Acceuil) controller).setCurrentUser(user);
                System.out.println("✓ Utilisateur passé à Acceuil");

            } else if (controller instanceof DashboardPersonnes) {
                System.out.println("  → Contrôleur: DashboardPersonnes");
                ((DashboardPersonnes) controller).setCurrentUser(user);
                System.out.println("✓ Utilisateur passé à DashboardPersonnes");

            } else {
                System.err.println("⚠️ Type de contrôleur inconnu: " + controller.getClass().getName());
                System.err.println("⚠️ L'utilisateur ne sera pas passé !");
            }

            // Changer de scène
            Stage stage = (Stage) loginButton.getScene().getWindow();
            if (stage == null) {
                System.err.println("✗ Stage est NULL !");
                return;
            }

            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle(title);
            stage.centerOnScreen();

            System.out.println("✓ Navigation réussie vers le dashboard");
            System.out.println("========================================\n");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de la navigation:");
            e.printStackTrace();
            showError("Impossible de charger le dashboard: " + e.getMessage());
        }
    }
    /**
     * Authentifier l'utilisateur en utilisant PersonneService
     */
    private Personne authenticateUser(String email, String password, String userType) throws SQLException {
        System.out.println("🔍 Tentative de connexion:");
        System.out.println("  Email: " + email);
        System.out.println("  Type demandé: " + userType);

        // Déterminer le rôle recherché
        int roleRecherche = 0;
        if (userType.equals("Admin")) {
            roleRecherche = 3; // Admin
        } else if (userType.equals("Agricole")) {
            roleRecherche = 1; // Agricole
        } else if (userType.equals("Employé")) {
            roleRecherche = 2; // Employé
        }

        System.out.println("  Rôle recherché: " + roleRecherche);

        // Récupérer toutes les personnes
        List<Personne> personnes = personneService.recuperer();

        // Chercher l'utilisateur avec l'email, le mot de passe et le rôle correspondants
        for (Personne personne : personnes) {
            if (personne.getEmail() != null &&
                    personne.getEmail().equalsIgnoreCase(email) &&
                    personne.getMdp() != null &&
                    personne.getMdp().equals(password) &&
                    personne.getRole() == roleRecherche) {

                System.out.println("✓ Utilisateur trouvé et authentifié!");
                return personne;
            }
        }

        System.out.println("✗ Aucun utilisateur trouvé avec ces identifiants");
        return null;
    }

    /**
     * Gérer le lien "Mot de passe oublié"
     */
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        System.out.println("Mot de passe oublié cliqué");

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Récupération de mot de passe");
        dialog.setHeaderText("Réinitialisation du mot de passe");
        dialog.setContentText("Entrez votre adresse email:");

        dialog.showAndWait().ifPresent(email -> {
            if (isValidEmail(email)) {
                try {
                    List<Personne> personnes = personneService.recuperer();
                    boolean emailExists = personnes.stream()
                            .anyMatch(p -> p.getEmail() != null && p.getEmail().equalsIgnoreCase(email));

                    if (emailExists) {
                        showInfo(Alert.AlertType.ERROR, "Erreur", "Un email de réinitialisation a été envoyé à: " + email);
                    } else {
                        showError("Aucun compte associé à cet email");
                    }
                } catch (SQLException e) {
                    showError("Erreur lors de la vérification de l'email");
                    e.printStackTrace();
                }
            } else {
                showError("Format d'email invalide");
            }
        });
    }

    /**
     * Valider le format de l'email
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Afficher un message d'erreur
     */
    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13px;");
        errorLabel.setVisible(true);
    }

    /**
     * Masquer le message d'erreur
     */
    private void hideError() {
        errorLabel.setVisible(false);
    }

    /**
     * Afficher un message de succès
     */
    private void showSuccess(String message) {
        errorLabel.setText("✓ " + message);
        errorLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px;");
        errorLabel.setVisible(true);
    }

    /**
     * Afficher un message d'information
     */
    private void showInfo(Alert.AlertType error, String erreur, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    
    public void handleSignup(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SignUp.fxml"));
            Parent root = loader.load();

            // Obtenir le stage depuis n'importe quel élément disponible
            Stage stage = (Stage) signupLink.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("AgroFlow - Inscription");
            stage.show();

        } catch (IOException e) {
            showInfo(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page: " + e.getMessage());
        }
    }
}