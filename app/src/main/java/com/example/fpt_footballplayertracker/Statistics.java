package com.example.fpt_footballplayertracker;

public class Statistics {
    public float topSpeed = 0;
    public float averageSpeed = 0;
    public float topHeartRate;
    public float averageHeartRate;
    public float totalDistanceCovered;
    public int numberOfSprints;
    public String wellbeing;
    public float performance;

    public void adjustSpeed(float currentSpeed){
        if(currentSpeed > topSpeed){
            topSpeed = currentSpeed;
        }

        averageSpeed = (averageSpeed + currentSpeed) / 2;
    }

    public void adjustDistance(float newDistance){
        totalDistanceCovered = newDistance;
    }

    public void adjustHeartRate(float currentHeartRate){
        if(currentHeartRate > topHeartRate){
            topHeartRate = currentHeartRate;
        }

        averageHeartRate = (averageHeartRate + currentHeartRate) / 2;
    }

    public void calculatePerformance(){
        float performanceScore = 0;
        performanceScore += Math.min(topSpeed / 20.0f, 2.0f);
        performanceScore += Math.min(averageSpeed / 10.0f, 2.0f);

        if (topHeartRate < 180) {
            performanceScore += 1.5f;
        } else if (topHeartRate <= 200) {
            performanceScore += 1.0f;
        } else {
            performanceScore += 0.5f;
        }

        performanceScore += Math.min(numberOfSprints / 20.0f, 2.0f);

        if ("good".equalsIgnoreCase(wellbeing)) {
            performanceScore += 2.0f;
        } else if ("tired".equalsIgnoreCase(wellbeing)) {
            performanceScore += 1.0f;
        } else {
            performanceScore += 0.5f;
        }

        performance = Math.min(performanceScore, 10.0f);
    }

}
