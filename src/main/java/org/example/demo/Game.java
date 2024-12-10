package org.example.demo;
import java.io.PrintWriter;
//import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Game {
    public int[][] board;
    public int row;
    public int col;

    public Game(int[][] generatedBoard) {
        this.board = generatedBoard;
        this.row = generatedBoard.length;
        this.col = generatedBoard[0].length;

        System.out.println("Generated board size: " + this.row + "x" + this.col);

    }

    public int[] checkMatchPoint(int row1, int col1, int row2, int col2) {
        int[] points = new int[4];

        // [0] = false
        // [1] = one-line
        // [2] = two-line
        // [3] = three-line

        // Check if the selected positions are the same or different values
        // No match 0 point
        if ((board[row1][col1] != board[row2][col2]) || (row1 == row2 && col1 == col2)) {

            return points; // [0,0,0,0]
        }

        String connectionDetails = "";

        // One-line connection (5 point)
        if (isDirectlyConnected(row1, col1, row2, col2, board)) {
            connectionDetails = "One-line connection: (" + row1 + "," + col1 + ") -> (" + row2 + "," + col2 + ")";
            //updateScore(points);
            System.out.println(connectionDetails);
            points[1] = 5 ;

            return points; // [0,5,0,0]
        }

        // Two-line connection (10 points)
        if ((row1 != row2) && (col1 != col2)) {
            if (board[row1][col2] == 0 && isDirectlyConnected(row1, col1, row1, col2, board)
                    && isDirectlyConnected(row1, col2, row2, col2, board)) {
                connectionDetails = "Two-line connection: (" + row1 + "," + col1 + ") -> (" + row1 + "," + col2 + ") -> (" + row2 + "," + col2 + ")";
                //updateScore(points);
                System.out.println(connectionDetails);
                points[2] = 10 ;

                return points; // [0,5,0,0]


            }

            if (board[row2][col1] == 0 && isDirectlyConnected(row2, col2, row2, col1, board)
                    && isDirectlyConnected(row2, col1, row1, col1, board)) {
                connectionDetails = "Two-line connection: (" + row2 + "," + col2 + ") -> (" + row2 + "," + col1 + ") -> (" + row1 + "," + col1 + ")";
                //updateScore(points);
                System.out.println(connectionDetails);
                points[2] = 10 ;

                return points; //[0,5,0,0]

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
                    points[3] = 15 ;

                    return points; // [0,5,0,0]

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
                    points[3] = 15 ;

                    return points; // [0,5,0,0]

                }
            }
        }

        // If no connection is found, return false
        return points;
    }

    public boolean checkMatch(int row1, int col1, int row2, int col2) {

        // Check if the selected positions are the same or different values
        // No match 0 point

        if (row1 < 0 || row1 >= board.length || col1 < 0 || col1 >= board[0].length ||
                row2 < 0 || row2 >= board.length || col2 < 0 || col2 >= board[0].length) {
            throw new IndexOutOfBoundsException("Tile selection out of bounds.");
        }

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


    private boolean isDirectlyConnected(int row1, int col1, int row2, int col2, int[][] board) {
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

    public void updateBoardMatch(int row1, int col1, int row2, int col2){
        this.board[row1][col1] = 0;
        this.board[row2][col2] = 0;

    }
    public boolean hasPossibleMoves() {
        for (int row1 = 0; row1 < board.length; row1++) {
            for (int col1 = 0; col1 < board[row1].length; col1++) {
                if (board[row1][col1] == 0) continue; // Skip empty tiles

                for (int row2 = 0; row2 < board.length; row2++) {
                    for (int col2 = 0; col2 < board[row2].length; col2++) {
                        if ((row1 != row2 || col1 != col2) && board[row1][col1] == board[row2][col2]) {
                            if (checkMatch(row1, col1, row2, col2)) {
                                return true; // Found a possible match
                            }
                        }
                    }
                }
            }
        }
        return false; // No matches found
    }


}
