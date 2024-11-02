package com.example.fpt_footballplayertracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class RealTimePositioning extends AppCompatActivity implements OnMapReadyCallback {

    // RELATIVE POSITIONING OF THE PLAYER ON THE FOOTBALL PITCH ( THE IMAGE RELATIVE TO Kunstgrasveld Enschede)
    private final double[] bottomLeftCorner = {52.242704, 6.850216};
    private final double[] topLeftCorner = {52.243232, 6.848823};
    private final double[] topRightCorner = {52.243797, 6.849397};
    private final double[] bottomRightCorner = {52.243275, 6.850786};

    // The player's current GPS coordinates:
    double playerLat;
    double playerLng;
    FrameLayout footballPitch;
    int footballPitchWidth;
    int footballPitchHeight;
    private GoogleMap mMap;

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
        footballPitch = findViewById(R.id.football_pitch);

        footballPitch.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure the layout is measured once and remove the listener
                footballPitchWidth = footballPitch.getWidth();
                footballPitchHeight = footballPitch.getHeight();

                if (footballPitchWidth > 0 && footballPitchHeight > 0) {
                    footballPitch.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

    private void updatePlayerMarker(double playerLat, double playerLng) {
        final LatLng playerPosition = new LatLng(playerLat, playerLng);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View playerMarker = findViewById(R.id.player_marker);
                playerMarker.setVisibility(View.VISIBLE);

                Point markerScreenPosition = mMap.getProjection().toScreenLocation(playerPosition);

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) playerMarker.getLayoutParams();
                params.leftMargin = markerScreenPosition.x - (playerMarker.getWidth() / 2);
                params.topMargin = markerScreenPosition.y - (playerMarker.getHeight() / 2);
                playerMarker.setLayoutParams(params);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng bottomLeft = new LatLng(bottomLeftCorner[0], bottomLeftCorner[1]);
        LatLng topLeft = new LatLng(topLeftCorner[0], topLeftCorner[1]);
        LatLng topRight = new LatLng(topRightCorner[0], topRightCorner[1]);
        LatLng bottomRight = new LatLng(bottomRightCorner[0], bottomRightCorner[1]);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(bottomLeft);
        builder.include(topLeft);
        builder.include(topRight);
        builder.include(bottomRight);
        LatLngBounds bounds = builder.build();

        LatLng centerPoint = bounds.getCenter();
        float zoomLevel = 18.9f;
        int rotationAngle = -60;


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(centerPoint)
                .zoom(zoomLevel)
                .bearing(rotationAngle)
                .tilt(0)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition)); // Animate to rotation
    }
}
