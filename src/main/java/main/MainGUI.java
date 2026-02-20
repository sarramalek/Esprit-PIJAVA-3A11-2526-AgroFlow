// Indique le dossier racine du projet où se trouve ton code source
package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

// La classe hérite de 'Application', ce qui en fait le point de départ du programme JavaFX
public class MainGUI extends Application {

    // La méthode main est la première lancée par Java
    public static void main(String[] args) {
        // Appelle la méthode start() de JavaFX pour initialiser l'interface
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // 1. Prépare le chargeur pour transformer le fichier FXML en interface Java
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherAnimaux.fxml"));

        // 2. Lit le fichier FXML et crée les objets graphiques correspondants
        Parent root = loader.load();

        // 3. Crée le "contenu" de la fenêtre (la Scène) avec une taille fixe (900x600 pixels)
        Scene scene = new Scene(root, 900, 600);

        // 4. Place la scène sur le "théâtre" (la fenêtre principale nommée primaryStage)
        primaryStage.setScene(scene);

        // 5. Définit le texte qui s'affiche dans la barre de titre de la fenêtre
        primaryStage.setTitle("AgroFlow - Gestion des Animaux");

        // 6. Rend la fenêtre visible à l'utilisateur
        primaryStage.show();
    }
}