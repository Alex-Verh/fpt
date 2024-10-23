package com.example.fpt_footballplayertracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView connectedDevice;
    private Button startTrackingButton;
    private Button stopTrackingButton;
    private Button seeDataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.realtracking_patterns);

//        connectedDevice = findViewById(R.id.connected_device);
//        startTrackingButton = findViewById(R.id.start_tracking_button);
//        stopTrackingButton = findViewById(R.id.stop_tracking_button);
//        seeDataButton = findViewById(R.id.see_data_button);

//        stopTrackingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "Tracking stopped.", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        seeDataButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(MainActivity.this, "Displaying tracker data.", Toast.LENGTH_SHORT).show();
//            }
//        });
    }



// RELATIVE POSITIONING OF THE PLAYER ON THE FOOTBALL PITCH ( THE IMAGE RELATIVE TO Kunstgrasveld Enschede)

//// Assuming you have the four corner GPS coordinates:
//double topLeftLat = ...; 52.243236, 6.848874
//double topLeftLng = ...; 52.243754, 6.849390
//double bottomRightLat = ...; 52.242740, 6.850181
//double bottomRightLng = ...; 52.243262, 6.850697
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
//double latFraction = (playerLat - bottomRightLat) / (topLeftLat - bottomRightLat);
//double lngFraction = (playerLng - topLeftLng) / (bottomRightLng - topLeftLng);
//
//// Convert to pixel coordinates:
//int xPos = (int) (lngFraction * imageWidth);
//int yPos = (int) (latFraction * imageHeight);
//
//// Update the player's position on the ImageView:
//playerMarker.setX(xPos);
//playerMarker.setY(yPos);
}
