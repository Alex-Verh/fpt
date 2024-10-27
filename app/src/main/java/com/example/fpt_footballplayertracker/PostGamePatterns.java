package com.example.fpt_footballplayertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.FrameLayout;

import java.util.Random;

public class PostGamePatterns extends AppCompatActivity {

    private FrameLayout footballPitch;

    @SuppressLint("WrongViewCast")
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



//        Randomizing movement patterns
        footballPitch = findViewById(R.id.football_pitch);

        footballPitch.post(() -> {
            int containerWidth = footballPitch.getWidth();
            int containerHeight = footballPitch.getHeight();

            for (int i = 0; i < 25; i++) {
                addRandomMarker(containerWidth, containerHeight);
            }
        });
    }

    private void addRandomMarker(int maxWidth, int maxHeight) {
        // Create a new View (marker)
        View marker = new View(this);
        marker.setLayoutParams(new FrameLayout.LayoutParams(45, 100)); // Width and height of marker
        marker.setBackgroundResource(R.drawable.movement_pattern_arrow);

        // Define the area within the ImageView (e.g., 80% of the width and height, centered)
        float widthLimit = 0.7f * maxWidth;
        float heightLimit = 0.7f * maxHeight;

        // Calculate starting points to center the markers within this limited area
        float xStart = (maxWidth - widthLimit) / 2;
        float yStart = (maxHeight - heightLimit) / 2;

        // Generate random x and y within the limited width and height range
        Random random = new Random();
        float randomX = xStart + random.nextFloat() * widthLimit;
        float randomY = yStart + random.nextFloat() * heightLimit;

        // Generate a random rotation angle between 0 and 360 degrees
        float randomRotation = random.nextInt(360);
        marker.setRotation(randomRotation);  // Apply the random rotation

        // Position the marker
        marker.setX(randomX);
        marker.setY(randomY);

        // Add the marker to the FrameLayout (overlaying the ImageView)
        footballPitch.addView(marker);
    }

}