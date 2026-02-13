package utilis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private final String USER = "root";
    private final String PASSWORD = "";
    private final String URL = "jdbc:mysql://localhost:3306/projet_pi";

    public Connection connection;
    private static MyDatabase instance;

    public MyDatabase (){
        try{
            connection = DriverManager.getConnection(URL, USER,PASSWORD);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static MyDatabase getInstance() {
        if(instance == null)
            instance = new MyDatabase();
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
