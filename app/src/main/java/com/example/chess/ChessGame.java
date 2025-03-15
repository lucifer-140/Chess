package com.example.chess;

import java.util.List;
import java.util.ArrayList;

public class ChessGame {
    public static final int BOARD_SIZE = 8;
    private String[][] board;
    private boolean isWhiteTurn = true;
    private boolean whiteInCheck = false;
    private boolean blackInCheck = false;

    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookLeftMoved = false, whiteRookRightMoved = false;
    private boolean blackRookLeftMoved = false, blackRookRightMoved = false;
    private boolean whiteRookKingsideMoved = false, whiteRookQueensideMoved = false;
    private boolean blackRookKingsideMoved = false, blackRookQueensideMoved = false;



    private OnPawnPromotionListener promotionListener;

    public interface OnPawnPromotionListener {
        void onPawnPromotion(int row, int col, String color);
    }

    public void setOnPawnPromotionListener(OnPawnPromotionListener listener) {
        this.promotionListener = listener;
    }

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

            // **Pawn Promotion Check**
            if ((piece.equals("whitepawn") && toRow == 0) || (piece.equals("blackpawn") && toRow == 7)) {
                if (promotionListener != null) {
                    promotionListener.onPawnPromotion(toRow, toCol, isWhite ? "white" : "black");
                }
            }

            if (piece.endsWith("king")) {
                if (isWhiteTurn) whiteKingMoved = true;
                else blackKingMoved = true;
            }
            if (piece.endsWith("rook")) {
                if (fromRow == 7 && fromCol == 7) whiteRookKingsideMoved = true;  // White kingside rook
                if (fromRow == 7 && fromCol == 0) whiteRookQueensideMoved = true; // White queenside rook
                if (fromRow == 0 && fromCol == 7) blackRookKingsideMoved = true;  // Black kingside rook
                if (fromRow == 0 && fromCol == 0) blackRookQueensideMoved = true; // Black queenside rook
            }



            isWhiteTurn = !isWhiteTurn; // Switch turn
        }
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

    public boolean canCastle(String color, boolean kingside) {
        if (color.equals("white")) {
            if (whiteKingMoved) return false;
            if (kingside && whiteRookKingsideMoved) return false;
            if (!kingside && whiteRookQueensideMoved) return false;
        } else {
            if (blackKingMoved) return false;
            if (kingside && blackRookKingsideMoved) return false;
            if (!kingside && blackRookQueensideMoved) return false;
        }

        // Ensure no pieces in between
        int row = color.equals("white") ? 7 : 0;
        int start = kingside ? 5 : 1;
        int end = kingside ? 6 : 3;

        for (int col = start; col <= end; col++) {
            if (getPieceAt(row, col) != null) return false;
        }

        // Ensure king does not move through check
        if (isKingInCheck(color) || !isMoveSafe(row, 4, row, kingside ? 6 : 2)) {
            return false;
        }

        return true;
    }

    public void castle(String color, boolean kingside) {
        int row = color.equals("white") ? 7 : 0;
        if (!canCastle(color, kingside)) return;

        // Move king
        setPieceAt(row, 4, null);
        setPieceAt(row, kingside ? 6 : 2, color + "king");

        // Move rook
        setPieceAt(row, kingside ? 7 : 0, null);
        setPieceAt(row, kingside ? 5 : 3, color + "rook");

        // Mark king and rook as moved
        if (color.equals("white")) {
            whiteKingMoved = true;
            if (kingside) whiteRookRightMoved = true;
            else whiteRookLeftMoved = true;
        } else {
            blackKingMoved = true;
            if (kingside) blackRookRightMoved = true;
            else blackRookLeftMoved = true;
        }

        // End turn immediately
        isWhiteTurn = !isWhiteTurn;

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