package com.example.chess;

import java.util.List;
import java.util.ArrayList;

public class ChessGame {
    public static final int BOARD_SIZE = 8;
    private String[][] board;
    private boolean isWhiteTurn = true;

    public ChessGame() {
        board = new String[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
    }

    private void initializeBoard() {
        // Place pawns
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = "blackpawn";
            board[6][i] = "whitepawn";
        }

        // Place other pieces
        String[] blackBackRow = {"blackrook", "blackknight", "blackbishop", "blackqueen", "blackking", "blackbishop", "blackknight", "blackrook"};
        String[] whiteBackRow = {"whiterook", "whiteknight", "whitebishop", "whitequeen", "whiteking", "whitebishop", "whiteknight", "whiterook"};

        for (int i = 0; i < BOARD_SIZE; i++) {
            board[0][i] = blackBackRow[i];
            board[7][i] = whiteBackRow[i];
        }
    }

    public String getPieceAt(int row, int col) {
        return board[row][col];
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }


    public List<int[]> getValidMoves(int row, int col) {
        List<int[]> validMoves = new ArrayList<>();
        String piece = board[row][col];

        if (piece == null) return validMoves;

        boolean isWhite = piece.startsWith("white");

        switch (piece) {
            case "blackpawn":
            case "whitepawn":
                validMoves.addAll(getPawnMoves(row, col, isWhite));
                break;
            case "blackrook":
            case "whiterook":
                validMoves.addAll(getRookMoves(row, col, isWhite));
                break;
            case "blackknight":
            case "whiteknight":
                validMoves.addAll(getKnightMoves(row, col, isWhite));
                break;
            case "blackbishop":
            case "whitebishop":
                validMoves.addAll(getBishopMoves(row, col, isWhite));
                break;
            case "blackqueen":
            case "whitequeen":
                validMoves.addAll(getQueenMoves(row, col, isWhite));
                break;
            case "blackking":
            case "whiteking":
                validMoves.addAll(getKingMoves(row, col, isWhite));
                break;
        }

        return validMoves;
    }

    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        List<int[]> validMoves = getValidMoves(fromRow, fromCol);
        for (int[] move : validMoves) {
            if (move[0] == toRow && move[1] == toCol) {
                return true;
            }
        }
        return false;
    }

    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        String piece = board[fromRow][fromCol];
        if (piece == null) return;

        boolean isWhite = piece.startsWith("white");

        // Enforce turn rules
        if ((isWhiteTurn && !isWhite) || (!isWhiteTurn && isWhite)) {
            return;
        }

        if (isValidMove(fromRow, fromCol, toRow, toCol)) {
            // Simulate move
            String capturedPiece = board[toRow][toCol];
            board[toRow][toCol] = board[fromRow][fromCol]; // Move piece
            board[fromRow][fromCol] = null; // Clear old position

            // Check if move puts own king in check
            String playerColor = isWhite ? "white" : "black";
            if (isKingInCheck(playerColor)) {
                // Undo move if king is in check
                board[fromRow][fromCol] = board[toRow][toCol];
                board[toRow][toCol] = capturedPiece;
                return;
            }

            isWhiteTurn = !isWhiteTurn; // Switch turn
        }
    }


    public int[] findKingPosition(String playerColor) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String piece = board[row][col];
                if (piece != null && piece.equals(playerColor + "king")) {
                    return new int[]{row, col};
                }
            }
        }
        return null; // Should never happen unless the king is missing
    }

    public boolean isKingInCheck(String playerColor) {
        int[] kingPos = findKingPosition(playerColor);
        if (kingPos == null) return false;

        // Check if any enemy piece can move to the king's position
        String opponentColor = playerColor.equals("white") ? "black" : "white";

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String piece = board[row][col];
                if (piece != null && piece.startsWith(opponentColor)) {
                    List<int[]> moves = getValidMoves(row, col);
                    for (int[] move : moves) {
                        if (move[0] == kingPos[0] && move[1] == kingPos[1]) {
                            return true; // King is in check
                        }
                    }
                }
            }
        }
        return false;
    }



    private List<int[]> getPawnMoves(int row, int col, boolean isWhite) {
        List<int[]> moves = new ArrayList<>();
        int direction = isWhite ? -1 : 1;
        int startRow = isWhite ? 6 : 1;

        // Forward move
        if (isInBounds(row + direction, col) && board[row + direction][col] == null) {
            moves.add(new int[]{row + direction, col});

            // Double move if at starting row
            if (row == startRow && board[row + 2 * direction][col] == null) {
                moves.add(new int[]{row + 2 * direction, col});
            }
        }

        // Capture moves (diagonal)
        int[][] attackOffsets = {{direction, -1}, {direction, 1}};
        for (int[] offset : attackOffsets) {
            int newRow = row + offset[0], newCol = col + offset[1];
            if (isInBounds(newRow, newCol) && isEnemy(isWhite, board[newRow][newCol])) {
                moves.add(new int[]{newRow, newCol});
            }
        }

        return moves;
    }

    private List<int[]> getRookMoves(int row, int col, boolean isWhite) {
        return getLinearMoves(row, col, isWhite, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
    }

    private List<int[]> getBishopMoves(int row, int col, boolean isWhite) {
        return getLinearMoves(row, col, isWhite, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
    }

    private List<int[]> getQueenMoves(int row, int col, boolean isWhite) {
        List<int[]> moves = new ArrayList<>();
        moves.addAll(getRookMoves(row, col, isWhite));
        moves.addAll(getBishopMoves(row, col, isWhite));
        return moves;
    }

    private List<int[]> getKingMoves(int row, int col, boolean isWhite) {
        List<int[]> moves = new ArrayList<>();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dir : directions) {
            int newRow = row + dir[0], newCol = col + dir[1];
            if (isInBounds(newRow, newCol) && !isFriendly(isWhite, board[newRow][newCol])) {
                moves.add(new int[]{newRow, newCol});
            }
        }

        return moves;
    }

    private List<int[]> getKnightMoves(int row, int col, boolean isWhite) {
        List<int[]> moves = new ArrayList<>();
        int[][] movesSet = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};

        for (int[] move : movesSet) {
            int newRow = row + move[0], newCol = col + move[1];
            if (isInBounds(newRow, newCol) && !isFriendly(isWhite, board[newRow][newCol])) {
                moves.add(new int[]{newRow, newCol});
            }
        }

        return moves;
    }

    private List<int[]> getLinearMoves(int row, int col, boolean isWhite, int[][] directions) {
        List<int[]> moves = new ArrayList<>();

        for (int[] dir : directions) {
            int newRow = row + dir[0], newCol = col + dir[1];

            while (isInBounds(newRow, newCol)) {
                if (board[newRow][newCol] == null) {
                    moves.add(new int[]{newRow, newCol});
                } else {
                    if (isEnemy(isWhite, board[newRow][newCol])) {
                        moves.add(new int[]{newRow, newCol});
                    }
                    break;
                }
                newRow += dir[0];
                newCol += dir[1];
            }
        }

        return moves;
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private boolean isFriendly(boolean isWhite, String piece) {
        return piece != null && piece.startsWith(isWhite ? "white" : "black");
    }

    private boolean isEnemy(boolean isWhite, String piece) {
        return piece != null && piece.startsWith(isWhite ? "black" : "white");
    }
}
