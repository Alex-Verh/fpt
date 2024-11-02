package com.example.fpt_footballplayertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class RealTimePositioning extends AppCompatActivity {

    // RELATIVE POSITIONING OF THE PLAYER ON THE FOOTBALL PITCH ( THE IMAGE RELATIVE TO Kunstgrasveld Enschede)
    private final double[] bottomLeftCorner = {52.242704, 6.850216};
    private final double[] topLeftCorner = {52.243232, 6.848823};
    private final double[] topRightCorner = {52.243797, 6.849397};
    private final double[] bottomRightCorner = {52.243275, 6.850786};

    //// The player's current GPS coordinates:
    double playerLat;
    double playerLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.realtime_positioning);

        // NAVIGATION BUTTONS
        ImageButton returnBtn = findViewById(R.id.back_button);
        Button statisticsBtn = findViewById(R.id.tab_statistics);

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RealTimePositioning.this, MainActivity.class);
                startActivity(intent);
            }
        });

        statisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RealTimePositioning.this, RealTimeStatistics.class);
                startActivity(intent);
            }
        });

        // END NAVIGATION BUTTONS

        // MQTT
        MqttManager.getInstance(this).addTopicListener(MqttManager.GPS_TOPIC, this::handleGpsUpdate);
    }


    private void handleGpsUpdate(String payload) {
        if (!payload.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(payload);

                String datetimeUTC = jsonObject.getString("datetime_utc");
                double lat = jsonObject.getDouble("lat");
                double lon = jsonObject.getDouble("lon");

                playerLat = lat;
                playerLng = lon;

                Log.d("MQTT", "GPS data - Datetime: " + datetimeUTC + ", Lat: " + lat + ", Lon: " + lon);

                // update player marker position on the image
                updatePlayerMarker(playerLat, playerLng);

            } catch (JSONException e) {
                Log.e("MQTT", "Invalid GPS data", e);
            }
        }
    }

    private double[] getRelativePosition(double playerLat, double playerLng) {
        double latRangeTop = topLeftCorner[0] - bottomLeftCorner[0];
        double lngRangeLeft = topLeftCorner[1] - bottomLeftCorner[1];

        double latRatio = (playerLat - bottomLeftCorner[0]) / latRangeTop;
        double lngRatio = (playerLng - bottomLeftCorner[1]) / lngRangeLeft;

        latRatio = Math.min(Math.max(latRatio, 0), 1);
        lngRatio = Math.min(Math.max(lngRatio, 0), 1);

        return new double[]{latRatio, lngRatio};
    }

    private int[] getImageCoordinates(double playerLat, double playerLng, int imageWidth, int imageHeight) {
        double[] relativePos = getRelativePosition(playerLat, playerLng);
        int x = (int) (relativePos[1] * imageWidth);
        int y = (int) ((1 - relativePos[0]) * imageHeight);

        return new int[]{x, y};
    }

    private void updatePlayerMarker(double playerLat, double playerLng) {
        View playerView = findViewById(R.id.player_marker);

        FrameLayout footballPitch = findViewById(R.id.football_pitch);
        int footballPitchWidth = footballPitch.getWidth();
        int footballPitchHeight = footballPitch.getHeight();

        int[] coordinates = getImageCoordinates(playerLat, playerLng, footballPitchWidth, footballPitchHeight);
        playerView.setX(coordinates[0]);
        playerView.setY(coordinates[1]);
    }


}