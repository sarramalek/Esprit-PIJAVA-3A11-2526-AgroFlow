package controllers.Stocks;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Stocks.Article;
import models.Stocks.Mouvement;
import services.Stocks.ArticleService;
import services.Stocks.MouvementService;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class historiqueMouvementsController {

    @FXML private Label lblTitre;
    @FXML private TableView<Mouvement> tableMouvements;
    @FXML private TableColumn<Mouvement, String> colDate;
    @FXML private TableColumn<Mouvement, String> colArticle;
    @FXML private TableColumn<Mouvement, String> colType;
    @FXML private TableColumn<Mouvement, Double> colQuantite;
    @FXML private TableColumn<Mouvement, String> colMotif;
    @FXML private TextField tfSearch;
    @FXML private ComboBox<String> cbType;

    private final MouvementService mvtService = new MouvementService();
    private final ArticleService articleService = new ArticleService();
    private ObservableList<Mouvement> masterData = FXCollections.observableArrayList();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDateMouvement().format(formatter)));
        
        colArticle.setCellValueFactory(cellData -> {
            try {
                Article a = articleService.rechercherParId(cellData.getValue().getArticleId());
                return new SimpleStringProperty(a != null ? a.getNom() : "ID: " + cellData.getValue().getArticleId());
            } catch (SQLException e) {
                return new SimpleStringProperty("ID: " + cellData.getValue().getArticleId());
            }
        });

        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colMotif.setCellValueFactory(new PropertyValueFactory<>("motif"));

        cbType.setItems(FXCollections.observableArrayList("Tous", "ENTREE", "SORTIE"));
        cbType.getSelectionModel().selectFirst();

        setupFiltering();
    }

    private void setupFiltering() {
        FilteredList<Mouvement> filteredData = new FilteredList<>(masterData, p -> true);

        tfSearch.textProperty().addListener((obs, old, nv) -> updatePredicate(filteredData));
        cbType.valueProperty().addListener((obs, old, nv) -> updatePredicate(filteredData));

        tableMouvements.setItems(filteredData);
    }

    private void updatePredicate(FilteredList<Mouvement> filteredData) {
        filteredData.setPredicate(mvt -> {
            String search = tfSearch.getText().toLowerCase().trim();
            String type = cbType.getValue();

            boolean matchesSearch = search.isEmpty() || mvt.getMotif().toLowerCase().contains(search);
            // On pourrait aussi chercher par nom d'article si on pré-chargeait les noms
            
            boolean matchesType = type.equals("Tous") || mvt.getType().equals(type);

            return matchesSearch && matchesType;
        });
    }

    public void chargerHistoriqueArticle(int articleId) {
        lblTitre.setText("📜 Historique - Article #" + articleId);
        try {
            masterData.setAll(mvtService.recupererParArticle(articleId));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void chargerHistoriqueGlobal(int userId) {
        lblTitre.setText("📜 Tous mes Mouvements");
        try {
            masterData.setAll(mvtService.recupererParUser(userId));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    void fermer(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    void exporterPDF(ActionEvent event) {
        // Logique d'export PDF simplifiée ou appel à un service d'export
        System.out.println("Export PDF des mouvements...");
    }
}
