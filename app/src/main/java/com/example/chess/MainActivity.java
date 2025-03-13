package com.example.chess;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1v1 = findViewById(R.id.btn_1v1);
        Button btn1vAI = findViewById(R.id.btn_1vAI);

        btn1v1.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        btn1vAI.setOnClickListener(v -> {
            // Later, add an AI selection screen before starting the game
            Intent intent = new Intent(MainActivity.this, AISelectionActivity.class);
            startActivity(intent);
        });
    }
}
