package com.example.chess;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PawnPromotionDialog extends DialogFragment {

    private int row, col;
    private String color;

    public static PawnPromotionDialog newInstance(int row, int col, String color) {
        PawnPromotionDialog fragment = new PawnPromotionDialog();
        Bundle args = new Bundle();
        args.putInt("row", row);
        args.putInt("col", col);
        args.putString("color", color);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            row = getArguments().getInt("row");
            col = getArguments().getInt("col");
            color = getArguments().getString("color");
        }

        // Inflate custom layout
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pawn_promotion, null);

        // Get ImageViews from layout
        ImageView queen = view.findViewById(R.id.promotion_queen);
        ImageView rook = view.findViewById(R.id.promotion_rook);
        ImageView bishop = view.findViewById(R.id.promotion_bishop);
        ImageView knight = view.findViewById(R.id.promotion_knight);

        // Set images dynamically based on color
        if (color.equals("black")) {
            queen.setImageResource(R.drawable.blackqueen);
            rook.setImageResource(R.drawable.blackrook);
            bishop.setImageResource(R.drawable.blackbishop);
            knight.setImageResource(R.drawable.blackknight);
        }

        // Set click listeners for each piece
        queen.setOnClickListener(v -> selectPiece("queen"));
        rook.setOnClickListener(v -> selectPiece("rook"));
        bishop.setOnClickListener(v -> selectPiece("bishop"));
        knight.setOnClickListener(v -> selectPiece("knight"));

        // Create dialog
        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false); // Prevent closing on outside touch
        return dialog;
    }

    private void selectPiece(String pieceName) {
        String newPiece = color + pieceName.toLowerCase();
        ((ChessBoardView) getActivity().findViewById(R.id.chessBoardView)).promotePawn(row, col, newPiece);
        dismiss(); // Close dialog after selection
    }
}
