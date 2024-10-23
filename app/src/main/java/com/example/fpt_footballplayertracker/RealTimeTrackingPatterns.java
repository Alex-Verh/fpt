package com.example.fpt_footballplayertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class RealTimeTrackingPatterns extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.realtracking_patterns);

        // Remove the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // NAVIGATION BUTTONS
        ImageButton returnBtn = findViewById(R.id.back_button);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RealTimeTrackingPatterns.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button positioningBtn = findViewById(R.id.tab_positioning);
        positioningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RealTimeTrackingPatterns.this, RealTimeTrackingPositioning.class);
                startActivity(intent);
            }
        });

        // END NAVIGATION BUTTONS

    }
}