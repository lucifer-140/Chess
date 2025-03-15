package com.example.chess;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    private ChessBoardView chessBoardView;
    private ImageView backgroundImage; // Add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game); // Ensure this matches your XML layout file

        // Initialize the ChessBoardView and ImageView
        chessBoardView = findViewById(R.id.chessBoardView);
        backgroundImage = findViewById(R.id.backgroundImage); // Add this line

        // Pass the backgroundImage to ChessBoardView
        chessBoardView.setBackgroundImage(backgroundImage); // Add this line
    }
}