package com.example.fpt_footballplayertracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class RandomPositionGenerator {

    //    Kunstgrasveld
//    private static final double[] bottomLeftCorner = {52.242704, 6.850216};
//    private static final double[] topLeftCorner = {52.243232, 6.848823};
//    private static final double[] topRightCorner = {52.243797, 6.849397};
//    private static final double[] bottomRightCorner = {52.243275, 6.850786};

    //    Natural grass field
    private static final double[] bottomLeftCorner = {52.24220658064753, 6.851613173106319};
    private static final double[] topLeftCorner = {52.24269782485437, 6.850296926995616};
    private static final double[] topRightCorner = {52.24322738698686, 6.850835563732699};
    private static final double[] bottomRightCorner = {52.24273847129517, 6.8521556030598605};


    // ION ograda
//    private static final double[] bottomLeftCorner = {52.226655975016556, 6.8647907602634675};
//    private static final double[] topLeftCorner = {52.22667979791006, 6.864679448598995};
//    private static final double[] topRightCorner = {52.22672415774671, 6.864711635104385};
//    private static final double[] bottomRightCorner = {52.226698691919985, 6.864817582351293};

    private static final int NUM_POSITIONS = 70; // Number of random positions to generate
    private static final Random random = new Random();
    private static final String DATE = "2024-11-03";

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
            double lat = getRandomLatitude() + 0.0002;
            double lon = getRandomLongitude() - 0.0002;;

            String datetimeUtc = generateRandomTimestamp(DATE);
            String payloadString = String.format(Locale.US, "{'datetime_utc': '%s', 'lat': %.15f, 'lat_dir': '', 'lon': %.15f, 'lon_dir': '', 'speed': '%.2f', 'course': '%.2f'}",
                    datetimeUtc, lat, lon, random.nextDouble() * 12, random.nextDouble() * 360);
            payloadStrings.add(payloadString);
        }
        return payloadStrings;
    }

    public static List<String> generateRandomSprints() {
        List<String> payloadStrings = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            double lat = getRandomLatitude() + 0.0002;
            double lon = getRandomLongitude() - 0.0002;
            String datetimeUtc = generateRandomTimestamp(DATE);
            String payloadString = String.format(Locale.US, "{'datetime_utc': '%s', 'lat': %.15f, 'lon': %.15f, 'course': '%.2f'}",
                    datetimeUtc, lat, lon, random.nextDouble() * 360);
            payloadStrings.add(payloadString);
        }
        return payloadStrings;
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
