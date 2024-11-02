package com.example.fpt_footballplayertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Random;

public class PostGameStatistics extends AppCompatActivity {

    private Handler handler;
    private Runnable statisticsUpdater;

    // RealTimeStatistics variables
    private float topSpeed = 16;
    private float averageSpeed = 9;
    private float topHeartRate = 190;
    private float averageHeartRate = 130;
    private float totalDistanceCovered = 6.9f;
    private int numberOfSprints = 21;
    private String wellBeing;
    private float performance;

    private DatabaseHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postgame_statistics);

        Intent intent = getIntent();
        String startTime = intent.getStringExtra("EXTRA_START_TIME");
        String endTime = intent.getStringExtra("EXTRA_END_TIME");
        String date = intent.getStringExtra("EXTRA_DATE");

        loadStatistics();

        // ---------- Initialize DB Helper ---------- //
        dbHelper = new DatabaseHelper(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageButton returnBtn = findViewById(R.id.back_button);
        Button patternsBtn = findViewById(R.id.tab_patterns);
        Button heatmapBtn = findViewById(R.id.tab_heatmap);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostGameStatistics.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        patternsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostGameStatistics.this, PostGamePatterns.class);

                intent.putExtra("EXTRA_START_TIME", startTime);
                intent.putExtra("EXTRA_END_TIME", endTime);
                intent.putExtra("EXTRA_DATE", date);

                startActivity(intent);
            }
        });

        heatmapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostGameStatistics.this, PostGameHeatmap.class);

                intent.putExtra("EXTRA_START_TIME", startTime);
                intent.putExtra("EXTRA_END_TIME", endTime);
                intent.putExtra("EXTRA_DATE", date);

                startActivity(intent);
            }
        });
    }

    @SuppressLint("DefaultLocale")
    public void loadStatistics() {
        handler = new Handler();
        Random random = new Random();

        numberOfSprints += 1;
        wellBeing = topHeartRate > 180 ? "Warning" : "Good";
        calculatePerformance();

        TextView topSpeed = findViewById(R.id.topSpeed);
        topSpeed.setText(String.format("%.2f km/h", PostGameStatistics.this.topSpeed));

        TextView averageSpeed = findViewById(R.id.averageSpeed);
        averageSpeed.setText(String.format("%.2f km/h", PostGameStatistics.this.averageSpeed));

        TextView averageHeartRate = findViewById(R.id.averageHeartRate);
        averageHeartRate.setText(String.format("%.2f bpm", PostGameStatistics.this.averageHeartRate));

        TextView topHeartRate = findViewById(R.id.topHeart);
        topHeartRate.setText(String.format("%.2f bpm", PostGameStatistics.this.topHeartRate));

        TextView distance = findViewById(R.id.totalDistanceCovered);
        distance.setText(String.format("%.2f km", PostGameStatistics.this.totalDistanceCovered));

        TextView numberOfSprints = findViewById(R.id.numberOfSprints);
        numberOfSprints.setText(String.valueOf(PostGameStatistics.this.numberOfSprints));

        TextView performance = findViewById(R.id.performance);
        performance.setText(String.valueOf(PostGameStatistics.this.performance));

        TextView currentWellbeing = findViewById(R.id.wellbeing);
        currentWellbeing.setText(String.valueOf(PostGameStatistics.this.wellBeing));

    }

    @SuppressLint("DefaultLocale")
    private void calculatePerformance(){
        float performanceScore = 0;
        performanceScore += Math.min(topSpeed / 20.0f, 2.0f);
        performanceScore += Math.min(averageSpeed / 10.0f, 2.0f);

        if (topHeartRate < 180) {
            performanceScore += 1.5f;
        } else if (topHeartRate <= 200) {
            performanceScore += 1.0f;
        } else {
            performanceScore += 0.5f;
        }

        performanceScore += Math.min(numberOfSprints / 20.0f, 2.0f);

        if ("good".equalsIgnoreCase(wellBeing)) {
            performanceScore += 2.0f;
        } else if ("warning".equalsIgnoreCase(wellBeing)) {
            performanceScore += 1.0f;
        } else {
            performanceScore += 0.5f;
        }

        performanceScore = Math.round(performanceScore * 100) / 100.0f;

        performance = Math.min(performanceScore, 10.0f);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(statisticsUpdater);
        }
    }
}
