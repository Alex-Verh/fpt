package com.example.fpt_footballplayertracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.menu);

        // NAVIGATION BUTTONS
        Button startBtn = findViewById(R.id.start_tracking_button);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RealTimeTrackingPositioning.class);
                startActivity(intent);
            }
        });

        // END NAVIGATION BUTTONS
    }

}
