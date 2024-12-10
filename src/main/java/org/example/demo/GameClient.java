package org.example.demo;

import javafx.application.Platform;
import javafx.scene.control.Control;

import java.io.*;
import java.net.*;

public class GameClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private final Controller controller;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private StringBuilder boardBuffer = new StringBuilder();
    private boolean receivingBoard = false;
    private String userName, password;

    public GameClient(Controller controller, String userName, String password) {
        this.controller = controller;
        this.userName = userName;
        this.password = password;

        //sendUsernameAndPassword();
    }
    public void sendUsernameAndPassword(){
        if (out != null) {
            out.println("USERNAME:" + this.userName + ":" + "PASSWORD:" + this.password);
            System.out.println("USERNAME:" + this.userName + ":PASSWORD:" + this.password);
        }
    }
    public void sendBoardSize(int size) {
        if (out != null) {
            out.println("[" + userName + "]" + " BOARD_SIZE:" + size);  // Send the size to the server
            System.out.println("Sending board size to ClientHandler: " + size);
        }
    }
//    public void sendTileSelection(int row1, int col1, int row2, int col2) {
//        if (out != null) {
//            // Serialize the board into a string representation
//
//            out.println("TILE_SELECTION:" + row1 + ":" + col1 + ":" + row2 + ":" + col2);
//        }
//    }

    public void sendTileSelection(int row1, int col1, int row2, int col2) {
        if (out != null) {
            String message = "TILE_SELECTION:" + row1 + ":" + col1 + ":" + row2 + ":" + col2;
            out.println(message);
            out.flush();
            System.out.println("Sending tile selection to server: " + message);  // Log sent message

            // Debugging the socket connection
            if (socket != null && !socket.isClosed()) {
                System.out.println("Socket is open. Data is being sent.");
            } else {
                System.err.println("Socket is closed. Unable to send data.");
            }
        } else {
            System.err.println("Output stream is null. Unable to send tile selection.");
        }
    }



    public void sendResetGame(){
        if (out != null){
            out.println("RESET_GAME");
        }
    }

    public void sendAction(String message) {
        if (out != null) {
            out.println(message); // Send the message to the server
            out.flush(); // Ensure the message is sent immediately
        }
    }

    public void connect() {
        try {
            // Attempt to connect to the server
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);
            sendUsernameAndPassword();

            // Start a new thread to listen for server messages
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println("Received from server: " + serverMessage);  // Debugging line
                        handleServerMessage(serverMessage);
                    }
                } catch (IOException e) {
                    if (e instanceof SocketException) {
                        // Connection lost, could be server shutdown or network issue
                        System.out.println("Connection lost. The server may be down or disconnected.");
                        handleServerShutdown();
                    } else {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to connect to server at " + SERVER_ADDRESS + ":" + SERVER_PORT);
            handleServerShutdown();
        }
    }

    private void handleServerShutdown() {
        // Notify the user that the server has shut down
        Platform.runLater(() -> {
            controller.showPopup("The server has shut down! Unable to continue the game!");
            controller.showNotYourTurnMessage("Server disconnected.");
            controller.gameOverUI();  // Transition to the game over UI or show a disconnect message
        });

        closeConnection();  // Gracefully close the connection
    }

    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Disconnected from server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleServerMessage(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("Waiting clients:")) {
                System.out.println("Waiting clients");
                String waitingList = message.substring("Waiting clients:".length()).trim();
                controller.displayWaitingClients(message, userName);
                //controller.showPopup("You are in the waiting status!");

            } else if (message.startsWith("Game started with")) {
                controller.hideWaitingList();

                controller.showPopup("Both player are connected!");

                if (message.contains("(leader)")){
                    controller.updateTurnLabel("Your Turn");
                } else {
                    controller.updateTurnLabel("Opponent's turn");

                }

            } else if (message.startsWith("It's not your turn")) {
                System.out.println(message);
                if (controller != null) {

                    controller.showNotYourTurnMessage("Not your turn! Be patient!");
                }

            } else if (message.startsWith("Nice!")) {
                controller.showNotYourTurnMessage(message);

            } else if (message.startsWith("No match!")) {
                controller.showNotYourTurnMessage("No match!!!");

            } else if (message.startsWith("SCORE")) {
                String score = message.split(":")[1];
                controller.updateScoreLabel(score);

            } else if (message.startsWith("Current board:")) {
                receivingBoard = true;
                boardBuffer.setLength(0); // Clear any previous data
                System.out.println("Receiving board...");

            } else if (receivingBoard) {
                if (message.trim().isEmpty()) {
                    // Empty line signals end of board transmission
                    receivingBoard = false;
                    String boardState = boardBuffer.toString();
                    System.out.println("Complete board received:\n" + boardState);

                    // Parse the board and update the controller
                    int[][] board = parseBoardState(boardState);
                    if (controller != null) {
                        controller.setGame(new Game(board));
                        controller.updateBoard(board);
                    }
                } else {
                    // Append current line to the board buffer
                    boardBuffer.append(message).append("\n");
                }

            } else if (message.contains("Turn:") && message.contains(userName)) {

                controller.updateTurnLabel("Your Turn");

            } else if (message.contains("Turn:") && !message.contains(userName)) {
                controller.updateTurnLabel("Opponent's turn");

            } else if (message.startsWith("WINNER")) {
                if (message.contains(userName)){
                    controller.showNotYourTurnMessage("Congratulation! You win!");

                } else {
                    controller.showNotYourTurnMessage("Sorry! You lose!");
                }

            } else if (message.startsWith("TIE")) {
                controller.showPopup("Game Tie!");
                controller.showNotYourTurnMessage("Game Tie");

            } else if (message.startsWith("GAME HISTORY:")) {
                String gameHistory = message.substring("GAME HISTORY:".length()).trim();
                controller.displayGameHistory(gameHistory);

            } else if (message.startsWith("PLAYERS STATUS:")) {
                String playerStatus = message.substring("PLAYERS STATUS:".length()).trim();
                controller.displayPlayerStatus(playerStatus);

            } else if (message.startsWith("The game session has ended")) {
                controller.gameOverUI();
            } else if (message.contains("has disconnected")) {
                controller.showPopup(message);
            } else if (message.startsWith("The game has resumed!")) {
                controller.hideWaitingList();
                controller.showPopup(message);

            }

        });
    }

    private int[][] parseBoardState(String boardState) {
        // Split the board state into rows
        String[] rows = boardState.split("\n");
        int[][] board = new int[rows.length][];

        for (int i = 0; i < rows.length; i++) {
            // Trim any leading/trailing spaces and split by spaces to get individual cell values
            String[] cells = rows[i].trim().split("\\s+");  // Use \\s+ to handle multiple spaces between numbers
            board[i] = new int[cells.length];

            for (int j = 0; j < cells.length; j++) {
                // Parse the number, but only if the cell is not empty
                if (!cells[j].isEmpty()) {
                    board[i][j] = Integer.parseInt(cells[j]);
                } else {
                    // If the cell is empty, we can set it to a default value, like 0
                    board[i][j] = 0;  // Or some other default value
                }
            }
        }

        return board;
    }








}
