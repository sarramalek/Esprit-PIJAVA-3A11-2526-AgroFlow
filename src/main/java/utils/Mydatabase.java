package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mydatabase {
    private final String USER = "root";
    private final String PASSWORD = "";
    private final String URL = "jdbc:mysql://localhost:3306/agroflow3";

    public Connection connection;
    public static Mydatabase instance;

    private Mydatabase() {
        try{
            connection= DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database successfully");

        }
        catch(SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public static Mydatabase getInstance() {
        if (instance == null) {
            instance = new Mydatabase();
        }
        return instance;
    }
}
