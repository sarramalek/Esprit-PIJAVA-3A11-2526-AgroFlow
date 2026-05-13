package controllers.Stocks;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import models.Stocks.Categorie;

public class CategorieCardController {

    @FXML private Label lblNom;
    private Categorie categorie;
    private AfficherStockOuvrierController mainController;

    public void setData(Categorie categorie, AfficherStockOuvrierController mainController) {
        this.categorie = categorie;
        this.mainController = mainController;
        lblNom.setText(categorie.getNom());
    }

    @FXML
    void handleCategoryClick() {
        mainController.filtrerParCategorie(categorie);
    }
}
