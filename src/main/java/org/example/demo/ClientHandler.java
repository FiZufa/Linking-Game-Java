package org.example.demo;

import javafx.application.Platform;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;

import static org.example.demo.Controller.game;
import static org.example.demo.GameServer.*;

//import static org.example.demo.GameServer.controller;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private int score;
    private String username, password ;
    private boolean isInGame;
    private int userStatus = 0; // 0: offline, 1: waiting, 2:playing
    private boolean isMyTurn = false;
    public String opponentName;
    private GameSession gameSession;

    public ClientHandler(Socket clientSocket, String username, String password) {
        this.clientSocket = clientSocket;
        //this.isFirstClient = isFirstClient;
        this.username = username;
        this.password = password;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        try {
            // Continuous message reading loop
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received message from client: " + message);

                if (message.contains("TILE_SELECTION")) {
                    //System
                    handleTileSelection(message);

                } else if (message.startsWith("RESET_GAME")) {
                    handleReset();

                } else if (message.startsWith("VIEW HISTORY")) {
                    // TODO: ASK SERVER TO SEND PLAYER'S HISTORY
                    String historyBoardString = sendHistoryBoard();
                    sendMessage(historyBoardString);

                } else if (message.startsWith("PLAYER STATUS")) {
                    // TODO: ASK SERVER TO SEND PLAYER'S STATUS
                    String playerStatusString = sendPlayerStatus();
                    sendMessage(playerStatusString);

                } else if (message.startsWith("SELECT_OPPONENT:")) {
                    // SELECT_OPPONENT:username:selectedOpponent:BOARD_SIZE:boardSize
                    String[] parts = message.substring("SELECT_OPPONENT:".length()).split(":");
                    String currentUsername = parts[0];
                    String selectedOpponent = parts[1];
                    int boardSize = Integer.parseInt(parts[3]);

                    this.opponentName = selectedOpponent;

                    handleMatching(currentUsername, selectedOpponent, boardSize);
                }


            }
        } catch (IOException e) {
            if (e instanceof SocketException) {
                GameServer.handleDisconnection(this);

                GameServer.broadcastWaitingList();
            }
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void sendMessage(String message) {
        out.println(message);
        System.out.println("Send message to client: " + message);
    }
    public void handleTileSelection(String message) throws IOException {
        String[] parts = message.split(":");
        if (parts.length == 5 && parts[0].equals("TILE_SELECTION")) {
            int row1 = Integer.parseInt(parts[1]);
            int col1 = Integer.parseInt(parts[2]);
            int row2 = Integer.parseInt(parts[3]);
            int col2 = Integer.parseInt(parts[4]);

            // Get the game session associated with this player
            GameSession gameSession = this.getGameSession();

            if (gameSession == null) {
                sendMessage("Error: No game session found for the player.");
                return;
            }

            int[][] board = gameSession.getGame().board;

            System.out.println("Player: " + this.username);
            System.out.println("Selected tiles: (" + row1 + ", " + col1 + ") and (" + row2 + ", " + col2 + ")");
            System.out.println("Board dimensions: " + board.length + "x" + board[0].length);

            if (row1 < 0 || row1 >= board.length || col1 < 0 || col1 >= board[0].length ||
                    row2 < 0 || row2 >= board.length || col2 < 0 || col2 >= board[0].length) {
                sendMessage("Error: Tile selection out of bounds.");
                return;
            }

            ClientHandler currentPlayer = gameSession.getCurrentTurn();

            // Ensure it's this player's turn
            if (!currentPlayer.equals(this)) {
                sendMessage("It's not your turn. Please wait for your turn.");
                return;
            }

            // Process move
            boolean match = gameSession.getGame().checkMatch(row1, col1, row2, col2);
            if (match) {

                //List<Game.Coordinate> connection = gameSession.getGame().getConnection(row1, col1, row2, col2);

                //sendConnectionToClient(connection);

                // If there's a match, update the board
                gameSession.getGame().updateBoardMatch(row1, col1, row2, col2);

                sendMessage("Nice!");
                // Switch turns after a valid move

                int point = Arrays.stream(gameSession.getGame().checkMatchPoint(row1, col1, row2, col2)).sum();

                // calculate points
                gameSession.updateScore(currentPlayer, point);

                // switch turn
                gameSession.switchTurns();

                // send score to UI
                gameSession.sendScores();

                // Send updated board and notify players
                gameSession.sendBoardToPlayers();

                gameSession.notifyPairs("Turn: " + gameSession.getCurrentTurn().getUsername());

                if (!gameSession.hasPossibleMoves()) {
                    String gameResult = gameSession.endGame();

                    PlayerDataManager.addMatchHistory(gameResult);

                    GameServer.removeGameSession(gameSession);

                }

            } else {
                // If no match, inform the player
                sendMessage("No match! Try again.");

                // switch turn
                gameSession.switchTurns();

                gameSession.notifyPairs("Turn: " + gameSession.getCurrentTurn().getUsername());

            }
        }
    }

    public void handleReset() {
        GameSession gameSession = this.getGameSession();

        gameSession.resetGame();
    }

    public String getUsername(){
        return username;
    }


    public boolean isInGame() {
        return this.isInGame;
    }

    public void setMyTurn(boolean myturn){
        this.isMyTurn = myturn;
    }
    public GameSession getGameSession() {
        return gameSession;
    }

    // Setter for the GameSession
    public void setGameSession(GameSession gameSession) {
        this.gameSession = gameSession;
    }

}



