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

public class RealTimeStatistics extends AppCompatActivity {

    private Handler handler;
    private Runnable statisticsUpdater;

    // RealTimeStatistics variables
    private float topSpeed = 0;
    private float currentSpeed;
    private float averageSpeed = 0;

    private float heartRate;
    private float averageHeartRate;

    private float totalDistanceCovered;
    private int numberOfSprints;
    private String wellBeing;

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
                Intent intent = new Intent(RealTimeStatistics.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        positioningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RealTimeStatistics.this, RealTimePositioning.class);
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

//                Randomize speed
                currentSpeed = 15 + random.nextFloat() * 5;
                adjustTopSpeed(currentSpeed);

//                Randomize speed
                heartRate = 160 + random.nextFloat() * 40;
                adjustHeartRate(heartRate);


                adjustDistance(random.nextFloat() * 0.1f);
                numberOfSprints += 1;
                wellBeing = averageHeartRate > 180 ? "Warning" : "Good";

                TextView currentSpeedText = findViewById(R.id.currentSpeed);
                currentSpeedText.setText(String.format("%.2f km/h", RealTimeStatistics.this.currentSpeed));

                TextView topSpeed = findViewById(R.id.topSpeed);
                topSpeed.setText(String.format("%.2f km/h", RealTimeStatistics.this.topSpeed));

                TextView averageSpeed = findViewById(R.id.averageSpeed);
                averageSpeed.setText(String.format("%.2f km/h", RealTimeStatistics.this.averageSpeed));

                TextView averageHeartRate = findViewById(R.id.averageHeartRate);
                averageHeartRate.setText(String.format("%.2f bpm", RealTimeStatistics.this.averageHeartRate));

                TextView currentHeart = findViewById(R.id.currentHeart);
                currentHeart.setText(String.format("%.2f bpm", RealTimeStatistics.this.heartRate));

                TextView distance = findViewById(R.id.totalDistanceCovered);
                distance.setText(String.format("%.2f km", RealTimeStatistics.this.totalDistanceCovered));

                TextView numberOfSprints = findViewById(R.id.numberOfSprints);
                numberOfSprints.setText(String.valueOf(RealTimeStatistics.this.numberOfSprints));

                handler.postDelayed(this, 1000);
            }
        };

        handler.post(statisticsUpdater);
    }

    private void adjustTopSpeed(float currentSpeed){
        if(currentSpeed > topSpeed){
            topSpeed = currentSpeed;
        }

        averageSpeed = (averageSpeed + currentSpeed) / 2;
    }

    private void adjustDistance(float newDistance){
        totalDistanceCovered = newDistance;
    }

    private void adjustHeartRate(float currentHeartRate){
        averageHeartRate = (averageHeartRate + currentHeartRate) / 2;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(statisticsUpdater);
        }
    }
}