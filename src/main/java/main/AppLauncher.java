package main;

/**
 * Cette classe sert de lanceur indirect.
 * JavaFX a parfois des difficultés à démarrer si le point d'entrée
 * hérite directement de la classe Application.
 */
public class AppLauncher {
    public static void main(String[] args) {
        // On appelle la méthode main de votre classe MainGUI originale
        MainGUI.main(args);
    }
}