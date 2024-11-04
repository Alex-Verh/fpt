package com.example.fpt_footballplayertracker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;


public class PostGameHeatmap extends AppCompatActivity implements OnMapReadyCallback {

    //    Kunstgrasveld
//    private final double[] bottomLeftCorner = {52.242704, 6.850216};
//    private final double[] topLeftCorner = {52.243232, 6.848823};
//    private final double[] topRightCorner = {52.243797, 6.849397};
//    private final double[] bottomRightCorner = {52.243275, 6.850786};

    //    Natural grass field
    private final double[] bottomLeftCorner = {52.24220658064753, 6.851613173106319};
    private final double[] topLeftCorner = {52.24269782485437, 6.850296926995616};
    private final double[] topRightCorner = {52.24322738698686, 6.850835563732699};
    private final double[] bottomRightCorner = {52.24273847129517, 6.8521556030598605};



    // ION ograda
//    private final double[] bottomLeftCorner = {52.226655975016556, 6.8647907602634675};
//    private final double[] topLeftCorner = {52.22667979791006, 6.864679448598995};
//    private final double[] topRightCorner = {52.22672415774671, 6.864711635104385};
//    private final double[] bottomRightCorner = {52.226698691919985, 6.864817582351293};
    private HeatmapOverlay heatmapOverlay;
    private List<LatLng> playerPositions;

    private FrameLayout footballPitch;
    int footballPitchWidth;
    int footballPitchHeight;
    private GoogleMap mMap;
    float zoomLevel = 18.5f;
    int rotationAngle = -60;
    private DatabaseHelper dbHelper;
    long startTimestamp;
    long endTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postgame_heatmap);

        // Get times
        Intent intentExtra = getIntent();
        String startTime = intentExtra.getStringExtra("EXTRA_START_TIME");
        String endTime = intentExtra.getStringExtra("EXTRA_END_TIME");
        String date = intentExtra.getStringExtra("EXTRA_DATE");

        // ---------- Initialize DB Helper ---------- //
        dbHelper = new DatabaseHelper(this);

        long[] timeStamps = PostGameStatistics.convertTimestamps(startTime, endTime, date);
        startTimestamp = timeStamps[0];
        endTimestamp = timeStamps[1];

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageButton returnBtn = findViewById(R.id.back_button);
        Button statisticsBtn = findViewById(R.id.tab_statistics);
        Button patternsBtn = findViewById(R.id.tab_patterns);

        returnBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PostGameHeatmap.this, MainActivity.class);
            startActivity(intent);
        });

        statisticsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PostGameHeatmap.this, PostGameStatistics.class);

            intent.putExtra("EXTRA_START_TIME", startTime);
            intent.putExtra("EXTRA_END_TIME", endTime);
            intent.putExtra("EXTRA_DATE", date);

            startActivity(intent);
        });
        patternsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PostGameHeatmap.this, PostGamePatterns.class);

            intent.putExtra("EXTRA_START_TIME", startTime);
            intent.putExtra("EXTRA_END_TIME", endTime);
            intent.putExtra("EXTRA_DATE", date);

            startActivity(intent);
        });


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

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(centerPoint)
                .zoom(zoomLevel)
                .bearing(rotationAngle)
                .tilt(0)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                // add all player positions
                loadPositionsData(startTimestamp, endTimestamp);
                List<PointF> screenPositions = convertToScreenPositions(playerPositions);

                heatmapOverlay = new HeatmapOverlay(PostGameHeatmap.this, screenPositions);
                footballPitch.addView(heatmapOverlay);
            }

            @Override
            public void onCancel() {
                loadPositionsData(startTimestamp, endTimestamp);
                List<PointF> screenPositions = convertToScreenPositions(playerPositions);

                heatmapOverlay = new HeatmapOverlay(PostGameHeatmap.this, screenPositions);
                footballPitch.addView(heatmapOverlay);
            }
        });


        //      Unable zoom and scroll on map
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setZoomGesturesEnabled(false);
        uiSettings.setScrollGesturesEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);

        // add all player positions
//        loadPositionsData(startTimestamp, endTimestamp);
//        List<PointF> screenPositions = convertToScreenPositions(playerPositions);
//
//        heatmapOverlay = new HeatmapOverlay(this, screenPositions);
//        footballPitch.addView(heatmapOverlay);
    }

    private void loadPositionsData(long startTimestamp, long endTimestamp) {
        Cursor cursor = dbHelper.getGpsData(startTimestamp, endTimestamp);
        playerPositions = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAT));
                double lon = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LON));
                playerPositions.add(new LatLng(lat, lon));
            }
            cursor.close();
        }
    }

    private List<PointF> convertToScreenPositions(List<LatLng> latLngPositions) {
        List<PointF> screenPositions = new ArrayList<>();
        for (LatLng latLng : latLngPositions) {
            // Log before conversion to check latLng values
            Log.d("HEATMAP - DEBUG", "Converting LatLng: " + latLng);

            PointF screenPosition = new PointF();
            android.graphics.Point screenPoint = mMap.getProjection().toScreenLocation(latLng);

            // Check if screenPoint is valid
            if (screenPoint.x >= 0 && screenPoint.y >= 0) {
                screenPosition.set(screenPoint.x, screenPoint.y);
                screenPositions.add(screenPosition);
                Log.d("HEATMAP - DEBUG", latLng + " , SCREEN POINT IS " + screenPoint);
            } else {
                Log.d("HEATMAP - DEBUG", "Invalid screen point for LatLng: " + latLng);
            }
        }
        return screenPositions;
    }
}
