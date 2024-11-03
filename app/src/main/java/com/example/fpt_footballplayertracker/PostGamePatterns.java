package com.example.fpt_footballplayertracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import android.widget.FrameLayout.LayoutParams;


public class PostGamePatterns extends AppCompatActivity implements OnMapReadyCallback {
    private final double[] bottomLeftCorner = {52.242704, 6.850216};
    private final double[] topLeftCorner = {52.243232, 6.848823};
    private final double[] topRightCorner = {52.243797, 6.849397};
    private final double[] bottomRightCorner = {52.243275, 6.850786};
    private FrameLayout footballPitch;
    int footballPitchWidth;
    int footballPitchHeight;
    private GoogleMap mMap;
    private DatabaseHelper dbHelper;
    long startTimestamp;
    long endTimestamp;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postgame_patterns);

        // Get times
        Intent intentExtra = getIntent();
        String startTime = intentExtra.getStringExtra("EXTRA_START_TIME");
        String endTime = intentExtra.getStringExtra("EXTRA_END_TIME");
        String date = intentExtra.getStringExtra("EXTRA_DATE");

        // ---------- Initialize DB Helper ---------- //
        dbHelper = new DatabaseHelper(this);

        assert date != null;
        long[] timeStamps = PostGameStatistics.convertTimestamps(startTime, endTime, date);
        startTimestamp = timeStamps[0];
        endTimestamp = timeStamps[1];

//        loadSprintsData(startTimestamp, endTimestamp);

        // Remove the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // NAVIGATION BUTTONS
        ImageButton returnBtn = findViewById(R.id.back_button);
        Button statisticsBtn = findViewById(R.id.tab_statistics);
        Button heatmapBtn = findViewById(R.id.tab_heatmap);

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostGamePatterns.this, MainActivity.class);
                startActivity(intent);
            }
        });
        statisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostGamePatterns.this, PostGameStatistics.class);

                intent.putExtra("EXTRA_START_TIME", startTime);
                intent.putExtra("EXTRA_END_TIME", endTime);
                intent.putExtra("EXTRA_DATE", date);

                startActivity(intent);
            }
        });

        heatmapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostGamePatterns.this, PostGameHeatmap.class);

                intent.putExtra("EXTRA_START_TIME", startTime);
                intent.putExtra("EXTRA_END_TIME", endTime);
                intent.putExtra("EXTRA_DATE", date);

                startActivity(intent);
            }
        });

        // END NAVIGATION BUTTONS


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
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private void loadSprintsData(long startTimestamp, long endTimestamp) {
        Cursor cursor = dbHelper.getSprintsData(startTimestamp, endTimestamp);
        if (cursor != null) {

            while (cursor.moveToNext()) {
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAT));
                double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LON));
                float course = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COURSE));

                Log.d("POST-SPRINT", "Marker data: " + lat + ", " + lon + ", " + course);
                addMarkerOnOverlay(lat, lon, course);
            }
            cursor.close();
        }
    }

    private void addMarkerOnOverlay(double latitude, double longitude, float rotation) {
        // Convert latitude and longitude to screen coordinates
        LatLng position = new LatLng(latitude, longitude);
        Projection projection = mMap.getProjection();
        Point screenPoint = projection.toScreenLocation(position);

        // Create a new ImageView for the marker
        ImageView markerImage = new ImageView(this);
        markerImage.setImageResource(R.drawable.movement_pattern_arrow);
        markerImage.setRotation(rotation);

        // Set the position of the marker
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        layoutParams.leftMargin = screenPoint.x - (markerImage.getDrawable().getIntrinsicWidth() / 2);
        layoutParams.topMargin = screenPoint.y - (markerImage.getDrawable().getIntrinsicHeight() / 2);
        markerImage.setLayoutParams(layoutParams);

        // Add the marker ImageView to the FrameLayout on top of the overlay image
        footballPitch.addView(markerImage);
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

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // add markers
        loadSprintsData(startTimestamp, endTimestamp);
    }

}