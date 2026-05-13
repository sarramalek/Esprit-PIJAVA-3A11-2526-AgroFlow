package controllers.Stocks;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import models.Stocks.Mouvement;
import models.User.Personne;
import services.Stocks.MouvementService;
import services.Stocks.ArticleService;
import utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AfficherMouvementOuvrierController {

    @FXML private Label userNameLabel;
    @FXML private TableView<Mouvement> tvMouvements;
    @FXML private TableColumn<Mouvement, String> colDate;
    @FXML private TableColumn<Mouvement, String> colType;
    @FXML private TableColumn<Mouvement, String> colArticle;
    @FXML private TableColumn<Mouvement, Double> colQuantite;
    @FXML private TableColumn<Mouvement, String> colMotif;

    private final MouvementService mouvementService = new MouvementService();
    private final ArticleService articleService = new ArticleService();

    @FXML
    public void initialize() {
        Personne user = SessionManager.getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getPrenom() + " " + user.getNom());
            chargerMouvements(user.getCin());
        }
        setupColumns();
    }

    private void setupColumns() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        colDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDateMouvement().format(formatter)));
        
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));
        
        colArticle.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(articleService.recupererNomParId(cellData.getValue().getArticleId()));
            } catch (SQLException e) {
                return new SimpleStringProperty("Inconnu");
            }
        });

        // Style pour le type (Rouge pour sortie)
        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("SORTIE".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void chargerMouvements(int userId) {
        try {
            List<Mouvement> list = mouvementService.recupererParUser(userId);
            tvMouvements.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void retourStocks() {
        changerScene(null, "/StocksInterface/AfficherStockOuvrier.fxml");
    }

    @FXML
    void handleDashboard(MouseEvent event) {
        changerScene(event, "/UsersInterface/AcceuilEmp.fxml");
    }

    @FXML
    void handleStocks(MouseEvent event) {
        retourStocks();
    }

    @FXML
    void handleLogout() {
        SessionManager.clearSession();
        javafx.application.Platform.exit();
    }

    private void changerScene(MouseEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage;
            if (event != null) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) tvMouvements.getScene().getWindow();
            }
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
