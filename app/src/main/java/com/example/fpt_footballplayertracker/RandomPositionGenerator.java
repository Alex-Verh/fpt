package com.example.fpt_footballplayertracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class RandomPositionGenerator {
    // Define the corners of the football pitch
    private static final double[] bottomLeftCorner = {52.242704, 6.850216};
    private static final double[] topLeftCorner = {52.243232, 6.848823};
    private static final double[] topRightCorner = {52.243797, 6.849397};
    private static final double[] bottomRightCorner = {52.243275, 6.850786};

    private static final int NUM_POSITIONS = 100; // Number of random positions to generate
    private static final Random random = new Random();
    private static final String DATE = "2024-11-02";

    public static void main(String[] args) {
        List<String> payloadStrings = generateRandomPositions();
        // Print the generated payloads
        for (String payload : payloadStrings) {
            System.out.println(payload);
        }
    }

    // Method to generate random positions
    public static List<String> generateRandomPositions() {
        List<String> payloadStrings = new ArrayList<>();
        for (int i = 0; i < NUM_POSITIONS; i++) {
            double lat = getRandomLatitude();
            double lon = getRandomLongitude();
            String datetimeUtc = generateRandomTimestamp(DATE);
            String payloadString = String.format(Locale.US, "{'datetime_utc': '%s', 'lat': %.15f, 'lat_dir': '', 'lon': %.15f, 'lon_dir': '', 'speed': 0, 'course': '%.2f'}",
                    datetimeUtc, lat, lon, random.nextDouble() * 360);
            payloadStrings.add(payloadString);
        }
        return payloadStrings;
    }

    public static void generateRandomSprints(DatabaseHelper db) {
        List<String> payloadStrings = new ArrayList<>();
        for (int i = 0; i < NUM_POSITIONS; i++) {
            double lat = getRandomLatitude();
            double lon = getRandomLongitude();
            double course = random.nextDouble() * 360;
            db.insertSprintsData(lat, lon, course);
        }
    }

    public static List<String> generateRandomHearbeat() {
        List<String> payloadStrings = new ArrayList<>();
        for (int i = 0; i < NUM_POSITIONS; i++) {
            String datetimeUtc = generateRandomTimestamp(DATE);

            String payloadString = String.format(Locale.US, "{'datetime_utc': '%s', 'pulseRate': '%.2f'}",
                    datetimeUtc, 80 + random.nextDouble() * 50);
            payloadStrings.add(payloadString);
        }
        return payloadStrings;
    }


    // Generate a random latitude within the pitch boundaries
    private static double getRandomLatitude() {
        double minLat = bottomLeftCorner[0];
        double maxLat = topLeftCorner[0];
        return minLat + (maxLat - minLat) * random.nextDouble();
    }

    // Generate a random longitude within the pitch boundaries
    private static double getRandomLongitude() {
        double minLon = bottomLeftCorner[1];
        double maxLon = topRightCorner[1];
        return minLon + (maxLon - minLon) * random.nextDouble();
    }

    // Generate a random timestamp for November 5, 2024
    private static String generateRandomTimestamp(String baseDate) {
        int randomHour = random.nextInt(24);
        int randomMinute = random.nextInt(60);
        int randomSecond = random.nextInt(60);
        return String.format("%s %02d:%02d:%02d", baseDate, randomHour, randomMinute, randomSecond);
    }
}
