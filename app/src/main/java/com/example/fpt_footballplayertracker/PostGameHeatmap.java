package com.example.fpt_footballplayertracker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;


public class PostGameHeatmap extends AppCompatActivity implements OnMapReadyCallback {

    private final double[] bottomLeftCorner = {52.242704, 6.850216};
    private final double[] topLeftCorner = {52.243232, 6.848823};
    private final double[] topRightCorner = {52.243797, 6.849397};
    private final double[] bottomRightCorner = {52.243275, 6.850786};
    private HeatmapOverlay heatmapOverlay;
    private List<LatLng> playerPositions;

    private FrameLayout footballPitch;
    int footballPitchWidth;
    int footballPitchHeight;
    private GoogleMap mMap;
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

        assert date != null;
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
        float zoomLevel = 18.9f;
        int rotationAngle = -60;


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(centerPoint)
                .zoom(zoomLevel)
                .bearing(rotationAngle)
                .tilt(0)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        // add all player positions
        loadPositionsData(startTimestamp, endTimestamp);
        List<PointF> screenPositions = convertToScreenPositions(playerPositions);

        heatmapOverlay = new HeatmapOverlay(this, screenPositions);
        footballPitch.addView(heatmapOverlay);
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
            PointF screenPosition = new PointF();
            android.graphics.Point screenPoint = mMap.getProjection().toScreenLocation(latLng);
            screenPosition.set(screenPoint.x, screenPoint.y);
            screenPositions.add(screenPosition);
        }
        return screenPositions;
    }
}
