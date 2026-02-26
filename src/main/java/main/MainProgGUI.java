package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainProgGUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
<<<<<<< HEAD
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/G-Evenements/Accueil.fxml"));
=======
        // Charge le fichier FXML de ton choix (ex: AjoutPlante ou AffichagePlante)
        // Assure-toi que le chemin commence par "/" s'il est à la racine des ressources
        //FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/ajoutplante.fxml"));
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichagePlante.fxml"));
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("/acceuilterrain.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/TerrainsInterface/acceuilagricoleterrain.fxml"));
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("/EmployeRotation.fxml"));

>>>>>>> origin/terrain
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Agroflow Application");
        primaryStage.show();
    }
}
