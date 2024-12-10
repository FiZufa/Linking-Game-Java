package org.example.demo;

import java.io.*;
import java.util.*;

public class PlayerDataManager {
    // Path to the file relative to the project directory
    private static final String RESOURCE_FILE = "src/main/resources/org/example/demo/playerdata.txt";
    private static final String HISTORY_FILE = "src/main/resources/org/example/demo/playerHistory.txt";
    public static Map<String, String> loadPlayerData() throws IOException {
        Map<String, String> playerData = new HashMap<>();
        File file = new File(RESOURCE_FILE);

        // Ensure the file exists
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + RESOURCE_FILE);
        }

        // Read data from the file
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 2) {
                    playerData.put(data[0].trim(), data[1].trim());
                }
            }
        }

        return playerData;
    }

    public static Map<String, String> loadPlayerStatus() throws IOException {
        Map<String, String> playerStatus = new HashMap<>();

        File file = new File(RESOURCE_FILE);

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + RESOURCE_FILE);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) {
                    playerStatus.put(data[0].trim(), data[2].trim());
                }
            }
        }

        return playerStatus;
    }

    public static List<String> loadPlayerHistory() throws IOException {
        List<String> playerHistory = new ArrayList<>();

        File file = new File(HISTORY_FILE);

        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + HISTORY_FILE);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                playerHistory.add(line);
            }
        }

        return playerHistory;
    }

    public static void savePlayerData(Map<String, String> playerData) throws IOException {
        File file = new File(RESOURCE_FILE);

        // Overwrite player data in the file (no append needed here)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<String, String> entry : playerData.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        }
    }

    public static void saveGameHistoryData(List<String> gameHistoryData) throws IOException {
        File file = new File(HISTORY_FILE);

        // Use append mode to ensure we're not overwriting the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String history : gameHistoryData) {
                writer.write(history);  // Write each history string
                writer.newLine();       // Add a newline after each entry
            }
        }
    }


    public static void addMatchHistory(String newHistory) throws IOException {
        List<String> existingHistoryData = loadPlayerHistory();

        Set<String> historySet = new HashSet<>(existingHistoryData);

        // Add the new history entry to the Set
        historySet.add(newHistory);

        // Convert the Set back to a List (since your save method works with a List)
        List<String> updatedHistoryData = new ArrayList<>(historySet);

        // Save the updated history data to the file
        saveGameHistoryData(updatedHistoryData);

    }


}
