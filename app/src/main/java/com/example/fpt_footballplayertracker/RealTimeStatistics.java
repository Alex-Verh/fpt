package com.example.fpt_footballplayertracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class RealTimeStatistics extends AppCompatActivity {

    private Handler handler;
    private Runnable statisticsUpdater;

    // RealTimeStatistics variables
    private double topSpeed = 0;
    private double currentSpeed;
    private double averageSpeed = 0;
    private double totalSpeed = 0;
    private int speedCount = 0;

    private double heartRate;
    private double averageHeartRate;
    private double totalHeartRate = 0;
    private int heartRateCount = 0;

    private boolean firstDistUpdate = true;
    private double lastLat = 0;
    private double lastLon = 0;
    private double totalDistanceCovered;
    private int numberOfSprints;
    private String wellBeing;

    private static final long SPRINT_COOLDOWN = 2000; // 1 second
    private long lastSprintTime = 0; // long for milliseconds

    private static final double SPRINT_THRESHOLD = 1;

    private double latestLat, latestLon;
    private double latestCourse;
    private DatabaseHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.realtime_statistics);

        loadStatistics();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // ---------- Subscribe to MQTT updates ---------- //
        MqttManager.getInstance(this).addTopicListener(MqttManager.PULSE_TOPIC, this::handlePulseUpdate);
        MqttManager.getInstance(this).addTopicListener(MqttManager.ACCEL_TOPIC, this::handleAccelUpdate);
        MqttManager.getInstance(this).addTopicListener(MqttManager.GPS_TOPIC, this::handleGpsUpdate);

        // ---------- Initialize DB Helper ---------- //
        dbHelper = new DatabaseHelper(this);

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

    private void handlePulseUpdate(String payload) {
        if (!payload.isEmpty()) {
            try {
                double newHeartRate = Double.parseDouble(payload.trim());
                adjustHeartRate(newHeartRate);

                Log.d("MQTT", "Pulse received: " + heartRate);
            } catch (NumberFormatException e) {
                Log.e("MQTT", "Invalid pulse data", e);
            }
        }
    }

    private void handleGpsUpdate(String payload) {
        if (!payload.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(payload);

                String datetimeUTC = jsonObject.getString("datetime_utc");
                double lat = jsonObject.getDouble("lat");
                String latDir = jsonObject.getString("lat_dir");
                double lon = jsonObject.getDouble("lon");
                String lonDir = jsonObject.getString("lon_dir");
                double speed = jsonObject.getDouble("speed");
                // course is the deviation from the true north (north pole); also I think it is not working on this gps
                latestCourse = jsonObject.getDouble("course");

                // used for sprint storage in db
                latestLat = lat;
                latestLon = lon;

                adjustDistance(lat, lon);
                adjustSpeed(speed);

                Log.d("MQTT-GPS", "GPS data - Datetime: " + datetimeUTC +
                        ", Lat: " + lat + " " + latDir + ", Lon: " + lon + " " + lonDir +
                        ", Speed: " + speed + " m/s, <- COURSE: " + latestCourse + " ->");
            } catch (JSONException e) {
                Log.e("MQTT", "Invalid GPS data", e);
            }
        }
    }

    private void handleAccelUpdate(String payload) {
        if (!payload.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(payload);

                double accelX = jsonObject.getDouble("Ax");
                double accelY = jsonObject.getDouble("Ax");
                double accelZ = jsonObject.getDouble("Ax");
                double accelMagnitude = jsonObject.getDouble("Ax");

                adjustSprints(accelMagnitude);

                Log.d("MQTT", "Accelerometer data - Ax: " + accelX +
                        ", Ay: " + accelY + ", Az: " + accelZ + ", Amag: " + accelMagnitude);
            } catch (JSONException e) {
                Log.e("MQTT", "Invalid accelerometer data", e);
            }
        }
    }

    public void loadStatistics() {
        handler = new Handler();
        statisticsUpdater = new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {

                LinearLayout wellBeingfield = findViewById(R.id.general_wellbeing);
                LinearLayout heartRateField = findViewById(R.id.heart_rate_field);
                LinearLayout topSpeedField = findViewById(R.id.top_speed_field);

                if (averageHeartRate > 180) {
                    heartRateField.setBackgroundColor(ContextCompat.getColor(RealTimeStatistics.this, R.color.alert));

                } else {
                    heartRateField.setBackgroundColor(ContextCompat.getColor(RealTimeStatistics.this, R.color.white));
                }

                if (heartRate > 180) {
                    wellBeing = "Warning";
                    wellBeingfield.setBackgroundColor(ContextCompat.getColor(RealTimeStatistics.this, R.color.warning));

                } else {
                    wellBeing = "Good";
                    wellBeingfield.setBackgroundColor(ContextCompat.getColor(RealTimeStatistics.this, R.color.good));
                }

                if (topSpeed > 10) {
                    topSpeedField.setBackgroundColor(ContextCompat.getColor(RealTimeStatistics.this, R.color.good));

                } else {
                    topSpeedField.setBackgroundColor(ContextCompat.getColor(RealTimeStatistics.this, R.color.white));
                }


                TextView currentWellbeing = findViewById(R.id.wellbeing);
                currentWellbeing.setText(String.valueOf(RealTimeStatistics.this.wellBeing));

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

    private void adjustDistance(double newLat, double newLon) {
        if (newLat == 0d || newLon == 0d) {
            return;
        }

        if (firstDistUpdate) {
            firstDistUpdate = false;
        } else {
            double distance = calculateDistance(lastLat, lastLon, newLat, newLon);
            totalDistanceCovered += distance;
        }

        Log.d("DISTANCE", "Coords: (" + lastLat + ", " + lastLon + ") vs (" + newLat + ", " + newLon + ")");

        lastLat = newLat;
        lastLon = newLon;
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c / 1000d; // dist in km
    }

    private void adjustSprints(double accelMagnitude) {
        long currentTime = System.currentTimeMillis();
        if (accelMagnitude > SPRINT_THRESHOLD && (currentTime - lastSprintTime) > SPRINT_COOLDOWN) {
            dbHelper.insertSprintsData(latestLat, latestLon, latestCourse);
            Log.d("SPRINTS", "Inserted in db: " + latestLat + ", " + latestLon + ", " + latestCourse);
            numberOfSprints++;
            lastSprintTime = currentTime;
            Log.d("MQTT", "Increased nr of sprints: " + accelMagnitude);
        }
    }

    private void adjustSpeed(double newSpeed) {
        totalSpeed += newSpeed;
        speedCount++;

        averageSpeed = totalSpeed / speedCount;

        currentSpeed = newSpeed;

        if (currentSpeed > topSpeed){
            topSpeed = currentSpeed;
        }
    }

    private void adjustHeartRate(double newHeartRate){
        totalHeartRate += newHeartRate;
        heartRateCount++;

        averageHeartRate = totalHeartRate / heartRateCount;

        heartRate = newHeartRate;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(statisticsUpdater);
        }
    }
}
