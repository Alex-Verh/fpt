package com.example.fpt_footballplayertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class PostGamePatterns extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postgame_patterns);

        // Remove the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // NAVIGATION BUTTONS
        ImageButton returnBtn = findViewById(R.id.back_button);
        Button statisticsBtn = findViewById(R.id.tab_statistics);

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostGamePatterns.this, MainActivity.class);
                startActivity(intent);
            }
        });
        statisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostGamePatterns.this, PostGameStatistics.class);
                startActivity(intent);
            }
        });

        // END NAVIGATION BUTTONS

    }
}