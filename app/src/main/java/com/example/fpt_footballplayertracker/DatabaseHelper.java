package com.example.fpt_footballplayertracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fpt_data.db";
    private static final int DATABASE_VERSION = 3;

    // table names
    public static final String TABLE_GPS = "gps_data";
    public static final String TABLE_ACCEL = "accel_data";
    public static final String TABLE_PULSE = "pulse_data";
    public static final String TABLE_SPRINTS = "sprints";

    // common columns
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // GPS table columns
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LAT_DIR = "lat_dir";
    public static final String COLUMN_LON = "lon";
    public static final String COLUMN_LON_DIR = "lon_dir";
    public static final String COLUMN_SPEED = "speed";
    public static final String COLUMN_COURSE = "course";

    // accelerometer columns
    public static final String COLUMN_ACCEL_X = "accel_x";
    public static final String COLUMN_ACCEL_Y = "accel_y";
    public static final String COLUMN_ACCEL_Z = "accel_z";
    public static final String COLUMN_ACCEL_MAGNITUDE = "accel_magnitude";

    // pulse data
    public static final String COLUMN_PULSE_RATE = "pulse_rate";

    public DatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createGpsTable(db);
        createAccelTable(db);
        createPulseTable(db);
        createSprintsTable(db);
    }

    private void createGpsTable(SQLiteDatabase db) {
        String createGpsTable = "CREATE TABLE " + TABLE_GPS + " (" +
                COLUMN_TIMESTAMP + " INTEGER PRIMARY KEY, " +
                COLUMN_LAT + " REAL, " +
                COLUMN_LAT_DIR + " TEXT, " +
                COLUMN_LON + " REAL, " +
                COLUMN_LON_DIR + " TEXT, " +
                COLUMN_SPEED + " REAL, " +
                COLUMN_COURSE + " REAL)";
        db.execSQL(createGpsTable);
    }

    private void createAccelTable(SQLiteDatabase db) {
        String createAccelTable = "CREATE TABLE " + TABLE_ACCEL + " (" +
                COLUMN_TIMESTAMP + " INTEGER PRIMARY KEY, " +
                COLUMN_ACCEL_X + " REAL, " +
                COLUMN_ACCEL_Y + " REAL, " +
                COLUMN_ACCEL_Z + " REAL, " +
                COLUMN_ACCEL_MAGNITUDE + " REAL)";
        db.execSQL(createAccelTable);
    }

    private void createPulseTable(SQLiteDatabase db) {
        String createPulseTable = "CREATE TABLE " + TABLE_PULSE + " (" +
                COLUMN_TIMESTAMP + " INTEGER PRIMARY KEY, " +
                COLUMN_PULSE_RATE + " REAL)";
        db.execSQL(createPulseTable);
    }

    private void createSprintsTable(SQLiteDatabase db) {
        String createSprintsTable = "CREATE TABLE " + TABLE_SPRINTS + " (" +
                COLUMN_TIMESTAMP + " INTEGER PRIMARY KEY, " +
                COLUMN_LAT + " TEXT, " +
                COLUMN_LON + " TEXT, " +
                COLUMN_COURSE + " REAL)";
        db.execSQL(createSprintsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCEL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PULSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPRINTS);
        onCreate(db);
    }

    public void insertSprintsData(double lat, double lon, double course) {
        try {
            long timestamp = System.currentTimeMillis();
            SQLiteDatabase db = this.getWritableDatabase();
            String insertSprintData = "INSERT OR REPLACE INTO " + TABLE_SPRINTS + " (" +
                    COLUMN_TIMESTAMP + ", " + COLUMN_LAT + ", " + COLUMN_LON + ", " + COLUMN_COURSE + ") VALUES (?, ?, ?, ?)";
            db.execSQL(insertSprintData, new Object[]{timestamp, lat, lon, course});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertRawSprintsData(String payload) {
        try {
            JSONObject json = new JSONObject(payload);

            // convert datetime_utc to milliseconds
            String utcString = json.getString("datetime_utc");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Set to UTC

            // in case if parsing fails, store current millis instead
            long timestamp = System.currentTimeMillis();
            try {
                Date date = sdf.parse(utcString);
                timestamp = date.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            double lat = json.getDouble("lat");
            double lon = json.getDouble("lon");
            double course = json.getDouble("course");

            SQLiteDatabase db = this.getWritableDatabase();
            String insertSprintData = "INSERT OR REPLACE INTO " + TABLE_SPRINTS + " (" +
                    COLUMN_TIMESTAMP + ", " + COLUMN_LAT + ", " + COLUMN_LON + ", " + COLUMN_COURSE + ") VALUES (?, ?, ?, ?)";
            db.execSQL(insertSprintData, new Object[]{timestamp, lat, lon, course});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertGpsData(String payload) {
        try {
            JSONObject json = new JSONObject(payload);

            // convert datetime_utc to milliseconds
            String utcString = json.getString("datetime_utc");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Set to UTC

            // in case if parsing fails, store current millis instead
            long timestamp = System.currentTimeMillis();
            try {
                Date date = sdf.parse(utcString);
                timestamp = date.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            double lat = json.getDouble("lat");
            String latDir = json.getString("lat_dir");
            double lon = json.getDouble("lon");
            String lonDir = json.getString("lon_dir");
            double speed = json.getDouble("speed");
            double course = json.getDouble("course");

            SQLiteDatabase db = this.getWritableDatabase();
            String insertGpsData = "INSERT OR REPLACE INTO " + TABLE_GPS + " (" +
                    COLUMN_TIMESTAMP + ", " + COLUMN_LAT + ", " + COLUMN_LAT_DIR + ", " +
                    COLUMN_LON + ", " + COLUMN_LON_DIR + ", " + COLUMN_SPEED + ", " + COLUMN_COURSE +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?)";
            db.execSQL(insertGpsData, new Object[]{timestamp, lat, latDir, lon, lonDir, speed, course});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertAccelData(String payload) {
        try {
            long timestamp = System.currentTimeMillis();
            JSONObject json = new JSONObject(payload);
            double accelX = json.getDouble("Ax");
            double accelY = json.getDouble("Ay");
            double accelZ = json.getDouble("Az");
            double accelMagnitude = json.getDouble("Amag");

            SQLiteDatabase db = this.getWritableDatabase();
            String insertAccelData = "INSERT OR REPLACE INTO " + TABLE_ACCEL + " (" +
                    COLUMN_TIMESTAMP + ", " + COLUMN_ACCEL_X + ", " +
                    COLUMN_ACCEL_Y + ", " + COLUMN_ACCEL_Z + ", " + COLUMN_ACCEL_MAGNITUDE +
                    ") VALUES (?, ?, ?, ?, ?)";
            db.execSQL(insertAccelData, new Object[]{timestamp, accelX, accelY, accelZ, accelMagnitude});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertRawPulseData(String payload) {
        try {
            JSONObject json = new JSONObject(payload);

            // convert datetime_utc to milliseconds
            String utcString = json.getString("datetime_utc");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Set to UTC

            // in case if parsing fails, store current millis instead
            long timestamp = System.currentTimeMillis();
            try {
                Date date = sdf.parse(utcString);
                timestamp = date.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            double pulseRate = json.getDouble("pulseRate");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertPulseData(String payload) {
        try {
            long timestamp = System.currentTimeMillis();
            float pulseRate = Float.parseFloat(payload.trim());

            SQLiteDatabase db = this.getWritableDatabase();
            String insertPulseData = "INSERT OR REPLACE INTO " + TABLE_PULSE + " (" +
                    COLUMN_TIMESTAMP + ", " + COLUMN_PULSE_RATE + ") VALUES (?, ?)";
            db.execSQL(insertPulseData, new Object[]{timestamp, pulseRate});
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor getGpsData(long startTime, long stopTime) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_GPS + " WHERE " +
                COLUMN_TIMESTAMP + " BETWEEN ? AND ?";
        return db.rawQuery(query, new String[]{String.valueOf(startTime), String.valueOf(stopTime)});
    }

    public Cursor getAccelData(long startTime, long stopTime) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_ACCEL + " WHERE " +
                COLUMN_TIMESTAMP + " BETWEEN ? AND ?";
        return db.rawQuery(query, new String[]{String.valueOf(startTime), String.valueOf(stopTime)});
    }

    public Cursor getPulseData(long startTime, long stopTime) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PULSE + " WHERE " +
                COLUMN_TIMESTAMP + " BETWEEN ? AND ?";
        return db.rawQuery(query, new String[]{String.valueOf(startTime), String.valueOf(stopTime)});
    }

    public Cursor getSprintsData(long startTime, long stopTime) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SPRINTS + " WHERE " +
                COLUMN_TIMESTAMP + " BETWEEN ? AND ?";
        return db.rawQuery(query, new String[]{String.valueOf(startTime), String.valueOf(stopTime)});
    }
}
