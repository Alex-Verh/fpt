package com.example.fpt_footballplayertracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

public class PostGameStatistics extends AppCompatActivity {


    // RealTimeStatistics variables
    private double topSpeed;
    private double averageSpeed;
    private double topHeartRate;
    private double averageHeartRate;
    private double totalDistanceCovered;
    private int numberOfSprints;
    private String wellBeing;
    private float performance;

    private DatabaseHelper dbHelper;
    private boolean firstTimeToLoad = true;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postgame_statistics);

        Intent intent = getIntent();
        String startTime = intent.getStringExtra("EXTRA_START_TIME");
        String endTime = intent.getStringExtra("EXTRA_END_TIME");
        String date = intent.getStringExtra("EXTRA_DATE");

        // ---------- Initialize DB Helper ---------- //
        dbHelper = new DatabaseHelper(this);


//        // ---------- Initialize FAKE DATA ---------- //
//
//        assert date != null;
////        RANDOM HEART
//        List<String> payloadStrings = RandomPositionGenerator.generateRandomHearbeat();
//        Log.d("INSERT - FAKE", payloadStrings.toString());
//        for (String str : payloadStrings) {
//            dbHelper.insertRawPulseData(str);
//            Log.d("INSERT - FAKE - ADD - HEART", str);
//        }
//
////        RANDOM POSITIONS
//        payloadStrings = RandomPositionGenerator.generateRandomPositions();
//        Log.d("INSERT - FAKE", payloadStrings.toString());
//        for (String str : payloadStrings) {
//            dbHelper.insertGpsData(str);
//            Log.d("INSERT - FAKE - ADD - POSITION", str);
//        }
//
////        RANDOM SPRINTS
//        payloadStrings = RandomPositionGenerator.generateRandomSprints();
//        Log.d("INSERT - FAKE", payloadStrings.toString());
//        for (String str : payloadStrings) {
//            dbHelper.insertRawSprintsData(str);
//            Log.d("INSERT - FAKE - ADD - SPRINT", str);
//        }
//        // ---------- FINISH FAKE DATA ---------- //


        long[] timeStamps = convertTimestamps(startTime, endTime, date);
        long startTimestamp = timeStamps[0];
        long endTimestamp = timeStamps[1];

        // actually get data from db and process it
        if (firstTimeToLoad) {
            loadGpsData(startTimestamp, endTimestamp);
            loadPulseData(startTimestamp, endTimestamp);
            loadSprintsData(startTimestamp, endTimestamp);
            firstTimeToLoad = false;
        }

        Log.d("POST-GPS", "Hello! " + startTime + "-" + endTime + " " + date);

        // mainly displays data
        loadStatistics();

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

    public static long[] convertTimestamps(String startTime, String endTime, String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // set timezone to UTC

        long startTimestamp = 0;
        long endTimestamp = 0;

        try {
            // transform date format to "yyyy-MM-dd" and append the start and end times
            String[] parts = date.split("/"); // Split the date by "/"
            String formattedDate = parts[2] + "-" + parts[1] + "-" + parts[0];

            String formattedStartTime = formattedDate + " " + startTime + ":00";
            String formattedEndTime = formattedDate + " " + endTime + ":00";

            Log.d("POST-GPS", "Formatted time: " + formattedStartTime + ", " + formattedEndTime);

            // Parse the date and time to get milliseconds
            startTimestamp = sdf.parse(formattedStartTime).getTime();
            endTimestamp = sdf.parse(formattedEndTime).getTime();

            return new long[]{startTimestamp, endTimestamp};
        } catch (ParseException e) {
            e.printStackTrace();
            return new long[]{0, 0};
        }
    }

    private void loadGpsData(long startTimestamp, long endTimestamp) {
        Log.d("POST-GPS", "Milliseconds: " + startTimestamp + ", " + endTimestamp);

        // speed helper var
        int speedCounter = 0;
        double totalSpeed = 0d;

        // distance helper vars
        double previousLat = 0d;
        double previousLon = 0d;
        boolean firstEntry = true;

        // get gps data from db
        Cursor cursor = dbHelper.getGpsData(startTimestamp, endTimestamp); // Assuming this returns a Cursor
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAT));
                String latDir = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAT_DIR));
                double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LON));
                String lonDir = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LON_DIR));
                double speed = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SPEED));
                double course = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE));

                // top speed
                if (speed > topSpeed) {
                    topSpeed = speed;
                }

                // average speed
                speedCounter++;
                totalSpeed += speed;

                // top total distance covered
                if (!firstEntry) {
                    double distance = RealTimeStatistics.calculateDistance(previousLat, previousLon, lat, lon);
                    totalDistanceCovered += distance;
                } else {
                    firstEntry = false;
                }

                previousLat = lat;
                previousLon = lon;

                // Print the retrieved data
                Log.d("POST-GPS", "Timestamp: " + timestamp + ", Lat: " + lat + " " + latDir +
                        ", Lon: " + lon + " " + lonDir + ", Speed: " + speed +
                        " km/h, Course: " + course);
            }
            cursor.close();

            // speed average
            if (speedCounter > 0) {
                averageSpeed = totalSpeed / speedCounter;
            }
        }
    }

    private void loadSprintsData(long startTimestamp, long endTimestamp) {
        Cursor cursor = dbHelper.getSprintsData(startTimestamp, endTimestamp);
        if (cursor != null) {
            numberOfSprints = cursor.getCount();
            cursor.close();
            Log.d("POST-SPRINTS", "Number of sprints: " + numberOfSprints);
        } else {
            Log.d("POST-SPRINTS", "Cursor is null, no sprints found.");
        }
    }

    private void loadPulseData(long startTimestamp, long endTimestamp) {
        double totalPulseRate = 0d;
        int pulseCounter = 0;

        Cursor cursor = dbHelper.getPulseData(startTimestamp, endTimestamp);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP));
                double pulseRate = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PULSE_RATE));

                // top pulse
                if (pulseRate > topHeartRate) {
                    topHeartRate = pulseRate;
                }

                // avg pulse
                totalPulseRate += pulseRate;
                pulseCounter++;

                Log.d("POST-PULSE", "Timestamp: " + timestamp + ", Pulse Rate: " + pulseRate);
            }
            Log.d("POST-PULSE", "Nr of pulse rows: " + cursor.getCount());
            cursor.close();

            if (pulseCounter > 0) {
                averageHeartRate = totalPulseRate / pulseCounter;
            }
        } else {
            Log.d("POST-PULSE", "Null pulse cursor!");
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void loadStatistics() {


        LinearLayout wellBeingfield = findViewById(R.id.general_wellbeing);
        LinearLayout heartRateField = findViewById(R.id.heart_rate_field);
        LinearLayout averageHeartField = findViewById(R.id.average_heart_field);
        LinearLayout topSpeedField = findViewById(R.id.top_speed_field);
        LinearLayout performanceField = findViewById(R.id.performance_field);

        if (averageHeartRate > 180) {
            wellBeingfield.setBackgroundColor(ContextCompat.getColor(PostGameStatistics.this, R.color.warning));
            averageHeartField.setBackgroundColor(ContextCompat.getColor(PostGameStatistics.this, R.color.alert));
            wellBeing = "Warning";

        } else {
            wellBeingfield.setBackgroundColor(ContextCompat.getColor(PostGameStatistics.this, R.color.good));
            averageHeartField.setBackgroundColor(ContextCompat.getColor(PostGameStatistics.this, R.color.white));
            wellBeing = "Good";
        }

        if (topHeartRate > 170) {
            heartRateField.setBackgroundColor(ContextCompat.getColor(PostGameStatistics.this, R.color.alert));

        } else {
            heartRateField.setBackgroundColor(ContextCompat.getColor(PostGameStatistics.this, R.color.white));
        }

        if (topSpeed > 10) {
            topSpeedField.setBackgroundColor(ContextCompat.getColor(PostGameStatistics.this, R.color.good));
        } else {
            topSpeedField.setBackgroundColor(ContextCompat.getColor(PostGameStatistics.this, R.color.white));
        }

        calculatePerformance();

        if (performance > 5) {
            performanceField.setBackgroundColor(ContextCompat.getColor(PostGameStatistics.this, R.color.good));
        } else {
            performanceField.setBackgroundColor(ContextCompat.getColor(PostGameStatistics.this, R.color.white));
        }

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
        performance.setText(PostGameStatistics.this.performance + "/10.0");

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
    }
}
