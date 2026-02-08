package utiles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private final String HOSTNAME = "jdbc:mysql://localhost:3306/projet3a";
    public static  MyDatabase instance;

    public Connection connection;

    public MyDatabase() {
        try {
            // Cette ligne force Java à charger le driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(HOSTNAME, USERNAME, PASSWORD);
            System.out.println("✅ Connexion réussie !");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Erreur : Driver non trouvé ! Ajoute le JAR au projet.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur : Connexion échouée (Vérifie XAMPP) : " + e.getMessage());
        }
    }
    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();

        }
        return instance;
    }
}
