package controllers.Stocks;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Stocks.Article;
import models.Stocks.Mouvement;
import models.User.Personne;
import services.Stocks.ArticleService;
import services.Stocks.MouvementService;
import utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonBar;
import java.util.stream.Collectors;
import javafx.scene.control.TableCell;

public class ArticleCardController {

    @FXML private Label lblCategorie;
    @FXML private Label lblNom;
    @FXML private Label lblQuantite;
    @FXML private Label lblUnite;
    @FXML private Button btnSortie;

    private Article article;
    private AfficherStockOuvrierController mainController;
    private final MouvementService mouvementService = new MouvementService();
    private final ArticleService articleService = new ArticleService();

    public void setData(Article article, AfficherStockOuvrierController mainController) {
        this.article = article;
        this.mainController = mainController;

        lblNom.setText(article.getNom());
        lblCategorie.setText(article.getNomCategorie() != null ? article.getNomCategorie().toUpperCase() : "SANS CATÉGORIE");
        lblQuantite.setText(String.valueOf(article.getQuantiteEnStock()));
        lblUnite.setText(article.getUniteMesure());
    }

    @FXML
    void handleVoirMouvements() {
        try {
            List<Mouvement> list = mouvementService.recupererParArticle(article.getId());
            
            // FILTRAGE : Uniquement les mouvements de SORTIE de l'utilisateur connecté (Ouvrier ou Agriculteur)
            Personne currentUser = SessionManager.getCurrentUser();
            if (currentUser != null && currentUser.getRole() != 3) { // On filtre pour tout le monde sauf l'Admin (Role 3)
                list = list.stream()
                        .filter(m -> m.getIdUser() == currentUser.getCin() && "SORTIE".equalsIgnoreCase(m.getType()))
                        .collect(Collectors.toList());
            }
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("📜 Mon Historique : " + article.getNom());
            dialog.setHeaderText("Mes mouvements de sortie sur cet article");
            
            ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().add(closeButton);
            
            TableView<Mouvement> table = new TableView<>();
            table.setItems(FXCollections.observableArrayList(list));
            table.setPrefWidth(700);
            
            TableColumn<Mouvement, String> colDate = new TableColumn<>("Date");
            colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDateMouvement().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            
            TableColumn<Mouvement, String> colType = new TableColumn<>("Type");
            colType.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("type"));
            
            TableColumn<Mouvement, Double> colQty = new TableColumn<>("Quantité");
            colQty.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantite"));
            
            TableColumn<Mouvement, String> colMotif = new TableColumn<>("Détails");
            colMotif.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("motif"));

            TableColumn<Mouvement, Void> colEdit = new TableColumn<>("Actions");
            colEdit.setCellFactory(param -> new TableCell<>() {
                private final Button btn = new Button("Modifier");
                {
                    btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand;");
                    btn.setOnAction(event -> {
                        Mouvement m = getTableView().getItems().get(getIndex());
                        modifierMouvement(m, dialog);
                    });
                }
                @Override protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });
            
            table.getColumns().addAll(colDate, colType, colQty, colMotif, colEdit);
            
            dialog.getDialogPane().setContent(table);
            dialog.showAndWait();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void modifierMouvement(Mouvement m, Dialog<?> parentDialog) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(m.getQuantite()));
        dialog.setTitle("Modifier Mouvement");
        dialog.setHeaderText("Modifier la quantité sortie pour : " + article.getNom());
        dialog.setContentText("Nouvelle quantité :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtyStr -> {
            try {
                double newQty = Double.parseDouble(qtyStr);
                if (newQty <= 0) {
                    showError("Erreur", "La quantité doit être supérieure à 0.");
                    return;
                }
                
                // Calcul de la différence pour vérifier le stock
                double diff = newQty - m.getQuantite();
                if (diff > article.getQuantiteEnStock()) {
                    showError("Erreur", "Stock insuffisant pour augmenter la sortie.");
                    return;
                }

                mouvementService.modifierQuantiteMouvement(m, newQty);
                
                // Mettre à jour l'article localement
                article.setQuantiteEnStock(article.getQuantiteEnStock() - diff);
                lblQuantite.setText(String.valueOf(article.getQuantiteEnStock()));
                
                showInfo("Succès", "Mouvement modifié !");
                parentDialog.close(); // On ferme le dialogue d'historique pour forcer le rafraîchissement
                mainController.rafraichir();
            } catch (NumberFormatException | SQLException e) {
                showError("Erreur", "Saisie invalide ou erreur DB.");
            }
        });
    }

    @FXML
    void handleSortie() {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("📉 Sortie de Stock");
        dialog.setHeaderText("Retirer du stock pour : " + article.getNom());
        dialog.setContentText("Quantité à retirer (" + article.getUniteMesure() + ") :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtyStr -> {
            try {
                double qty = Double.parseDouble(qtyStr);
                if (qty <= 0) {
                    showError("Erreur", "La quantité doit être supérieure à 0.");
                    return;
                }
                if (qty > article.getQuantiteEnStock()) {
                    showError("Erreur", "Stock insuffisant (disponible: " + article.getQuantiteEnStock() + ")");
                    return;
                }

                effectuerSortie(qty);
            } catch (NumberFormatException e) {
                showError("Erreur", "Veuillez saisir un nombre valide.");
            }
        });
    }

    private void effectuerSortie(double qty) {
        Personne user = SessionManager.getCurrentUser();
        if (user == null) return;

        try {
            // 1. Mettre à jour la quantité de l'article (localement pour l'objet, la DB est gérée par le service mouvement)
            article.setQuantiteEnStock(article.getQuantiteEnStock() - qty);
            
            // 2. Créer le mouvement (qui mettra à jour la DB via MouvementService.ajouterMouvement)
            Mouvement m = new Mouvement();
            m.setArticleId(article.getId());
            m.setType("SORTIE");
            m.setQuantite(qty);
            m.setDateMouvement(LocalDateTime.now());
            m.setIdUser(user.getCin());
            
            // Commentaire par défaut: Nom et ID de l'ouvrier
            m.setMotif("Sortie effectuée par " + user.getPrenom() + " " + user.getNom() + " (ID: " + user.getCin() + ")");

            mouvementService.ajouterMouvement(m);

            showInfo("Succès", "Mouvement de sortie enregistré !");
            mainController.rafraichir();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur DB", "Impossible d'enregistrer le mouvement.");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
