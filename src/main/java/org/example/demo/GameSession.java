package org.example.demo;

import javax.xml.transform.Result;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameSession {
    private Game game;  // Use the existing Game class
    private ClientHandler playerOne; // leader
    private ClientHandler playerTwo;
    private boolean playerOneTurn;
    private boolean playerTwoTurn;
    private int playerOneScore;
    private int playerTwoScore;
    private ClientHandler currentTurn;
    private int boardSize;
    private boolean playerOneDisconnected = false;
    private boolean playerTwoDisconnected = false;

    public GameSession(ClientHandler playerOne, ClientHandler playerTwo, int boardSize) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.game = new Game(generateBoard(boardSize));  // Create a new game with the board
        this.playerOneTurn = true;  // Player One starts
        this.playerTwoTurn = false;
        this.playerOneScore = 0;
        this.playerTwoScore = 0;
        this.currentTurn = playerOne;
        this.boardSize = boardSize;

        playerOne.setMyTurn(true);
        playerTwo.setMyTurn(false);

        System.out.println("GameSession created between " + playerOne.getUsername() + " and " + playerTwo.getUsername());
    }

    public ClientHandler getCurrentTurn() {
        return currentTurn;
    }

    public void switchTurns() {
        if (currentTurn == playerOne) {
            currentTurn = playerTwo;
            playerOne.setMyTurn(false);
            playerTwo.setMyTurn(true);
        } else {
            currentTurn = playerOne;
            playerOne.setMyTurn(true);
            playerTwo.setMyTurn(false);
        }
    }

    public void notifyPairs(String message) {
        playerOne.sendMessage(message);
        playerTwo.sendMessage(message);

    }

    private int[][] generateBoard(int size) {
        // Generate a random game board with the given size
        int[][] board;
        boolean validBoard;

        do {
            // Step 1: Generate a random board
            board = new int[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    // Generate a random number between 0 and 11
                    board[i][j] = (int) (Math.random() * 12); // 0 to 11 inclusive
                }
            }

            // Step 2: Check if the board is valid
            validBoard = hasValidMatch(board);

        } while (!validBoard); // Regenerate the board if no matches are found

        return board;
    }

    public void sendBoardToPlayers() {
        String boardState = generateBoardState();

        playerOne.sendMessage("Current board:\n" + boardState);
        playerTwo.sendMessage("Current board:\n" + boardState);

    }

    public String generateBoardState() {
        StringBuilder boardState = new StringBuilder();
        for (int i = 0; i < this.game.board.length; i++) {
            for (int j = 0; j < this.game.board[i].length; j++) {
                boardState.append(game.board[i][j]).append(" ");
            }
            boardState.append("\n");
        }
        return boardState.toString();
    }

    // Getters and setters
    public Game getGame() {
        return game;
    }

    public ClientHandler getPlayerOne() {
        return playerOne;
    }

    public ClientHandler getPlayerTwo() {
        return playerTwo;
    }
    public void resetGame() {
        this.game = new Game(generateBoard(boardSize));
        this.currentTurn = playerOne;

        this.playerOneScore = 0;
        this.playerTwoScore = 0;

        sendBoardToPlayers();
        sendScores();
    }

    public boolean isPlayerOneTurn() {
        return playerOneTurn;
    }

    public boolean isPlayerTwoTurn() {
        return playerTwoTurn;
    }

    // get opponent

    public void toggleTurn() {
        playerOneTurn = !playerOneTurn;
        playerTwoTurn = !playerTwoTurn;
    }

    public void updateScore(ClientHandler player, int points) {
        if (player.equals(playerOne)) {
            playerOneScore += points;
        } else if (player.equals(playerTwo)) {
            playerTwoScore += points;
        }
    }

    public void sendScores() {
        playerOne.sendMessage("SCORE:" + this.playerOneScore);
        playerTwo.sendMessage("SCORE:" + this.playerTwoScore);
    }

    public boolean hasPossibleMoves() {
        return game.hasPossibleMoves();  // Use the method in the Game class
    }

    public String endGame() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = sdf.format(new Date());

        synchronized (this) {

            String resultMessage;
            String result;

            if (playerOneScore > playerTwoScore) {
                resultMessage = "WINNER:" + playerOne.getUsername() ;
                result = playerOne.getUsername() + ":" + playerTwo.getUsername() + "," + "WIN:LOSE";

            } else if (playerTwoScore > playerOneScore) {
                resultMessage = "WINNER:" + playerTwo.getUsername() ;
                result = playerOne.getUsername() + ":" + playerTwo.getUsername() + "," + "LOSE:WIN";

            } else {
                resultMessage = "TIE";
                result = playerOne.getUsername() + ":" + playerTwo.getUsername() + "," + "TIE:TIE";
            }

            // Notify both players of the game result
            playerOne.sendMessage(resultMessage);
            playerTwo.sendMessage(resultMessage);

            // Optionally notify the server or log the results
            System.out.println("Game session between " + playerOne.getUsername() + " and " +
                    playerTwo.getUsername() + " has ended.");

            String history = String.format("%s,[%s]", result, formattedTime);

            this.playerOneScore = 0;
            this.playerTwoScore = 0;

            sendScores();

            // Clean up: Remove this session from active games
            //GameServer.removeGameSession(this);

            // Optionally allow rematches or disconnect players
            //playerOne.setGameSession(null);
            //playerTwo.setGameSession(null);

            return history;
        }
    }


    private static boolean hasValidMatch(int[][] board) {
        int row = board.length;
        int col = board[0].length;

        // Iterate through every pair of tiles on the board
        for (int r1 = 0; r1 < row; r1++) {
            for (int c1 = 0; c1 < col; c1++) {
                for (int r2 = 0; r2 < row; r2++) {
                    for (int c2 = 0; c2 < col; c2++) {
                        // Skip the same tile
                        if (r1 == r2 && c1 == c2) continue;

                        // Check if these two tiles form a valid match
                        if (checkMatchGeneration(r1, c1, r2, c2, board)) {
                            return true; // A match is found
                        }
                    }
                }
            }
        }

        return false; // No valid matches found
    }

    private static boolean checkMatchGeneration(int row1, int col1, int row2, int col2, int[][] board) {
        if ((board[row1][col1] != board[row2][col2]) || (row1 == row2 && col1 == col2)) {

            return false; // [0,0,0,0]
        }

        String connectionDetails = "";

        // One-line connection (5 point)
        if (isDirectlyConnected(row1, col1, row2, col2, board)) {
            connectionDetails = "One-line connection: (" + row1 + "," + col1 + ") -> (" + row2 + "," + col2 + ")";
            //updateScore(points);
            System.out.println(connectionDetails);
            //points[1] = 5 ;

            return true; // [0,5,0,0]
        }

        // Two-line connection (10 points)
        if ((row1 != row2) && (col1 != col2)) {
            if (board[row1][col2] == 0 && isDirectlyConnected(row1, col1, row1, col2, board)
                    && isDirectlyConnected(row1, col2, row2, col2, board)) {
                connectionDetails = "Two-line connection: (" + row1 + "," + col1 + ") -> (" + row1 + "," + col2 + ") -> (" + row2 + "," + col2 + ")";
                //updateScore(points);
                System.out.println(connectionDetails);
                //points[2] = 10 ;

                return true; // [0,5,0,0]


            }

            if (board[row2][col1] == 0 && isDirectlyConnected(row2, col2, row2, col1, board)
                    && isDirectlyConnected(row2, col1, row1, col1, board)) {
                connectionDetails = "Two-line connection: (" + row2 + "," + col2 + ") -> (" + row2 + "," + col1 + ") -> (" + row1 + "," + col1 + ")";
                //updateScore(points);
                System.out.println(connectionDetails);
                //points[2] = 10 ;

                return true; //[0,5,0,0]

            }
        }

        // Three-line connection (15 points)
        if (row1 != row2) {
            for (int i = 0; i < board[0].length; i++) {
                if (board[row1][i] == 0 && board[row2][i] == 0 &&
                        isDirectlyConnected(row1, col1, row1, i, board) && isDirectlyConnected(row1, i, row2, i, board)
                        && isDirectlyConnected(row2, col2, row2, i, board)) {
                    connectionDetails = "Three-line connection: (" + row1 + "," + col1 + ") -> (" + row1 + "," + i + ") -> (" + row2 + "," + i + ") -> (" + row2 + "," + col2 + ")";
                    //updateScore(points);
                    System.out.println(connectionDetails);
                    //points[3] = 15 ;

                    return true; // [0,5,0,0]

                }
            }
        }

        if (col1 != col2) {
            for (int j = 0; j < board.length; j++) {
                if (board[j][col1] == 0 && board[j][col2] == 0 &&
                        isDirectlyConnected(row1, col1, j, col1, board) && isDirectlyConnected(j, col1, j, col2, board)
                        && isDirectlyConnected(row2, col2, j, col2, board)) {
                    connectionDetails = "Three-line connection: (" + row1 + "," + col1 + ") -> (" + j + "," + col1 + ") -> (" + j + "," + col2 + ") -> (" + row2 + "," + col2 + ")";
                    //updateScore(points);
                    System.out.println(connectionDetails);
                    //points[3] = 15 ;

                    return true; // [0,5,0,0]

                }
            }
        }

        // If no connection is found, return false
        return false;
    }

    private static boolean isDirectlyConnected(int row1, int col1, int row2, int col2, int[][] board) {
        if (row1 == row2) {
            int minCol = Math.min(col1, col2);
            int maxCol = Math.max(col1, col2);
            for (int col = minCol + 1; col < maxCol; col++) {
                if (board[row1][col] != 0) {
                    return false;
                }
            }
            return true;
        } else if (col1 == col2) {
            int minRow = Math.min(row1, row2);
            int maxRow = Math.max(row1, row2);
            for (int row = minRow + 1; row < maxRow; row++) {
                if (board[row][col1] != 0) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    public ClientHandler getOpponent(ClientHandler clientHandler) {

        if (clientHandler == playerOne){
            return playerTwo ;
        } else if (clientHandler == playerTwo) {
            return playerOne ;
        } else {
            return null;
        }
    }

    public boolean containsPlayer(ClientHandler client) {

        return client == playerOne || client == playerTwo;
    }

    public void reconnectPlayer(ClientHandler client) {
        // Reconnect the player to the correct side
        if (client.getUsername().equals(playerOne.getUsername())) {
            this.playerOne = client;
        } else if (client.getUsername().equals(playerTwo.getUsername())) {
            this.playerTwo = client;
        }

        // Set the player's game session
        client.setGameSession(this);

        // Send current scores to both players
        sendScores();

        // Send the current board state to both players to resume the game
        sendBoardToPlayers();

        // Now determine whose turn it is, based on the current game session
        if (currentTurn.equals(playerOne)) {
            this.currentTurn = playerOne;
            playerOne.sendMessage("Turn: " + this.currentTurn.getUsername());
            playerTwo.sendMessage("Turn: " + this.currentTurn.getUsername());
        } else {
            this.currentTurn = playerTwo;
            playerTwo.sendMessage("Turn: " + this.currentTurn.getUsername());
            playerOne.sendMessage("Turn: " + this.currentTurn.getUsername());
        }

        // Optional: Set player’s turn flags if necessary (this should already be handled by the currentTurn)
        playerOne.setMyTurn(currentTurn.equals(playerOne));
        playerTwo.setMyTurn(currentTurn.equals(playerTwo));
    }

    public boolean hasPlayer(String username) {
        return (playerOne != null && playerOne.getUsername().equals(username)) ||
                (playerTwo != null && playerTwo.getUsername().equals(username));
    }

    public synchronized void playerDisconnected(ClientHandler player) {
        if (player.equals(playerOne)) {
            playerOneDisconnected = true;
        } else if (player.equals(playerTwo)) {
            playerTwoDisconnected = true;
        }

        // If both players are disconnected, terminate the session
        if (playerOneDisconnected && playerTwoDisconnected) {
            endGameSession();
        }
    }

    void endGameSession() {
        // Log the session termination
        System.out.println("Both players disconnected. Terminating session.");

        // Optionally, log details of the session (timestamp, player names, etc.)
        logSessionTermination();

        // Remove the game session from active sessions and log it
        GameServer.removeGameSessionDisconnected(this);
    }

    private void logSessionTermination() {
        // Log details about the terminated session (e.g., player names, timestamp)
        String logMessage = String.format("Game session terminated. Players: %s and %s. Timestamp: %s",
                playerOne.getUsername(), playerTwo.getUsername(), new java.util.Date());
        System.out.println(logMessage); // For now, print it to console. Could also log to a file.
    }

    public boolean isBothPlayersDisconnected() {
        return playerOneDisconnected && playerTwoDisconnected;
    }


}

