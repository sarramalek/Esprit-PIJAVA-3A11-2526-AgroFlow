package services;

import models.User;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private Connection connection;

    public UserService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    public List<User> recuperer() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT id_user, nom FROM user"; // adaptez le nom de la table
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id_user"), rs.getString("nom")));
            }
        }
        return users;
    }
}