package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import models.Personne;
import models.Admin;
import models.Employe;
import models.Utilisateur;
import services.PersonneService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrôleur pour la page d'authentification
 * Compatible avec le système existant (cin, role int, PersonneService)
 */
public class Authentification {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Hyperlink signupLink;

    @FXML
    private Button loginButton;

    private PersonneService personneService;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    private void initialize() {
        personneService = new PersonneService();

        // Cacher le message d'erreur au démarrage
        hideError();

        // Listener pour effacer l'erreur lors de la saisie
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (errorLabel.isVisible()) {
                hideError();
            }
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (errorLabel.isVisible()) {
                hideError();
            }
        });
    }

    /**
     * Gère le clic sur le bouton de connexion
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        // Récupération des valeurs
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation basique
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        // Validation format email
        if (!isValidEmail(email)) {
            showError("Format d'email invalide");
            return;
        }

        // Désactiver le bouton pendant l'authentification
        loginButton.setDisable(true);

        try {
            // Authentification
            Personne personne = authenticate(email, password);

            if (personne != null) {
                // Authentification réussie
                hideError();

                // Sauvegarder la session utilisateur
                SessionManager.setCurrentUser(personne);

                // Afficher un message de bienvenue
                System.out.println("════════════════════════════════════════");
                System.out.println("✅ CONNEXION RÉUSSIE");
                System.out.println("   Utilisateur: " + personne.getPrenom() + " " + personne.getNom());
                System.out.println("   Email: " + personne.getEmail());
                System.out.println("   Rôle: " + getRoleName(personne.getRole()));
                System.out.println("════════════════════════════════════════");

                // Redirection selon le rôle
                redirectToDashboard(personne);

            } else {
                // Authentification échouée
                showError("Email ou mot de passe incorrect");
            }

        } catch (SQLException e) {
            showError("Erreur de connexion à la base de données");
            e.printStackTrace();

        } catch (Exception e) {
            showError("Erreur lors de la connexion. Veuillez réessayer.");
            e.printStackTrace();

        } finally {
            // Réactiver le bouton
            loginButton.setDisable(false);
        }
    }

    /**
     * Authentifie un utilisateur avec email et mot de passe
     * Le rôle est récupéré automatiquement depuis la table users
     */
    private Personne authenticate(String email, String password) throws SQLException {
        // Récupérer tous les utilisateurs
        List<Personne> personnes = personneService.recuperer();

        // Chercher l'utilisateur avec l'email et mot de passe correspondants
        for (Personne p : personnes) {
            if (p.getEmail() != null && p.getEmail().equalsIgnoreCase(email) && p.getMdp() != null && p.getMdp().equals(password)) {
                // Utilisateur trouvé
                return p;
            }
        }

        // Aucun utilisateur trouvé
        return null;
    }

    /**
     * Redirige vers le dashboard approprié selon le rôle de l'utilisateur
     */
    private void redirectToDashboard(Personne personne) {
        try {
            String fxmlPath;
            String title;

            // Déterminer quelle vue charger selon le rôle
            // role = 1 : Utilisateur (Agricole)
            // role = 2 : Employé
            // role = 3 : Admin
            int role = personne.getRole();

            switch (role) {
                case 3 -> { // Admin
                    fxmlPath = "/Acceuil.fxml";
                    title = "AgroFlow - Dashboard Admin";
                }
                case 2 -> { // Employé
                    fxmlPath = "/AcceuilEmp.fxml";
                    title = "AgroFlow - Dashboard Employé";
                }
                case 1 -> { // Utilisateur (Agricole)
                    fxmlPath = "/AcceuillAgr.fxml";
                    title = "AgroFlow - Dashboard Utilisateur";
                }
                default -> {
                    showError("Rôle utilisateur non reconnu");
                    return;
                }
            }

            System.out.println("\n========================================");
            System.out.println("🚀 Navigation vers: " + fxmlPath);
            System.out.println("👤 Utilisateur: " + personne.getNom() + " " + personne.getPrenom());
            System.out.println("📋 Rôle: " + personne.getRole());

            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            System.out.println("✓ FXML chargé");

            // Récupérer le contrôleur et transférer l'utilisateur
            Object controller = loader.getController();

            if (controller != null) {
                System.out.println("✓ Contrôleur: " + controller.getClass().getSimpleName());

                // Utiliser instanceof pour chaque type de contrôleur
                boolean userTransferred = false;

                if (controller instanceof AcceuilEmploye) {
                    ((AcceuilEmploye) controller).setCurrentUser(personne);
                    userTransferred = true;
                } else if (controller instanceof AcceuilAgricole) {
                    ((AcceuilAgricole) controller).setCurrentUser(personne);
                    userTransferred = true;
                } else if (controller instanceof Acceuil) {
                    ((Acceuil) controller).setCurrentUser(personne);
                    userTransferred = true;
                } else if (controller instanceof DashboardPersonnes) {
                    ((DashboardPersonnes) controller).setCurrentUser(personne);
                    userTransferred = true;
                }

                if (userTransferred) {
                    System.out.println("✓ Utilisateur transféré au contrôleur");
                } else {
                    System.err.println("⚠️ Type de contrôleur non géré: " + controller.getClass().getName());
                }
            } else {
                System.err.println("✗ Contrôleur est NULL !");
            }

            // Obtenir la scène actuelle et changer de scène
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

            System.out.println("✓ Navigation réussie vers le dashboard");
            System.out.println("========================================\n");

        } catch (IOException e) {
            System.err.println("✗ Erreur lors de la navigation:");
            e.printStackTrace();
            showError("Impossible de charger le dashboard: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("✗ Erreur inattendue lors de la redirection:");
            e.printStackTrace();
            showError("Erreur lors de la redirection");
        }
    }


    /**
     * Obtient le nom du rôle en texte
     */
    private String getRoleName(int role) {
        switch (role) {
            case 1: return "Utilisateur (Agricole)";
            case 2: return "Employé";
            case 3: return "Administrateur";
            default: return "Inconnu";
        }
    }

    /**
     * Valide le format de l'email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Cache le message d'erreur
     */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setText("");
    }

    /**
     * Gère le mot de passe oublié
     */
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ForgotPassword.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) forgotPasswordLink.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("AgroFlow - Mot de passe oublié");
            stage.show();

        } catch (Exception e) {
            System.err.println("❌ Impossible de charger la page de récupération");
            e.printStackTrace();
            showError("Fonctionnalité temporairement indisponible");
        }
    }

    /**
     * Gère la création de compte
     */
    @FXML
    private void handleSignup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SignUp.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) signupLink.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("AgroFlow - Créer un compte");
            stage.show();

        } catch (Exception e) {
            System.err.println("❌ Impossible de charger la page d'inscription");
            e.printStackTrace();
            showError("Fonctionnalité temporairement indisponible");
        }
    }
}