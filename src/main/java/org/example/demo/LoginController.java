package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.example.demo.PlayerDataManager.loadPlayerData;
import static org.example.demo.PlayerDataManager.savePlayerData;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private Map<String, String> playerData;
    private static Set<String> loggedInUsers = new HashSet<>();
    private ClientApp mainApp;  // Reference to ClientApp
    private Stage stage;        // Reference to the primary stage
    private Map<String, String> matchPlayer = new HashMap<>();

    public void setMainApp(ClientApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Initialize method to load player data when the app starts
    public void initialize() {
        try {
            playerData = loadPlayerData();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to load player data.");
        }
    }

    // Handle login action
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        System.out.println("Attempting login for: " + username); // Debugging
        System.out.println("Currently logged-in users before login attempt: " + loggedInUsers); // Debugging

        // Check if the username is already logged in
        if (loggedInUsers.contains(username)) {
            showError("Login Failed", "User already logged in. Please log out first.");
            System.out.println("Login failed: User already logged in"); // Debugging
            return;
        }

        // Verify credentials
        if (playerData.containsKey(username) && playerData.get(username).equals(password)) {
            loggedInUsers.add(username); // Mark the user as logged in
            System.out.println("Login successful: " + username); // Debugging
            showSuccess("Login Successful", "Welcome, " + username + "!");
            mainApp.startGame(username, password); // Start the game or next phase

        } else {
            showError("Login Failed", "Incorrect username or password.");
        }

        usernameField.setText("");
        passwordField.setText("");
    }

    // Handle registration action
    @FXML
    public void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Registration Failed", "Please fill in both username and password.");
            return;
        }

        // Check if the username already exists
        try {
            Map<String, String> existingPlayerData = loadPlayerData();

            if (existingPlayerData.containsKey(username)) {
                showAlert("Registration Failed", "Username already exists. Please choose a different one.");
            } else {
                // Username does not exist, register the new user
                existingPlayerData.put(username, password);
                savePlayerData(existingPlayerData);

                // Reload the in-memory playerData to reflect the updated state
                playerData = loadPlayerData();

                showAlert("Registration Successful", "You have successfully registered!");
                usernameField.setText("");
                passwordField.setText("");

            }
        } catch (IOException e) {
            showAlert("Registration Failed", "Error occurred while checking user data.");
            e.printStackTrace();
        }
    }

    // Utility method to show success alerts
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Utility method to show error alerts
    private void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Stage getStage() {
        return this.stage;
    }

    @FXML
    private void handleLogout(String username) {
        if (loggedInUsers.remove(username)) {
            showSuccess("Logout Successful", "Goodbye, " + username + "!");
        } else {
            showError("Logout Failed", "User not logged in.");
        }
    }
}
