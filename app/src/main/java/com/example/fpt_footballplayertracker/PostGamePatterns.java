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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import android.widget.FrameLayout.LayoutParams;

import org.json.JSONObject;


public class PostGamePatterns extends AppCompatActivity implements OnMapReadyCallback {
//    private final double[] bottomLeftCorner = {52.242704, 6.850216};
//    private final double[] topLeftCorner = {52.243232, 6.848823};
//    private final double[] topRightCorner = {52.243797, 6.849397};
//    private final double[] bottomRightCorner = {52.243275, 6.850786};
private final double[] bottomLeftCorner = {52.226655975016556, 6.8647907602634675};
    private final double[] topLeftCorner = {52.22667979791006, 6.864679448598995};
    private final double[] topRightCorner = {52.22672415774671, 6.864711635104385};
    private final double[] bottomRightCorner = {52.226698691919985, 6.864817582351293};
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
        // TODO: remove this line
//        dbHelper.insertSprintsData(52.242377856892794, 6.851441534664767, 0);
//        dbHelper.insertSprintsData(52.24260281282532, 6.851509809267719, 60);
//        dbHelper.insertSprintsData(52.242708322737094, 6.851925959228571, 270);
//        dbHelper.insertSprintsData(52.242659176468024, 6.851195827314727, 210);
//        dbHelper.insertSprintsData(52.24255734963551, 6.850664749975794, 0);

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
        LatLng position = new LatLng(latitude, longitude);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.movement_pattern_arrow))
                .anchor(0.5f, 0.5f)
                .rotation(rotation);

        mMap.addMarker(markerOptions);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        LatLng bottomLeft = new LatLng(bottomLeftCorner[0], bottomLeftCorner[1]);
        LatLng topLeft = new LatLng(topLeftCorner[0], topLeftCorner[1]);
        LatLng topRight = new LatLng(topRightCorner[0], topRightCorner[1]);
        LatLng bottomRight = new LatLng(bottomRightCorner[0], bottomRightCorner[1]);

        LatLngBounds footballFieldBounds = new LatLngBounds(
                new LatLng(bottomLeftCorner[0], bottomLeftCorner[1]),  // southwest corner
                new LatLng(topRightCorner[0], topRightCorner[1])       // northeast corner
        );

        GroundOverlayOptions footballOverlay = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.football_field_image))
                .positionFromBounds(footballFieldBounds)
                .transparency(0.3f);

        mMap.addGroundOverlay(footballOverlay);

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