package com.example.fpt_footballplayertracker;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PostGameHeatmap extends AppCompatActivity {

    private HeatmapOverlay heatmapOverlay;
    private List<PointF> playerPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postgame_heatmap);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageButton returnBtn = findViewById(R.id.back_button);
        Button statisticsBtn = findViewById(R.id.tab_statistics);
        Button patternsBtn = findViewById(R.id.tab_patterns);
        returnBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PostGameHeatmap.this, MainActivity.class);
            startActivity(intent);
        });
        statisticsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PostGameHeatmap.this, PostGameStatistics.class);
            startActivity(intent);
        });
        patternsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PostGameHeatmap.this, PostGamePatterns.class);
            startActivity(intent);
        });

        playerPositions = new ArrayList<>();
        generateSamplePositions();

        heatmapOverlay = new HeatmapOverlay(this, playerPositions);
        FrameLayout pitchLayout = findViewById(R.id.football_pitch);
        pitchLayout.addView(heatmapOverlay);
    }

    private void generateSamplePositions() {
        Random random = new Random();
        for (int i = 0; i < 60; i++) {
            playerPositions.add(new PointF(
                300 + random.nextInt(500),
                700 + random.nextInt(550)
            ));
        }
    }

    public void updateHeatmap(List<PointF> newPositions) {
        heatmapOverlay.setPlayerPositions(newPositions);
    }
}
