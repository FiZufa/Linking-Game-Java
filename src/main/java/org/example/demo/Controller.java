package org.example.demo;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;

import java.util.Objects;
import java.util.Optional;

public class Controller {

    @FXML
    private Label scoreLabel;
    @FXML
    private Label turnLabel;

    @FXML
    private GridPane gameBoard;
    @FXML
    private ListView<String> waitingClientsListView;

    @FXML
    private Button selectOpponentBtn, startGameBtn; // This links to the button in the FXML
    @FXML
    private Canvas gameCanvas;  // Canvas for drawing connections
    private GraphicsContext gc;

    public static Game game;

    private String username;
    int[] position = new int[3];

    @FXML
    private Label notYourTurnLabel;
    private GameClient gameClient;
    private boolean isFirstPlayer = false;
    public int boardSize;
    private Image imageApple, imageMango, imageBlueberry, imageCherry, imageGrape;
    private Image imageCarambola, imageKiwi, imageOrange, imagePeach, imagePear;
    private Image imagePineapple, imageWatermelon;

    @FXML
    void initialize() {
        loadImages();
        //gc = gameCanvas.getGraphicsContext2D();
        System.out.println("initialize() called");
        //this.username = username;

    }

    public void setFirstPlayer(boolean isFirstPlayer) {
        this.isFirstPlayer = isFirstPlayer;
    }

    private void hideButtons() {

    }

    public void setGameClient(GameClient client) {
        this.gameClient = client;
    }

    void showBoardSizeSelectionPopup() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Board Size Selection");
        dialog.setHeaderText("You are the leader! Select board size!");

        // Set the buttons
        ButtonType button4x4 = new ButtonType("4x4");
        ButtonType button8x8 = new ButtonType("8x8");
        ButtonType button16x16 = new ButtonType("16x16");
        dialog.getDialogPane().getButtonTypes().addAll(button4x4, button8x8, button16x16);

        // Handle button clicks
        dialog.setResultConverter(buttonType -> {
            if (buttonType == button4x4) return 4;
            if (buttonType == button8x8) return 8;
            if (buttonType == button16x16) return 16;
            return 4;
        });

        // Show dialog and handle result
        Optional<Integer> result = dialog.showAndWait();
        result.ifPresent(boardSize -> {
            System.out.println("Selected board size: " + boardSize);
            //sendBoardSize(boardSize);
            this.boardSize = boardSize;
            showPopup("You are the first player");
        });
    }

    public void updateTurnLabel(String message) {
        Platform.runLater(() -> turnLabel.setText(message));
    }

    public void updateScoreLabel(String message) {
        Platform.runLater(() -> scoreLabel.setText("Score: " + message));
    }
    void showPopup(String message) {
        // Create a Label for the popup content
        Label popupContent = new Label(message);
        popupContent.setStyle("-fx-background-color: lightblue; -fx-padding: 10; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Create the Popup and add the Label
        Popup popup = new Popup();
        popup.getContent().add(popupContent);
        popup.setAutoHide(true); // Automatically hide when clicked or after delay

        // Retrieve the current active window
        Stage ownerStage = (Stage) turnLabel.getScene().getWindow(); // Example using turnLabel or any other Node
        if (ownerStage != null) {
            popup.show(ownerStage);
        } else {
            System.out.println("Owner stage is null. Cannot display popup.");
            return;
        }

        // Use PauseTransition to hide the popup after a few seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(event -> popup.hide());
        delay.play();
    }

    public void handleResetGame(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Reset");
        alert.setHeaderText("Are you sure you want to reset the game?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (gameClient != null) {
                gameClient.sendResetGame();
            }
        }
    }

    public void handleHistoryButton(ActionEvent actionEvent) {
        // IF clicked, open new window displaying history board [username, status] [username, history (Win,time)]
        if (gameClient != null) {
            gameClient.sendAction("VIEW HISTORY");
        }
        System.out.println("button history clicked");
    }

    public void handlePlayerStatus(ActionEvent actionEvent) {
        // IF clicked, open new window displaying list of players with status
        if (gameClient != null) {
            gameClient.sendAction("PLAYER STATUS");
        }
    }

    public void displayGameHistory(String gameHistory) {
        String[] historyEntries = gameHistory.split(";");

        // Create and display a new window
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().add(new Label("Game History:"));

        for (String entry : historyEntries) {
            root.getChildren().add(new Label(entry.trim())); // Trim to remove unnecessary spaces
        }

        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Game History");
        stage.show();
    }

    public void displayPlayerStatus(String playerStatus) {
        String[] statusEntries = playerStatus.split(";");

        // Create and display a new window
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().add(new Label("Online Players"));

        for (String entry : statusEntries) {
            root.getChildren().add(new Label(entry.trim()));
        }

        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.setTitle("List of currently online players");
        stage.show();
    }

    public void showNotYourTurnMessage(String message) {
        notYourTurnLabel.setText(message); // Set the message to the label
        notYourTurnLabel.setVisible(true); // Make the label visible

        // Optional: Hide the label after a few seconds
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> notYourTurnLabel.setVisible(false)); // Hide after 2 seconds
        delay.play();
    }

    public void displayWaitingClients(String message, String username) {
        this.username = username;

        // Remove the "Waiting clients:" prefix and trim any extra spaces
        message = message.substring("Waiting clients:".length()).trim();

        // Split the string by semicolon delimiter (or newline, depending on what you're using)
        String[] clients = message.split(";");

        // Clear the current list in the ListView
        waitingClientsListView.getItems().clear();

        // Track whether other players are present
        boolean otherPlayersExist = false;

        // Iterate through the clients and add them to the ListView, excluding the current user's username
        for (String client : clients) {
            client = client.trim(); // Remove extra spaces

            // Exclude the current username from the list
            if (!client.isEmpty() && !client.equals(username)) {
                waitingClientsListView.getItems().add(client); // Add the client to the ListView
                otherPlayersExist = true; // Mark that other players are present
            }
        }

        // If there are no other players in the list, show the waiting message
        if (!otherPlayersExist) {
            waitingClientsListView.getItems().add("Waiting for other players to join...");
            selectOpponentBtn.setVisible(false);
        } else {
            selectOpponentBtn.setVisible(true);
        }

        scoreLabel.setVisible(false);
        startGameBtn.setVisible(false);

    }


    @FXML
    private void handleOpponentSelection() {
        // Get the selected opponent from the ListView
        String selectedOpponent = waitingClientsListView.getSelectionModel().getSelectedItem();

        if (selectedOpponent != null) {
            // Remove the selected opponent from the ListView and inform the server
            waitingClientsListView.getItems().remove(selectedOpponent);

            hideWaitingList();

            // Show board size selection popup (for the leader)
            showBoardSizeSelectionPopup();  // Assuming this will let the leader pick the board size

            // Send the opponent selection and board size to the server
            String message = "SELECT_OPPONENT:" + username + ":" + selectedOpponent + ":BOARD_SIZE:" + boardSize;

            whenSelectOppAndBoard();

            gameClient.sendAction(message);

        }
    }

    @FXML
    void hideWaitingList() {
        scoreLabel.setVisible(true);
        startGameBtn.setVisible(true);

        waitingClientsListView.setVisible(false);
        selectOpponentBtn.setVisible(false);

    }
    public void whenSelectOppAndBoard() {
        // display board
        // display scoreLabel
        scoreLabel.setVisible(true);
        // display turnLabel
        turnLabel.setVisible(true);
        // display resetButton
        startGameBtn.setVisible(true);
        //gameCanvas.setVisible(true);


    }
    public void gameOverUI() {
        gameBoard.getChildren().clear();
        // hide turnLabel
        turnLabel.setVisible(false);
        // hide scoreLabel
        scoreLabel.setVisible(false);
        // hide reset game btn
        startGameBtn.setVisible(false);
        // display waitingList

        waitingClientsListView.setVisible(true);

    }
    public void createGameBoard() {

        gameBoard.getChildren().clear();

        for (int row = 0; row < game.row; row++) {
            for (int col = 0; col < game.col; col++) {
                Button button = new Button();
                button.setPrefSize(40, 40);
                ImageView imageView = addContent(game.board[row][col]);
                imageView.setFitWidth(30);
                imageView.setFitHeight(30);
                imageView.setPreserveRatio(true);
                button.setGraphic(imageView);
                int finalRow = row;
                int finalCol = col;
                button.setOnAction(event -> handleButtonPress(finalRow, finalCol));
                gameBoard.add(button, col, row);
            }
        }
    }

    private void handleButtonPress(int row, int col) {
        System.out.println("Button pressed at: " + row + ", " + col);

        if (position[0] == 0) {
            // First selection
            position[1] = row;
            position[2] = col;
            position[0] = 1;
        } else {
            // Second selection
            position[0] = 0;

            // Log and send the selection to the server
            System.out.println("Sending tile selection: " + position[1] + "," + position[2] + " to " + row + "," + col);
            gameClient.sendTileSelection(position[1], position[2], row, col);
        }
    }


    public void setGame(Game game){
        this.game = game;
    }
    public void updateBoard(int[][] newBoard) {
        // Update the JavaFX board UI based on the newBoard state
        Platform.runLater(() -> {
            // Clear existing board UI elements
            gameBoard.getChildren().clear();

            // Rebuild the board with the new state
            for (int row = 0; row < newBoard.length; row++) {
                for (int col = 0; col < newBoard[row].length; col++) {
                    Button button = new Button();
                    button.setPrefSize(40, 40);
                    ImageView imageView = addContent(newBoard[row][col]);
                    imageView.setFitWidth(30);
                    imageView.setFitHeight(30);
                    imageView.setPreserveRatio(true);
                    button.setGraphic(imageView);
                    int finalRow = row;
                    int finalCol = col;
                    button.setOnAction(event -> handleButtonPress(finalRow, finalCol));
                    gameBoard.add(button, col, row);
                }
            }
        });
    }


    public ImageView addContent(int content) {
        Image image = switch (content) {
            case 0 -> imageCarambola;
            case 1 -> imageApple;
            case 2 -> imageMango;
            case 3 -> imageBlueberry;
            case 4 -> imageCherry;
            case 5 -> imageGrape;
            case 6 -> imageKiwi;
            case 7 -> imageOrange;
            case 8 -> imagePeach;
            case 9 -> imagePear;
            case 10 -> imagePineapple;
            case 11 -> imageWatermelon;
            default -> null;
        };

        // Add a check to see if the image is null
        if (image == null) {
            return new ImageView(); // Returning an empty ImageView if no image is found
        }

        return new ImageView(image);
    }


    private void loadImages() {
        try {
            imageApple = new Image(getClass().getResource("/org/example/demo/apple.png").toExternalForm());
            imageMango = new Image(getClass().getResource("/org/example/demo/mango.png").toExternalForm());
            imageBlueberry = new Image(getClass().getResource("/org/example/demo/blueberry.png").toExternalForm());
            imageCherry = new Image(getClass().getResource("/org/example/demo/cherry.png").toExternalForm());
            imageGrape = new Image(getClass().getResource("/org/example/demo/grape.png").toExternalForm());
            imageCarambola = new Image(getClass().getResource("/org/example/demo/carambola.png").toExternalForm());
            imageKiwi = new Image(getClass().getResource("/org/example/demo/kiwi.png").toExternalForm());
            imageOrange = new Image(getClass().getResource("/org/example/demo/orange.png").toExternalForm());
            imagePeach = new Image(getClass().getResource("/org/example/demo/peach.png").toExternalForm());
            imagePear = new Image(getClass().getResource("/org/example/demo/pear.png").toExternalForm());
            imagePineapple = new Image(getClass().getResource("/org/example/demo/pineapple.png").toExternalForm());
            imageWatermelon = new Image(getClass().getResource("/org/example/demo/watermelon.png").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading images.");
        }
    }


}
