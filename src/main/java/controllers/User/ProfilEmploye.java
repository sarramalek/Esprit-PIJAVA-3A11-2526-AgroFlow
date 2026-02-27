package controllers.User;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.User.Personne;
import services.User.CloudinaryService;
import services.User.PersonneService;
import services.User.SmsService;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class ProfilEmploye {

    // ══════════════════════════════════════════════════════════════
    // FXML — Photo
    // ══════════════════════════════════════════════════════════════
    @FXML private ImageView profileImageView;
    @FXML private Label     defaultAvatarLabel;
    @FXML private Label     photoStatusLabel;
    @FXML private Circle    photoCircleBg;

    // ══════════════════════════════════════════════════════════════
    // FXML — Formulaire
    // ══════════════════════════════════════════════════════════════
    @FXML private TextField     cinField;
    @FXML private TextField     nomField;
    @FXML private TextField     prenomField;
    @FXML private TextField     emailField;
    @FXML private TextField     telField;
    @FXML private TextField     adresseField;
    @FXML private TextField     villeField;
    @FXML private DatePicker    dateNaissField;
    @FXML private PasswordField ancienMdpField;
    @FXML private PasswordField nouveauMdpField;
    @FXML private PasswordField confirmMdpField;
    @FXML private Button        saveBtn;
    @FXML private Button        desactiverBtn;
    @FXML private Button        cancelBtn;
    @FXML private Label         roleLabel;

    // ══════════════════════════════════════════════════════════════
    // Instance Variables
    // ══════════════════════════════════════════════════════════════
    private File              selectedPhotoFile = null;
    private Personne          currentUser;
    private PersonneService   personneService;
    private CloudinaryService cloudinaryService;
    private AcceuilEmploye    parentController;

    // ══════════════════════════════════════════════════════════════
    // Initialization
    // ══════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        try {
            personneService   = new PersonneService();
            cloudinaryService = new CloudinaryService();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cinField != null) {
            cinField.setEditable(false);
            cinField.setStyle("-fx-background-color: #F0F0F0;");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Setters
    // ══════════════════════════════════════════════════════════════
    public void setCurrentUser(Personne user) {
        if (user == null) return;
        this.currentUser = user;
        loadUserData();
    }

    public void setParentController(AcceuilEmploye parent) {
        this.parentController = parent;
    }

    // ══════════════════════════════════════════════════════════════
    // Load Data
    // ══════════════════════════════════════════════════════════════
    private void loadUserData() {
        if (currentUser == null) return;

        if (cinField     != null) cinField.setText(String.valueOf(currentUser.getCin()));
        if (nomField     != null) nomField.setText(currentUser.getNom());
        if (prenomField  != null) prenomField.setText(currentUser.getPrenom());
        if (emailField   != null) emailField.setText(currentUser.getEmail());
        if (telField     != null) telField.setText(currentUser.getTel());
        if (adresseField != null) adresseField.setText(currentUser.getAdresse());
        if (villeField   != null) villeField.setText(currentUser.getVille());
        if (roleLabel    != null) roleLabel.setText("👷 EMPLOYÉ");

        if (dateNaissField != null && currentUser.getDate_naiss() != null) {
            try {
                String[] p = currentUser.getDate_naiss().split("-");
                if (p.length == 3)
                    dateNaissField.setValue(java.time.LocalDate.of(
                            Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2])));
            } catch (Exception ignored) {}
        }

        // ── Charger la photo depuis l'URL Cloudinary ──────────────────────────
        chargerPhotoDepuisUrl(currentUser.getPhotoUrl());
    }

    // ══════════════════════════════════════════════════════════════
    // Photo — Chargement depuis URL (thread background)
    // ══════════════════════════════════════════════════════════════

    /**
     * Télécharge et affiche la photo depuis l'URL Cloudinary dans un thread
     * séparé pour ne pas bloquer l'interface.
     */
    private void chargerPhotoDepuisUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            afficherAvatarParDefaut();
            return;
        }

        if (photoStatusLabel != null) photoStatusLabel.setText("⏳ Chargement de la photo...");

        Thread thread = new Thread(() -> {
            try {
                // Image JavaFX accepte directement les URLs https://
                Image image = new Image(photoUrl, 100, 100, false, true, true);

                Platform.runLater(() -> {
                    if (image.isError()) {
                        afficherAvatarParDefaut();
                        if (photoStatusLabel != null) photoStatusLabel.setText("⚠️ Photo indisponible");
                    } else {
                        profileImageView.setImage(image);
                        profileImageView.setVisible(true);
                        profileImageView.setManaged(true);
                        defaultAvatarLabel.setVisible(false);
                        if (photoCircleBg    != null) photoCircleBg.setVisible(false);
                        if (photoStatusLabel != null) photoStatusLabel.setText("✓ Photo chargée");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    afficherAvatarParDefaut();
                    if (photoStatusLabel != null) photoStatusLabel.setText("⚠️ Erreur réseau");
                });
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void afficherAvatarParDefaut() {
        profileImageView.setVisible(false);
        profileImageView.setManaged(false);
        defaultAvatarLabel.setVisible(true);
        if (photoCircleBg    != null) photoCircleBg.setVisible(true);
        if (photoStatusLabel != null) photoStatusLabel.setText("Aucune photo de profil");
    }

    // ══════════════════════════════════════════════════════════════
    // Photo — Sélection locale
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void handleUploadPhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une photo de profil");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"));

        File file = fc.showOpenDialog(profileImageView.getScene().getWindow());
        if (file == null) return;

        if (file.length() > 2 * 1024 * 1024) {
            alert(Alert.AlertType.WARNING, "Fichier trop volumineux", "La photo ne doit pas dépasser 2 Mo.");
            return;
        }

        selectedPhotoFile = file;

        // Aperçu local immédiat sans attendre l'upload Cloudinary
        Image preview = new Image(file.toURI().toString(), 100, 100, false, true);
        profileImageView.setImage(preview);
        profileImageView.setVisible(true);
        profileImageView.setManaged(true);
        defaultAvatarLabel.setVisible(false);
        if (photoCircleBg    != null) photoCircleBg.setVisible(false);
        if (photoStatusLabel != null) photoStatusLabel.setText("📎 " + file.getName() + " (cliquez Enregistrer pour sauvegarder)");
    }

    @FXML
    private void handleRemovePhoto() {
        selectedPhotoFile = null;
        if (currentUser != null) currentUser.setPhotoUrl(null);
        afficherAvatarParDefaut();
    }

    // ══════════════════════════════════════════════════════════════
    // Photo — Upload Cloudinary (thread background)
    // ══════════════════════════════════════════════════════════════

    /**
     * Upload le fichier sélectionné vers Cloudinary, met à jour currentUser.photoUrl,
     * puis appelle onSuccess sur le thread UI.
     */
    private void uploadVersCloudinaire(File file, Runnable onSuccess) {
        if (photoStatusLabel != null) photoStatusLabel.setText("⬆️ Upload en cours...");
        if (saveBtn != null) saveBtn.setDisable(true);

        Thread thread = new Thread(() -> {
            String publicId = "employe_" + currentUser.getCin();
            String url = cloudinaryService.uploadImage(file, publicId);

            Platform.runLater(() -> {
                if (url != null) {
                    currentUser.setPhotoUrl(url);
                    if (photoStatusLabel != null) photoStatusLabel.setText("✓ Photo uploadée");
                    if (onSuccess != null) onSuccess.run();
                } else {
                    if (saveBtn != null) saveBtn.setDisable(false);
                    alert(Alert.AlertType.ERROR, "Échec upload",
                            "Impossible d'uploader la photo vers Cloudinary.\n" +
                                    "Vérifiez vos identifiants dans CloudinaryService.java et votre connexion internet.");
                    if (photoStatusLabel != null) photoStatusLabel.setText("✗ Upload échoué");
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ══════════════════════════════════════════════════════════════
    // Save
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void handleSave() {
        if (currentUser == null) { alert(Alert.AlertType.ERROR, "Erreur", "Session expirée."); return; }
        if (!validateFields()) return;

        if (saveBtn != null) saveBtn.setDisable(true);

        if (selectedPhotoFile != null) {
            // 1. Upload Cloudinary → 2. Save BDD
            uploadVersCloudinaire(selectedPhotoFile, () -> {
                selectedPhotoFile = null;
                sauvegarderProfil();
            });
        } else {
            // Pas de nouvelle photo → Save BDD directement
            sauvegarderProfil();
        }
    }

    private void sauvegarderProfil() {
        try {
            currentUser.setNom(nomField.getText().trim());
            currentUser.setPrenom(prenomField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setTel(telField.getText().trim());
            currentUser.setAdresse(adresseField.getText().trim());
            currentUser.setVille(villeField.getText().trim());

            if (dateNaissField != null && dateNaissField.getValue() != null)
                currentUser.setDate_naiss(dateNaissField.getValue().toString());

            if (nouveauMdpField != null && !nouveauMdpField.getText().isEmpty())
                if (!handlePasswordChange()) { if (saveBtn != null) saveBtn.setDisable(false); return; }

            // photo_url est déjà dans currentUser (mis à jour par uploadVersCloudinaire)
            personneService.modifier(currentUser);

            alert(Alert.AlertType.INFORMATION, "Succès", "Profil mis à jour avec succès !");
            handleFermer();

        } catch (SQLException e) {
            e.printStackTrace();
            alert(Alert.AlertType.ERROR, "Erreur BDD", "Impossible de sauvegarder : " + e.getMessage());
        } finally {
            if (saveBtn != null) saveBtn.setDisable(false);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Validation
    // ══════════════════════════════════════════════════════════════
    private boolean validateFields() {
        if (nomField.getText().trim().isEmpty()) {
            alert(Alert.AlertType.WARNING, "Validation", "Le nom est obligatoire."); return false; }
        if (prenomField.getText().trim().isEmpty()) {
            alert(Alert.AlertType.WARNING, "Validation", "Le prénom est obligatoire."); return false; }
        if (emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
            alert(Alert.AlertType.WARNING, "Validation", "Email invalide."); return false; }
        if (nouveauMdpField != null && !nouveauMdpField.getText().isEmpty()) {
            if (ancienMdpField.getText().isEmpty()) {
                alert(Alert.AlertType.WARNING, "Validation", "Saisissez l'ancien mot de passe."); return false; }
            if (!nouveauMdpField.getText().equals(confirmMdpField.getText())) {
                alert(Alert.AlertType.WARNING, "Validation", "Les mots de passe ne correspondent pas."); return false; }
            if (nouveauMdpField.getText().length() < 6) {
                alert(Alert.AlertType.WARNING, "Validation", "Mot de passe trop court (6 car. min)."); return false; }
        }
        return true;
    }

    private boolean handlePasswordChange() {
        if (!currentUser.getMdp().equals(ancienMdpField.getText())) {
            alert(Alert.AlertType.ERROR, "Erreur", "Ancien mot de passe incorrect."); return false; }
        currentUser.setMdp(nouveauMdpField.getText());
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    // Fermer
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void handleFermer() {
        Stage stage = null;
        if (cancelBtn != null && cancelBtn.getScene() != null)
            stage = (Stage) cancelBtn.getScene().getWindow();
        else if (saveBtn != null && saveBtn.getScene() != null)
            stage = (Stage) saveBtn.getScene().getWindow();
        if (stage != null) stage.close();
    }

    // ══════════════════════════════════════════════════════════════
    // Désactiver compte
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void handleDesactiver() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("⚠️ Désactiver votre compte");
        confirm.setContentText("Cette action est irréversible.\nVotre compte sera supprimé définitivement.\n\nÊtes-vous sûr ?");
        ButtonType btnOui     = new ButtonType("Oui, supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnuler = new ButtonType("Annuler",         ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnOui, btnAnnuler);
        confirm.showAndWait().ifPresent(r -> {
            if (r == btnOui) {
                try {
                    personneService.supprimer(currentUser.getCin());
                    Stage stage = (Stage) desactiverBtn.getScene().getWindow();
                    stage.close();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/UsersInterface/login.fxml"));
                    Parent root = loader.load();
                    Stage loginStage = new Stage();
                    loginStage.setScene(new Scene(root, 900, 600));
                    loginStage.setTitle("AgroFlow - Connexion");
                    loginStage.show();
                    if (parentController != null) {
                        Stage dash = parentController.getStage();
                        if (dash != null) dash.close();
                    }
                } catch (SQLException | IOException e) {
                    e.printStackTrace();
                    alert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le compte.");
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // 2FA
    // ══════════════════════════════════════════════════════════════
    @FXML
    private void handleNavigateToSettings2FA(MouseEvent event) {
        if (currentUser == null) { alert(Alert.AlertType.ERROR, "Erreur", "Session expirée."); return; }
        String telephone = currentUser.getTel();
        if (telephone == null || telephone.isEmpty()) { navigateToSettings2FA(event); return; }
        try {
            String otp = String.format("%06d", (int)(Math.random() * 999999));
            SessionManager.setTempOtp(otp);
            SmsService smsService = new SmsService();
            if (!smsService.sendOtpCode(telephone, otp)) {
                alert(Alert.AlertType.ERROR, "Erreur", "Impossible d'envoyer le SMS."); return; }
            String masked = telephone.length() > 4
                    ? telephone.substring(0, telephone.length() - 4).replaceAll("\\d", "*")
                    + telephone.substring(telephone.length() - 4)
                    : telephone;
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("🔐 Vérification SMS");
            dialog.setHeaderText("Code envoyé au : " + masked);
            ButtonType verifyBtn = new ButtonType("Vérifier", ButtonBar.ButtonData.OK_DONE);
            ButtonType resendBtn = new ButtonType("Renvoyer", ButtonBar.ButtonData.LEFT);
            dialog.getDialogPane().getButtonTypes().addAll(verifyBtn, resendBtn, ButtonType.CANCEL);
            TextField codeField = new TextField();
            codeField.setPromptText("000000");
            codeField.setMaxWidth(200);
            codeField.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 24px; -fx-alignment: center;" +
                    "-fx-pref-height: 52px; -fx-border-color: #52B788; -fx-border-radius: 8;" +
                    "-fx-background-radius: 8; -fx-border-width: 2;");
            codeField.textProperty().addListener((obs, o, n) -> {
                if (!n.matches("\\d*")) codeField.setText(n.replaceAll("[^\\d]", ""));
                if (n.length() > 6)     codeField.setText(n.substring(0, 6));
            });
            VBox content = new VBox(14);
            content.setAlignment(javafx.geometry.Pos.CENTER);
            content.getChildren().addAll(new Label("📱 Entrez le code reçu par SMS :"), codeField, new Label("⏱ Valable 5 minutes"));
            dialog.getDialogPane().setContent(content);
            final String[] cur = { otp };
            dialog.setResultConverter(btn -> {
                if (btn == resendBtn) {
                    String nOtp = String.format("%06d", (int)(Math.random() * 999999));
                    cur[0] = nOtp; SessionManager.setTempOtp(nOtp);
                    boolean ok = smsService.sendOtpCode(telephone, nOtp);
                    alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR,
                            ok ? "SMS renvoyé" : "Erreur",
                            ok ? "Nouveau code envoyé au " + masked : "Échec envoi SMS.");
                    return null;
                }
                return btn == verifyBtn ? codeField.getText() : null;
            });
            dialog.showAndWait().ifPresent(code -> {
                if (code.equals(cur[0])) { SessionManager.clearTempOtp(); navigateToSettings2FA(event); }
                else alert(Alert.AlertType.ERROR, "Code incorrect", "Code SMS invalide.");
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void navigateToSettings2FA(MouseEvent event) {
        new Settings2FA().launch();
    }

    // ══════════════════════════════════════════════════════════════
    // Alert Helper
    // ══════════════════════════════════════════════════════════════
    private void alert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}