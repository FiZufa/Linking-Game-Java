package org.example.demo;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 12345;
    public static PlayerDataManager playerDataManager = new PlayerDataManager();
    static final Set<String> loggedInUsers = new HashSet<>();
    static final List<ClientHandler> allClients = new ArrayList<>();
    private static final List<ClientHandler> waitingClients = new ArrayList<>();
    private static List<GameSession> activeGames = new ArrayList<>();
    private static final Map<String, GameSession> disconnectedSessions = new HashMap<>();
    static Controller controller;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running and waiting for clients to connect...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                BufferedReader tempIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter tempOut = new PrintWriter(clientSocket.getOutputStream(), true);

                String credentials = tempIn.readLine();
                if (credentials != null && credentials.startsWith("USERNAME:")) {
                    String[] parts = credentials.split(":");
                    if (parts.length == 4 && parts[0].equals("USERNAME") && parts[2].equals("PASSWORD")) {
                        String username = parts[1];
                        String password = parts[3];

                        synchronized (loggedInUsers) {
                            if (loggedInUsers.contains(username)) {
                                tempOut.println("This username is already logged in. Please log out before logging in again.");
                                clientSocket.close();
                                continue;
                            }
                        }

                        // Check for reconnection
                        synchronized (disconnectedSessions) {
                            if (disconnectedSessions.containsKey(username)) {
                                // Reconnect the client
                                ClientHandler reconnectedClient = new ClientHandler(clientSocket, username, password);

                                // Re-add to loggedInUsers
                                synchronized (loggedInUsers) {
                                    loggedInUsers.add(username);
                                }

                                // Handle reconnection
                                handleReconnect(reconnectedClient);

                                // Start the reconnected client thread
                                new Thread(reconnectedClient).start();

                                System.out.println("Reconnection handled for user: " + username);
                                continue; // Skip adding to waitingClients or other steps
                            }
                        }

                        // If not reconnecting, handle as a new connection
                        ClientHandler clientHandler = new ClientHandler(clientSocket, username, password);

                        synchronized (waitingClients) {
                            waitingClients.add(clientHandler); // Add the actual ClientHandler object
                        }

                        synchronized (allClients) {
                            allClients.add(clientHandler);
                        }

                        synchronized (loggedInUsers) {
                            loggedInUsers.add(username);
                        }

                        // Start the client handler thread for the new connection
                        new Thread(clientHandler).start();

                        broadcastWaitingList();
                    } else {
                        tempOut.println("Invalid credentials format. Disconnecting.");
                        clientSocket.close();
                    }
                } else {
                    tempOut.println("No credentials provided. Disconnecting.");
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void broadcastWaitingList() {
        StringBuilder waitingList = new StringBuilder("Waiting clients:");
        synchronized (waitingClients) {

            for (ClientHandler client : waitingClients) {
                if (!client.isInGame()) {  // Only show clients that are not in-game
                    waitingList.append(client.getUsername()).append(";");
                }
            }
        }
        // Send the updated waiting list to all clients
        for (ClientHandler client : waitingClients) {
            client.sendMessage(waitingList.toString());
        }
    }

    public static void createGameSession(ClientHandler playerOne, ClientHandler playerTwo, int boardSize) {
        GameSession session = new GameSession(playerOne, playerTwo, boardSize);
        activeGames.add(session);

        playerOne.setGameSession(session);
        playerTwo.setGameSession(session);

        // Notify players that the game has started
        playerOne.sendMessage("Game started with (leader) " + playerTwo.getUsername());
        playerTwo.sendMessage("Game started with " + playerOne.getUsername());

        session.sendBoardToPlayers();
    }

    public static void handleMatching(String leaderPlayer, String opponent, int boardSize) {
        ClientHandler leader = null;
        ClientHandler opp = null;

        synchronized (waitingClients) {
            for (ClientHandler client : waitingClients) {
                if (client.getUsername().equals(leaderPlayer)) {
                    leader = client;
                }
                if (client.getUsername().equals(opponent)) {
                    opp = client;
                }
                if (leader != null && opp != null) break;
            }

            if (leader != null && opp != null) {
                // Remove players from waiting list
                waitingClients.remove(leader);
                waitingClients.remove(opp);

                broadcastWaitingList(); // Update waiting clients' UI

                createGameSession(leader, opp, boardSize); // Create and link the game session
            } else {
                System.err.println("Error: One or both players not found in waitingClients.");
            }
        }
    }


    public static void setController(Controller controller) {
        GameServer.controller = controller;
    }

    public static void removeGameSession(GameSession session) {
        synchronized (activeGames) {
            // Remove the game session from the active list
            if (activeGames.remove(session)) {
                System.out.println("GameSession removed successfully.");
            } else {
                System.out.println("GameSession not found in the active list.");
            }
        }

        // Clear the players' references to the game session
        session.getPlayerOne().setGameSession(null);
        session.getPlayerTwo().setGameSession(null);

        // Notify players to clear the game board and switch back to the waiting list UI
        session.getPlayerOne().sendMessage("CLEAR_BOARD");
        session.getPlayerTwo().sendMessage("CLEAR_BOARD");

        // Ensure players are added to the waiting list only once
        synchronized (waitingClients) {
            if (!waitingClients.contains(session.getPlayerOne())) {
                waitingClients.add(session.getPlayerOne());
            }
            if (!waitingClients.contains(session.getPlayerTwo())) {
                waitingClients.add(session.getPlayerTwo());
            }
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // Delay of 5 seconds before sending the message
        scheduler.schedule(() -> {
            session.getPlayerOne().sendMessage("The game session has ended. Returning to the waiting list.");
            session.getPlayerTwo().sendMessage("The game session has ended. Returning to the waiting list.");
        }, 5, TimeUnit.SECONDS);

        // Broadcast the updated waiting list to all clients
        broadcastWaitingList();

        session.getPlayerOne().sendMessage("Waiting clients: " + getFormattedWaitingClientsList());
        session.getPlayerTwo().sendMessage("Waiting clients: " + getFormattedWaitingClientsList());
    }

    public static void removeGameSessionDisconnected(GameSession session) {
        synchronized (activeGames) {
            // Remove the game session from the active list
            if (activeGames.remove(session)) {
                System.out.println("GameSession removed successfully.");
            } else {
                System.out.println("GameSession not found in the active list.");
            }
        }

        // Clear the players' references to the game session
        session.getPlayerOne().setGameSession(null);
        session.getPlayerTwo().setGameSession(null);


    }



    private static String getFormattedWaitingClientsList() {
        return waitingClients.stream()
                .map(ClientHandler::getUsername)  // assuming getUsername() is available
                .collect(Collectors.joining(";"));
    }

    public static void handleDisconnection(ClientHandler disconnectedClient) {
        synchronized (activeGames) {
            // Check if the client is in the waiting list
            synchronized (waitingClients) {
                if (waitingClients.contains(disconnectedClient)) {
                    waitingClients.remove(disconnectedClient);

                    System.out.println(disconnectedClient.getUsername() + " removed from waitingClients.");

                }

                broadcastWaitingList();

            }


            // Also remove from loggedInUsers
            synchronized (loggedInUsers) {
                loggedInUsers.remove(disconnectedClient.getUsername());
                System.out.println(disconnectedClient.getUsername() + " removed from loggedInUsers.");

                broadcastWaitingList();
            }

            // Handle the case where the client was already in a game
            GameSession session = findActiveSession(disconnectedClient);
            if (session != null) {
                // If the player was in a game, handle the disconnection
                if (session.getPlayerOne().equals(disconnectedClient)) {
                    session.playerDisconnected(disconnectedClient);  // Mark player one as disconnected
                } else if (session.getPlayerTwo().equals(disconnectedClient)) {
                    session.playerDisconnected(disconnectedClient);  // Mark player two as disconnected
                }

                // If both players are disconnected, terminate the session
                if (session.isBothPlayersDisconnected()) {

                    session.endGameSession();  // Handle session cleanup and logging

                    System.out.println("Both players disconnected. Game session terminated.");
                    broadcastWaitingList();

                } else {
                    // If only one player disconnected, notify the other player
                    ClientHandler opponent = session.getOpponent(disconnectedClient);
                    if (opponent != null) {
                        opponent.sendMessage(disconnectedClient.getUsername() + " has disconnected. The game will resume when they reconnect.");
                    }

                    broadcastWaitingList();

                    // Move session to disconnectedSessions for potential reconnection
                    disconnectedSessions.put(disconnectedClient.getUsername(), session);
                    System.out.println("Game session moved to disconnectedSessions for " + disconnectedClient.getUsername());
                }
            }
        }
    }

    public static void handleReconnect(ClientHandler reconnectedClient) {
        synchronized (disconnectedSessions) {
            GameSession session = disconnectedSessions.remove(reconnectedClient.getUsername());

            if (session != null) {
                // Restore the session to active games
                activeGames.add(session);

                // Assign the reconnected client back to the session
                session.reconnectPlayer(reconnectedClient);

                // Notify both players that the game has resumed
                session.getPlayerOne().sendMessage("The game has resumed!");
                session.getPlayerTwo().sendMessage("The game has resumed!");

                System.out.println("Game session resumed for " + reconnectedClient.getUsername());
            } else {
                // If no disconnected session, add the client to the waiting list
                addToWaitingList(reconnectedClient);
            }
        }

        new Thread(reconnectedClient).start();
    }


    private static GameSession findActiveSession(ClientHandler client) {
        for (GameSession session : activeGames) {
            if (session.containsPlayer(client)) {
                return session;
            }
        }
        return null;
    }

    public static void addToWaitingList(ClientHandler clientHandler) {
        synchronized (waitingClients) {
            if (!waitingClients.contains(clientHandler)) {
                waitingClients.add(clientHandler);
                broadcastWaitingList();
            }
        }
    }

    public static String sendHistoryBoard() throws IOException {
        List<String> historyBoard = PlayerDataManager.loadPlayerHistory();

        if (historyBoard.isEmpty()) {
            return "GAME HISTORY: No history available.";
        }

        StringBuilder historyData = new StringBuilder();
        historyData.append("GAME HISTORY:");

        // Use a loop to append each line, avoid adding a semicolon after the last line
        for (int i = 0; i < historyBoard.size(); i++) {
            historyData.append(historyBoard.get(i));
            if (i < historyBoard.size() - 1) {
                historyData.append("; ");
            }
        }

        return historyData.toString();
    }

    public static String sendPlayerStatus() throws IOException {

        if (loggedInUsers == null || loggedInUsers.isEmpty()) {
            return "PLAYERS STATUS: No players available.";
        }

        StringBuilder playerDataString = new StringBuilder();
        playerDataString.append("PLAYERS STATUS:");

        for (String user : loggedInUsers) {
            playerDataString.append(user).append(";");
        }

        return playerDataString.toString();
    }

}
