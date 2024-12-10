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

//import java.awt.Label;
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
    private Label notYourTurnLabel; // fx:id="notYourTurnLabel"
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
                //selectOpponentBtn.setVisible(true);
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

            gameClient.sendAction(message); // Send the message to the server

            // Optionally, show or hide other UI components for the game
            // showGameBoard();  // A method to switch the view to the game board

            // Update any other UI components here (e.g., hide buttons, etc.)
        }
    }

    @FXML
    void hideWaitingList() {
        scoreLabel.setVisible(true);
        startGameBtn.setVisible(true);

        waitingClientsListView.setVisible(false);
        selectOpponentBtn.setVisible(false);
        //gameCanvas.setVisible(false);
    }

    public void clearGameBoard() {
        gameBoard.getChildren().clear(); // Remove all children from the GridPane
        turnLabel.setVisible(false);
        scoreLabel.setVisible(false);
    }

    // When client select opponent and board size
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

    // When game over
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

    public void displayConnection(List<Game.Coordinate> connection) {
        // Clear any previous drawings (optional)
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Start drawing the connection with animation
        animateConnection(connection);
    }

    private void animateConnection(List<Game.Coordinate> connection) {
        // Loop through the coordinates and animate the connection
        for (int i = 1; i < connection.size(); i++) {
            final int prevIndex = i - 1;
            final int currentIndex = i;

            // Delay the drawing of each line segment (like setTimeout in JavaScript)
            PauseTransition pause = new PauseTransition(Duration.millis(currentIndex * 500)); // Delay between each line

            pause.setOnFinished(event -> {
                // Draw line between two coordinates
                Game.Coordinate prevCoord = connection.get(prevIndex);
                Game.Coordinate currentCoord = connection.get(currentIndex);
                drawLine(prevCoord, currentCoord);
            });

            pause.play();
        }
    }

    private void drawLine(Game.Coordinate startCoord, Game.Coordinate endCoord) {
        double tileSize = 50;  // Assume each tile is 50x50 pixels (adjust based on your layout)

        // Get the pixel positions from the coordinate system
        double startX = startCoord.x * tileSize + tileSize / 2;
        double startY = startCoord.y * tileSize + tileSize / 2;
        double endX = endCoord.x * tileSize + tileSize / 2;
        double endY = endCoord.y * tileSize + tileSize / 2;

        // Set the line color and width
        gc.setStroke(Color.RED);
        gc.setLineWidth(3);

        // Draw a line between the two coordinates
        gc.strokeLine(startX, startY, endX, endY);
    }

    public void onMatchFound(List<Game.Coordinate> connection) {
        // Assuming the connection is a list of coordinates like [{x: 0, y: 0}, {x: 0, y: 1}, {x: 1, y: 1}]
        displayConnection(connection);
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



//    public static Image imageApple = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/apple.png")).toExternalForm());
//    public static Image imageMango = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/mango.png")).toExternalForm());
//    public static Image imageBlueberry = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/blueberry.png")).toExternalForm());
//    public static Image imageCherry = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/cherry.png")).toExternalForm());
//    public static Image imageGrape = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/grape.png")).toExternalForm());
//    public static Image imageCarambola = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/carambola.png")).toExternalForm());
//    public static Image imageKiwi = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/kiwi.png")).toExternalForm());
//    public static Image imageOrange = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/orange.png")).toExternalForm());
//    public static Image imagePeach = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/peach.png")).toExternalForm());
//    public static Image imagePear = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/pear.png")).toExternalForm());
//    public static Image imagePineapple = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/pineapple.png")).toExternalForm());
//    public static Image imageWatermelon = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/watermelon.png")).toExternalForm());



}
