package com.example.chess;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ChessGame {
    public static final int BOARD_SIZE = 8;
    private String[][] board;
    private boolean[][] hasMoved;
    private Map<String, Boolean> moveHistory = new HashMap<>();
    private boolean isWhiteTurn = true;
    private boolean whiteInCheck = false;
    private boolean blackInCheck = false;

    private OnPawnPromotionListener promotionListener;

    public interface OnPawnPromotionListener {
        void onPawnPromotion(int row, int col, String color);
    }

    public void setOnPawnPromotionListener(OnPawnPromotionListener listener) {
        this.promotionListener = listener;
    }

    public ChessGame() {
        board = new String[BOARD_SIZE][BOARD_SIZE];
        hasMoved = new boolean[BOARD_SIZE][BOARD_SIZE];
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

    public boolean hasPieceMoved(int row, int col) {
        return hasMoved[row][col];
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

        // Check if the move is normally valid
        boolean foundMove = false;
        for (int[] move : validMoves) {
            if (move[0] == toRow && move[1] == toCol) {
                foundMove = true;
                break;
            }
        }

        if (!foundMove) return false;

        // Simulate the move
        String piece = board[fromRow][fromCol];
        String capturedPiece = board[toRow][toCol];

        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;

        boolean kingStillInCheck = isKingInCheck(piece.startsWith("white") ? "white" : "black");

        // Undo move
        board[fromRow][fromCol] = piece;
        board[toRow][toCol] = capturedPiece;

        return !kingStillInCheck; // Move is only valid if king is not left in check
    }


    public void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        String piece = board[fromRow][fromCol];
        if (piece == null) return;

        boolean isWhite = piece.startsWith("white");

        // Enforce turn rules
        if ((isWhiteTurn && !isWhite) || (!isWhiteTurn && isWhite)) {
            return;
        }

        // **Handle Castling Move**
        if (piece.equals("whiteking") || piece.equals("blackking")) {
            int colDiff = toCol - fromCol;
            if (Math.abs(colDiff) == 2) { // King moved two squares → Castling
                boolean isKingSide = colDiff > 0;
                int rookCol = isKingSide ? 7 : 0;
                int newRookCol = isKingSide ? toCol - 1 : toCol + 1;

                // Move the King
                board[toRow][toCol] = piece;
                board[fromRow][fromCol] = null;

                // Move the Rook
                board[toRow][newRookCol] = board[fromRow][rookCol];
                board[fromRow][rookCol] = null;

                // Update movement tracking
                hasMoved[toRow][toCol] = true;
                hasMoved[toRow][newRookCol] = true;

                Log.d("CASTLING", (isWhite ? "White" : "Black") + " castled " + (isKingSide ? "King-side" : "Queen-side"));
                isWhiteTurn = !isWhiteTurn;
                return;
            }
        }

        // **Regular Move Handling**
        if (isValidMove(fromRow, fromCol, toRow, toCol)) {
            String capturedPiece = board[toRow][toCol];

            boolean movedBefore = hasMoved[fromRow][fromCol];

            if (capturedPiece != null) {
                if (capturedPiece.startsWith("white")) {
                    capturedWhitePieces.add(capturedPiece);
                } else {
                    capturedBlackPieces.add(capturedPiece);
                }
            }

            // Move piece
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = null;
            hasMoved[toRow][toCol] = true;

            Log.d("ChessMove", piece + " moved from (" + fromRow + "," + fromCol + ") to (" + toRow + "," + toCol + "). " +
                    "Has moved before? " + movedBefore);

            // Check if move puts own king in check
            String playerColor = isWhite ? "white" : "black";
            if (isKingInCheck(playerColor)) {
                // Undo move if king is in check
                board[fromRow][fromCol] = board[toRow][toCol];
                board[toRow][toCol] = capturedPiece;
                return;
            }

            // Pawn Promotion Check
            if ((piece.equals("whitepawn") && toRow == 0) || (piece.equals("blackpawn") && toRow == 7)) {
                if (promotionListener != null) {
                    promotionListener.onPawnPromotion(toRow, toCol, isWhite ? "white" : "black");
                }
            }

            isWhiteTurn = !isWhiteTurn; // Switch turn
        }
    }


    private List<String> capturedWhitePieces = new ArrayList<>();
    private List<String> capturedBlackPieces = new ArrayList<>();


    public List<String> getCapturedWhitePieces() {
        return new ArrayList<>(capturedWhitePieces);
    }

    public List<String> getCapturedBlackPieces() {
        return new ArrayList<>(capturedBlackPieces);
    }


    public void setPieceAt(int row, int col, String newPiece) {
        board[row][col] = newPiece;
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




    public boolean isMoveSafe(int fromRow, int fromCol, int toRow, int toCol) {
        // Simulate the move
        String piece = board[fromRow][fromCol];
        String capturedPiece = board[toRow][toCol];

        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;

        // Check if the king is still in check
        boolean kingStillInCheck = isKingInCheck(piece.startsWith("white") ? "white" : "black");

        // Undo move
        board[fromRow][fromCol] = piece;
        board[toRow][toCol] = capturedPiece;

        return !kingStillInCheck; // Move is safe if king is not left in check
    }


    public boolean isKingInCheck(String playerColor) {
        int[] kingPos = findKingPosition(playerColor);
        if (kingPos == null) return false;

        String opponentColor = playerColor.equals("white") ? "black" : "white";


        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String piece = board[row][col];
                if (piece != null && piece.startsWith(opponentColor)) {
                    List<int[]> moves = getValidMoves(row, col);
                    for (int[] move : moves) {
                        if (move[0] == kingPos[0] && move[1] == kingPos[1]) {
                            if (playerColor.equals("white")) {
                                whiteInCheck = true;
                            } else {
                                blackInCheck = true;
                            }
                            return true;
                        }
                    }
                }
            }
        }

        // Reset check state if no check is found
        if (playerColor.equals("white")) {
            whiteInCheck = false;
        } else {
            blackInCheck = false;
        }

        return false;
    }

    public boolean isCheckmate(String playerColor) {
        if (!isKingInCheck(playerColor)) {
            return false; // Not in check, so not checkmate
        }

        // Loop through all pieces of the player
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String piece = board[row][col];
                if (piece != null && piece.startsWith(playerColor)) {
                    List<int[]> moves = getValidMoves(row, col);

                    // Check if any move is safe
                    for (int[] move : moves) {
                        int toRow = move[0];
                        int toCol = move[1];

                        if (isMoveSafe(row, col, toRow, toCol)) {
                            return false; // At least one legal move exists, so not checkmate
                        }
                    }
                }
            }
        }

        return true; // No valid moves left, checkmate
    }

    public boolean isStalemate(String playerColor) {
        if (isKingInCheck(playerColor)) {
            return false; // If king is in check, it's not stalemate
        }

        // Loop through all pieces of the player
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String piece = board[row][col];
                if (piece != null && piece.startsWith(playerColor)) {
                    List<int[]> moves = getValidMoves(row, col);

                    // Check if any move is safe
                    for (int[] move : moves) {
                        int toRow = move[0];
                        int toCol = move[1];

                        if (isMoveSafe(row, col, toRow, toCol)) {
                            return false; // At least one legal move exists, so not stalemate
                        }
                    }
                }
            }
        }

        return true; // No valid moves left, stalemate
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

        // Castling logic
        if (canCastle(row, col, isWhite, true)) { // Kingside
            moves.add(new int[]{row, col + 2});
        }
        if (canCastle(row, col, isWhite, false)) { // Queenside
            moves.add(new int[]{row, col - 2});
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


    private boolean hasMoved(int row, int col) {
        String key = row + "," + col;
        return moveHistory.getOrDefault(key, false);
    }

    private void markAsMoved(int row, int col) {
        String key = row + "," + col;
        moveHistory.put(key, true);
    }

    public boolean canCastle(int row, int col, boolean isWhite, boolean isKingSide) {
        int direction = isKingSide ? 1 : -1;
        int rookCol = isKingSide ? 7 : 0;

        if (hasMoved(row, col)) {
//            Log.d("CASTLING", "King has moved, castling not possible.");
            return false;
        }

        if (hasMoved(row, rookCol)) {
//            Log.d("CASTLING", "Rook has moved, castling not possible.");
            return false;
        }

        // Check if the path between King and Rook is clear
        for (int i = col + direction; i != rookCol; i += direction) {
            if (board[row][i] != null) {
//                Log.d("CASTLING", "Path blocked at (" + row + ", " + i + "), castling not possible.");
                return false;
            }
        }

        // Check if King is in check or will pass through check
        if (isUnderCheck(row, col, isWhite) || isUnderCheck(row, col + direction, isWhite) || isUnderCheck(row, col + 2 * direction, isWhite)) {
//            Log.d("CASTLING", "King is in check or will move through check, castling not possible.");
            return false;
        }

        Log.d("CASTLING", "Castling is possible on " + (isKingSide ? "King's side" : "Queen's side") + " for " + (isWhite ? "White" : "Black"));
        return true;
    }



    private boolean isUnderCheck(int row, int col, boolean isWhite) {
        String opponentColor = isWhite ? "black" : "white";

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                String piece = board[i][j];
                if (piece != null && piece.startsWith(opponentColor)) {
                    // ⚠️ Directly check king moves to avoid recursion!
                    List<int[]> moves;
                    if (piece.endsWith("king")) {
                        moves = getBasicKingMoves(i, j); // Use a non-recursive method
                    } else {
                        moves = getValidMoves(i, j); // Normal valid moves
                    }

                    for (int[] move : moves) {
                        if (move[0] == row && move[1] == col) {
                            return true; // Square is attacked
                        }
                    }
                }
            }
        }

        return false; // Square is safe
    }

    private List<int[]> getBasicKingMoves(int row, int col) {
        List<int[]> moves = new ArrayList<>();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dir : directions) {
            int newRow = row + dir[0], newCol = col + dir[1];
            if (isInBounds(newRow, newCol)) {
                moves.add(new int[]{newRow, newCol});
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