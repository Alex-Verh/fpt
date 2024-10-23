package com.example.fpt_footballplayertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class RealTimeTrackingPositioning extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.realtracking_positioning);


        // NAVIGATION BUTTONS
        ImageButton returnBtn = findViewById(R.id.back_button);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RealTimeTrackingPositioning.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button patternsBtn = findViewById(R.id.tab_patterns);
        patternsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RealTimeTrackingPositioning.this, RealTimeTrackingPatterns.class);
                startActivity(intent);
            }
        });

        // END NAVIGATION BUTTONS
    }
}