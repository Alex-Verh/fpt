package com.example.fpt_footballplayertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

public class PostGamePatterns extends AppCompatActivity {

    public Statistics statistics;
    private View positioningSection;
    private View statisticsSection;
    private LinearLayout tabPositioning;
    private LinearLayout tabStatistics;
    private Handler handler;
    private Runnable statisticsUpdater;

    // TextView references
    private TextView topSpeedTextView, averageSpeedTextView, topHeartRateTextView;

    private void showPositioningSection() {
        positioningSection.setVisibility(View.VISIBLE);
        statisticsSection.setVisibility(View.GONE);
    }

    private void showStatisticsSection() {
        Log.i("STATISTICS", "Showing statistics section");
        positioningSection.setVisibility(View.GONE);
        statisticsSection.setVisibility(View.VISIBLE);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postgame_patterns);

        statistics = new Statistics();
        statisticsSection = findViewById(R.id.statistics_section);
        tabStatistics = findViewById(R.id.tab_statistics);

        showStatisticsSection();
        loadStatistics();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        tabPositioning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPositioningSection();
            }
        });

        tabStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStatisticsSection();
            }
        });

        ImageButton returnBtn = findViewById(R.id.back_button);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostGamePatterns.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void loadStatistics() {
        handler = new Handler();
        Random random = new Random();

        statisticsUpdater = new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                statistics.adjustSpeed(15 + random.nextFloat() * 5);
                statistics.adjustHeartRate(160 + random.nextFloat() * 40);
                statistics.adjustDistance(random.nextFloat() * 0.1f);
                statistics.numberOfSprints += 1;
                statistics.wellbeing = statistics.topHeartRate > 180 ? "Tired" : "Good";
                statistics.calculatePerformance();

                TextView topSpeed = findViewById(R.id.topSpeed);
                topSpeed.setText(String.format("%.2f km/h", statistics.topSpeed));

                TextView averageSpeed = findViewById(R.id.averageSpeed);
                averageSpeed.setText(String.format("%.2f km/h", statistics.averageSpeed));

                TextView averageHeartRate = findViewById(R.id.averageHeartRate);
                averageHeartRate.setText(String.format("%.2f bpm", statistics.averageHeartRate));

                TextView topHeartRate = findViewById(R.id.topHeartRate);
                topHeartRate.setText(String.format("%.2f bpm", statistics.topHeartRate));

                TextView distance = findViewById(R.id.totalDistanceCovered);
                distance.setText(String.format("%.2f km", statistics.totalDistanceCovered));

                TextView numberOfSprints = findViewById(R.id.numberOfSprints);
                numberOfSprints.setText(String.valueOf(statistics.numberOfSprints));

                TextView performance = findViewById(R.id.performanceResult);
                performance.setText(String.valueOf(statistics.performance));

                handler.postDelayed(this, 1000);
            }
        };

        handler.post(statisticsUpdater);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(statisticsUpdater);
        }
    }
}
