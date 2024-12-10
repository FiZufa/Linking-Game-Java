package org.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    private Controller controller;
    private GameClient gameClient;
    private LoginController loginController; // Make loginController an instance variable

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent loginRoot = loader.load();

        // Get the LoginController and set the main app and stage
        loginController = loader.getController(); // Assign to the instance variable
        loginController.setMainApp(this); // Pass ClientApp instance to LoginController
        loginController.setStage(primaryStage); // Pass the primary stage to LoginController

        // Set up the login scene
        Scene loginScene = new Scene(loginRoot, 800, 800);
        primaryStage.setTitle("Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    // This method will be called after a successful login
    public void startGame(String username, String password) {
        try {
            // Load the game screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("board.fxml"));
            Parent root = loader.load();

            // Get the controller for the game screen
            controller = loader.getController();
            GameServer.setController(controller);

            // Initialize the game client and set it in the controller
            gameClient = new GameClient(controller, username, password);
            controller.setGameClient(gameClient);

            // Set up the game scene
            Scene gameScene = new Scene(root, 600, 600);
            Stage gameStage = new Stage(); // Create a new stage for the game screen
            gameStage.setTitle(username);
            gameStage.setScene(gameScene);
            gameStage.show(); // Show the game stage

            // Close the login screen
            Stage primaryStage = loginController.getStage(); // Use the stored loginController instance
            primaryStage.close();

            // Connect to the game server
            gameClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
