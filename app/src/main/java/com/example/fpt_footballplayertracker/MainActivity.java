package com.example.fpt_footballplayertracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
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
        setContentView(R.layout.activity_main);

        connectedDevice = findViewById(R.id.connected_device);
        startTrackingButton = findViewById(R.id.start_tracking_button);
        stopTrackingButton = findViewById(R.id.stop_tracking_button);
        seeDataButton = findViewById(R.id.see_data_button);

        stopTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Tracking stopped.", Toast.LENGTH_SHORT).show();
            }
        });

        seeDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Displaying tracker data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
