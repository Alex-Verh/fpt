package com.example.fpt_footballplayertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

    private double heartRate;
    private double averageHeartRate;

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
                // TODO: handle pulse data

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
                // TODO: handle accelerometer data

                Log.d("MQTT", "Accelerometer data - Ax: " + accelX +
                        ", Ay: " + accelY + ", Az: " + accelZ + ", Amag: " + accelMagnitude);
            } catch (JSONException e) {
                Log.e("MQTT", "Invalid accelerometer data", e);
            }
        }
    }

    public void loadStatistics() {
        handler = new Handler();
        Random random = new Random();

        statisticsUpdater = new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {

//                Randomize speed
//                currentSpeed = 15 + random.nextFloat() * 5;
//                adjustTopSpeed(currentSpeed);

//                adjustDistance(random.nextFloat() * 0.1f);
//                numberOfSprints += 1;
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

                TextView currentWellbeing = findViewById(R.id.wellbeing);
                currentWellbeing.setText(String.valueOf(RealTimeStatistics.this.wellBeing));

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
//        return R * c; // dist in meters
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
        currentSpeed = newSpeed;

        if (currentSpeed > topSpeed){
            topSpeed = currentSpeed;
        }
        averageSpeed = (averageSpeed + currentSpeed) / 2;
    }

    private void adjustHeartRate(double newHeartRate){
        heartRate = newHeartRate;
        averageHeartRate = (averageHeartRate + newHeartRate) / 2;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(statisticsUpdater);
        }
    }
}
