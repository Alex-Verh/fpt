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
    private float topSpeed = 0;
    private float averageSpeed = 0;
    private float topHeartRate;
    private float averageHeartRate;
    private float totalDistanceCovered;
    private int numberOfSprints;
    private String wellBeing;
    private float performance;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.realtime_statistics);

        loadStatistics();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageButton returnBtn = findViewById(R.id.back_button);
        Button positioningBtn = findViewById(R.id.tab_positioning);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostGameStatistics.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        positioningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostGameStatistics.this, RealTimePositioning.class);
                startActivity(intent);
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
                adjustSpeed(15 + random.nextFloat() * 5);
                adjustHeartRate(160 + random.nextFloat() * 40);
                adjustDistance(random.nextFloat() * 0.1f);
                numberOfSprints += 1;
                wellBeing = topHeartRate > 180 ? "Warning" : "Good";
                calculatePerformance();

                TextView topSpeed = findViewById(R.id.topSpeed);
                topSpeed.setText(String.format("%.2f km/h", PostGameStatistics.this.topSpeed));

                TextView averageSpeed = findViewById(R.id.averageSpeed);
                averageSpeed.setText(String.format("%.2f km/h", PostGameStatistics.this.averageSpeed));

                TextView averageHeartRate = findViewById(R.id.averageHeartRate);
                averageHeartRate.setText(String.format("%.2f bpm", PostGameStatistics.this.averageHeartRate));

                TextView topHeartRate = findViewById(R.id.topHeartRate);
                topHeartRate.setText(String.format("%.2f bpm", PostGameStatistics.this.topHeartRate));

                TextView distance = findViewById(R.id.totalDistanceCovered);
                distance.setText(String.format("%.2f km", PostGameStatistics.this.totalDistanceCovered));

                TextView numberOfSprints = findViewById(R.id.numberOfSprints);
                numberOfSprints.setText(String.valueOf(PostGameStatistics.this.numberOfSprints));

                TextView performance = findViewById(R.id.performanceResult);
                performance.setText(String.valueOf(PostGameStatistics.this.performance));

                handler.postDelayed(this, 1000);
            }
        };

        handler.post(statisticsUpdater);
    }

    private void adjustSpeed(float currentSpeed){
        if(currentSpeed > topSpeed){
            topSpeed = currentSpeed;
        }

        averageSpeed = (averageSpeed + currentSpeed) / 2;
    }

    private void adjustDistance(float newDistance){
        totalDistanceCovered = newDistance;
    }

    private void adjustHeartRate(float currentHeartRate){
        if(currentHeartRate > topHeartRate){
            topHeartRate = currentHeartRate;
        }

        averageHeartRate = (averageHeartRate + currentHeartRate) / 2;
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
