package com.example.fpt_footballplayertracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class RealTimePositioning extends AppCompatActivity {

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
//                intent.putExtra("key_name", "key_value");
                startActivity(intent);
            }
        });
        // END NAVIGATION BUTTONS
    }

// RELATIVE POSITIONING OF THE PLAYER ON THE FOOTBALL PITCH ( THE IMAGE RELATIVE TO Kunstgrasveld Enschede)

//// Assuming you have the four corner GPS coordinates:
//double bottomLeftCorner = ...; 52.242704, 6.850216
//double topLeftCorner  = ...; 52.243232, 6.848823
//double topRightCorner  = ...; 52.243797, 6.849397
//double bottomRightCorner  = ...; 552.243275, 6.850786
//
//// The player's current GPS coordinates:
//double playerLat = ...; X
//double playerLng = ...; Y
//
//// Image dimensions:
//int imageWidth = imageView.getWidth();
//int imageHeight = imageView.getHeight();
//
//// Calculate relative position:
//double latFraction = ...;
//double lngFraction = ...;
//
//// Convert to pixel coordinates:
//int xPos = (int) (lngFraction * imageWidth);
//int yPos = (int) (latFraction * imageHeight);
//
//// Update the player's position on the ImageView:
//playerMarker.setX(xPos);
//playerMarker.setY(yPos);
}