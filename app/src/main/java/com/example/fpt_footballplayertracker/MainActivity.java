package com.example.fpt_footballplayertracker;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private String startTimeSelected, endTimeSelected, dateSelected;
    Button startTime, endTime, resetTime, seeData;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.menu);


        // NAVIGATION BUTTONS
        Button startBtn = findViewById(R.id.real_time_data);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RealTimePositioning.class);
                startActivity(intent);
            }
        });


        startTime = findViewById(R.id.selected_start_time);
        startTime.setOnClickListener(view -> selectTimeDateDialog(true));

        endTime = findViewById(R.id.selected_end_time);
        endTime.setOnClickListener(view -> selectTimeDateDialog(false));

        resetTime = findViewById(R.id.reset_time);
        resetTime.setOnClickListener(view -> resetTimeDate());

        seeData = findViewById(R.id.see_data);
        seeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Objects.equals(startTimeSelected, "") && !Objects.equals(endTimeSelected, "") && !Objects.equals(dateSelected, "")) {
                    Intent intent = new Intent(MainActivity.this, PostGamePatterns.class);

                    intent.putExtra("EXTRA_START_TIME", startTimeSelected);
                    intent.putExtra("EXTRA_END_TIME", endTimeSelected);
                    intent.putExtra("EXTRA_DATE", dateSelected);

                    startActivity(intent);
                }
            }
        });
        // END NAVIGATION BUTTONS

    }

    private void selectTimeDateDialog(boolean isStart) {
        showTimePickerDialog(isStart);
        if (isStart) {
            showDatePickerDialog();
        }
    }

    @SuppressLint("SetTextI18n")
    private void resetTimeDate() {
        startTimeSelected = "";
        endTimeSelected = "";
        dateSelected = "";

        resetTime.setBackgroundColor(ContextCompat.getColor(this, R.color.button_inactive));
        resetTime.setClickable(false);

        seeData.setBackgroundColor(ContextCompat.getColor(this, R.color.button_inactive));
        seeData.setClickable(false);

        endTime.setClickable(false);
        endTime.setBackgroundColor(ContextCompat.getColor(this, R.color.button_inactive));

        startTime.setClickable(true);
        startTime.setBackgroundColor(ContextCompat.getColor(this, R.color.button_active));

        startTime.setText("Select Time");
        endTime.setText("Select Time");
    }

    private void showTimePickerDialog(boolean isStart) {
        // Get current time as default value
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create the TimePickerDialog
        @SuppressLint("SetTextI18n") TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, selectedMinute) -> {

            @SuppressLint("DefaultLocale") String time = String.format("%02d:%02d", hourOfDay, selectedMinute);

            if (isStart) {
                startTimeSelected = time;
                startTime.setText(startTimeSelected + " - " + dateSelected);

                // Enable End Time Button
                endTime.setBackgroundColor(ContextCompat.getColor(this, R.color.button_active));
                endTime.setClickable(true);

                // Enable Reset Button
                resetTime.setBackgroundColor(ContextCompat.getColor(this, R.color.button_active));
                resetTime.setClickable(true);
            } else {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                try {
                    Date d1 = sdf.parse(startTimeSelected);
                    Date d2 = sdf.parse(time);

                    assert d1 != null;
                    assert d2 != null;

                    if (d2.getTime() > d1.getTime()) {
                        endTimeSelected = time;
                        endTime.setText(endTimeSelected);

                        // Disable Start Time Button
                        startTime.setBackgroundColor(ContextCompat.getColor(this, R.color.button_inactive));
                        startTime.setClickable(false);

                        // Enable See Data Button
                        seeData.setBackgroundColor(ContextCompat.getColor(this, R.color.button_active));
                        seeData.setClickable(true);
                    } else {
                        endTime.setText("Wrong Time Period");
                        seeData.setBackgroundColor(ContextCompat.getColor(this, R.color.button_inactive));
                        seeData.setClickable(false);
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }

        }, hour, minute, true);

        timePickerDialog.show();
    }

    // Method to show the DatePickerDialog
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);  // Note: Month is 0-indexed
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create the DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            @SuppressLint("DefaultLocale") String date = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
            dateSelected = date;
        }, year, month, day);

        datePickerDialog.show();
    }

}
