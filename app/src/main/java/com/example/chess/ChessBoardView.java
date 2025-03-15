package com.example.chess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChessBoardView extends View implements ChessGame.OnPawnPromotionListener{
    private static final int BOARD_SIZE = 8;
    private Paint paint;
    private int cellSize;
    private ChessGame game;
    private Map<String, Bitmap> pieceBitmaps;
    private int selectedRow = -1, selectedCol = -1;
    private List<int[]> validMoves = new ArrayList<>();

    private float pulseRadiusFactor = 0.2f;
    private boolean increasing = true;
    private Handler handler = new Handler();

    int lightColor = Color.parseColor("#E8EDF9");
    int darkColor = Color.parseColor("#B7C0D8");
    int lightColorActive = Color.parseColor("#b1a6fc");
    int darkColorActive = Color.parseColor("#9890ec");
    int hintColor = Color.parseColor("#b0a3f2");

    public ChessBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        game = new ChessGame();
        pieceBitmaps = new HashMap<>();
        loadPieceImages();
        startPulseAnimation();
        game.setOnPawnPromotionListener(this);
    }

    private void loadPieceImages() {
        int[] pieceIds = {
                R.drawable.blackbishop, R.drawable.blackking, R.drawable.blackknight, R.drawable.blackpawn, R.drawable.blackqueen, R.drawable.blackrook,
                R.drawable.whitebishop, R.drawable.whiteking, R.drawable.whiteknight, R.drawable.whitepawn, R.drawable.whitequeen, R.drawable.whiterook
        };
        String[] pieceNames = {
                "blackbishop", "blackking", "blackknight", "blackpawn", "blackqueen", "blackrook",
                "whitebishop", "whiteking", "whiteknight", "whitepawn", "whitequeen", "whiterook"
        };

        for (int i = 0; i < pieceNames.length; i++) {
            pieceBitmaps.put(pieceNames[i], BitmapFactory.decodeResource(getResources(), pieceIds[i]));
        }
    }

    private void startPulseAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (increasing) {
                    pulseRadiusFactor += 0.01f;
                    if (pulseRadiusFactor >= 0.15f) {
                        increasing = false;
                    }
                } else {
                    pulseRadiusFactor -= 0.01f;
                    if (pulseRadiusFactor <= 0.08f) {
                        increasing = true;
                    }
                }
                invalidate();
                handler.postDelayed(this, 50);
            }

        }, 50);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        int boardLength = Math.min(screenWidth, screenHeight) * 9 / 10;
        cellSize = boardLength / BOARD_SIZE;
        int startX = (screenWidth - boardLength) / 2;
        int startY = (screenHeight - boardLength) / 2;

        // Find kings' positions and check status
        int[] whiteKingPos = game.findKingPosition("white");
        int[] blackKingPos = game.findKingPosition("black");
        boolean whiteInCheck = game.isKingInCheck("white");
        boolean blackInCheck = game.isKingInCheck("black");
        boolean whiteCheckmate = game.isCheckmate("white");
        boolean blackCheckmate = game.isCheckmate("black");
        boolean whiteStalemate = game.isStalemate("white");
        boolean blackStalemate = game.isStalemate("black");

        // Draw board
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean isSelected = (row == selectedRow && col == selectedCol);

                // Highlight king in red if in check
                if ((whiteInCheck && whiteKingPos != null && whiteKingPos[0] == row && whiteKingPos[1] == col) ||
                        (blackInCheck && blackKingPos != null && blackKingPos[0] == row && blackKingPos[1] == col)) {
                    paint.setColor(Color.RED); // Red highlight for checked king
                } else {
                    paint.setColor(isSelected ? ((row + col) % 2 == 0 ? lightColorActive : darkColorActive)
                            : ((row + col) % 2 == 0 ? lightColor : darkColor));
                }

                // Draw cell
                canvas.drawRect(startX + col * cellSize, startY + row * cellSize,
                        startX + (col + 1) * cellSize, startY + (row + 1) * cellSize, paint);

                // Draw piece
                String piece = game.getPieceAt(row, col);
                if (piece != null) {
                    Bitmap pieceBitmap = pieceBitmaps.get(piece);
                    if (pieceBitmap != null) {
                        canvas.drawBitmap(pieceBitmap, null,
                                new android.graphics.Rect(startX + col * cellSize, startY + row * cellSize,
                                        startX + (col + 1) * cellSize, startY + (row + 1) * cellSize), null);
                    }
                }
            }
        }







        // **Filter Valid Moves: Only Show Moves That Don't Leave King in Check**
        List<int[]> safeMoves = new ArrayList<>();
        for (int[] move : validMoves) {
            int fromRow = selectedRow;
            int fromCol = selectedCol;
            int toRow = move[0];
            int toCol = move[1];

            // Simulate move and check if the king is still in check
            if (game.isMoveSafe(fromRow, fromCol, toRow, toCol)) {
                safeMoves.add(move);
            }
        }

        // **Draw pulsating hint circles for filtered valid moves**
        for (int[] move : safeMoves) {
            int row = move[0];
            int col = move[1];

            boolean isCaptureMove = (game.getPieceAt(row, col) != null); // Check if opponent's piece is there

            paint.setColor(hintColor);
            paint.setStyle(Paint.Style.FILL);
            float radius = isCaptureMove ? cellSize * pulseRadiusFactor : cellSize * (pulseRadiusFactor - 0.05f); // Pulsating both
            canvas.drawCircle(
                    startX + col * cellSize + cellSize / 2,
                    startY + row * cellSize + cellSize / 2,
                    radius,
                    paint
            );
        }

        if (selectedRow != -1 && game.getPieceAt(selectedRow, selectedCol) != null &&
                game.getPieceAt(selectedRow, selectedCol).endsWith("king")) {

            boolean canCastleKingside = game.canCastle(game.isWhiteTurn() ? "white" : "black", true);
            boolean canCastleQueenside = game.canCastle(game.isWhiteTurn() ? "white" : "black", false);

            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.FILL);

            float castleRadius = cellSize * pulseRadiusFactor; // Pulsating effect

            if (canCastleKingside) {
                int rookCol = 7;
                canvas.drawCircle(
                        startX + rookCol * cellSize + cellSize / 2,
                        startY + selectedRow * cellSize + cellSize / 2,
                        castleRadius,
                        paint
                );
            }
            if (canCastleQueenside) {
                int rookCol = 0;
                canvas.drawCircle(
                        startX + rookCol * cellSize + cellSize / 2,
                        startY + selectedRow * cellSize + cellSize / 2,
                        castleRadius,
                        paint
                );
            }
        }


        // **Display checkmate or stalemate message**
        if (whiteCheckmate || blackCheckmate || whiteStalemate || blackStalemate) {
            String message;
            if (whiteCheckmate) {
                message = "Checkmate! Black Wins!";
            } else if (blackCheckmate) {
                message = "Checkmate! White Wins!";
            } else {
                message = "Stalemate! Draw!";
            }

            // Draw text
            paint.setColor(Color.BLACK);
            paint.setTextSize(80);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(message, screenWidth / 2, screenHeight / 2, paint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int screenWidth = getWidth();
            int screenHeight = getHeight();
            int boardLength = Math.min(screenWidth, screenHeight) * 9 / 10;
            int startX = (screenWidth - boardLength) / 2;
            int startY = (screenHeight - boardLength) / 2;

            int col = (int) ((event.getX() - startX) / cellSize);
            int row = (int) ((event.getY() - startY) / cellSize);

            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                handleTouch(row, col);
                invalidate();
            }
        }
        return true;
    }

    private void handleTouch(int row, int col) {
        if (selectedRow == -1 && selectedCol == -1) {
            String piece = game.getPieceAt(row, col);
            if (piece != null) {
                boolean isWhite = piece.startsWith("white");
                if ((game.isWhiteTurn() && isWhite) || (!game.isWhiteTurn() && !isWhite)) {
                    selectedRow = row;
                    selectedCol = col;
                    validMoves = game.getValidMoves(row, col);

                    // If selected piece is the king, check castling
                    if (piece.endsWith("king")) {
                        if (game.canCastle(isWhite ? "white" : "black", true)) {
                            validMoves.add(new int[]{row, 7}); // Select rook
                        }
                        if (game.canCastle(isWhite ? "white" : "black", false)) {
                            validMoves.add(new int[]{row, 0}); // Select rook
                        }
                    }
                }
            }
        } else {
            for (int[] move : validMoves) {
                if (move[0] == row && move[1] == col) {
                    String selectedPiece = game.getPieceAt(selectedRow, selectedCol);

                    // If clicked rook while king is selected, perform castling
                    if (selectedPiece.endsWith("king") && game.getPieceAt(row, col) != null &&
                            game.getPieceAt(row, col).endsWith("rook")) {

                        game.castle(game.isWhiteTurn() ? "white" : "black", col == 7);
                    } else {
                        game.movePiece(selectedRow, selectedCol, row, col);
                    }
                    break;
                }
            }
            selectedRow = -1;
            selectedCol = -1;
            validMoves.clear();
        }
        invalidate();
    }


    public void promotePawn(int row, int col, String newPiece) {
        game.setPieceAt(row, col, newPiece); // Replace pawn with selected piece
        invalidate(); // Redraw board
    }

    private void showPawnPromotionDialog(int row, int col, String color) {
        PawnPromotionDialog dialog = PawnPromotionDialog.newInstance(row, col, color);
        dialog.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "PawnPromotionDialog");
    }

    @Override
    public void onPawnPromotion(int row, int col, String color) {
        showPawnPromotionDialog(row, col, color); // Call method to show dialog
    }

}