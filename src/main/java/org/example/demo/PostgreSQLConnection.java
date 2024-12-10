package org.example.demo;

import java.sql.*;

public class PostgreSQLConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/linking_game_db";  // Database URL
    private static final String USER = "postgres";  // Replace with your PostgreSQL username
    private static final String PASSWORD = "06122002";  // Replace with your PostgreSQL password

    public static void main(String[] args) {
        try {
            // Load the PostgreSQL JDBC Driver
            Class.forName("org.postgresql.Driver");

            // Establish the connection to the database
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);

            // Create a Statement to execute queries
            Statement stmt = connection.createStatement();

            // Execute a simple query (e.g., checking if the connection is successful)
            ResultSet rs = stmt.executeQuery("SELECT * FROM users");

            // Process the result set
            while (rs.next()) {
                String username = rs.getString("username");
                String passwordHash = rs.getString("password_hash");
                boolean onlineStatus = rs.getBoolean("online_status");
                String gameHistory = rs.getString("game_history");
                System.out.println("Username: " + username);
                System.out.println("Password Hash: " + passwordHash);
                System.out.println("Online Status: " + onlineStatus);
                System.out.println("Game History: " + gameHistory);
            }

            // Close the resources
            rs.close();
            stmt.close();
            connection.close();
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Database connection error.");
            e.printStackTrace();
        }
    }
}
