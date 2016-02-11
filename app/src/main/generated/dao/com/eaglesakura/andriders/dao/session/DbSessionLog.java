package com.eaglesakura.andriders.dao.session;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 

/**
 * Entity mapped to table DB_SESSION_LOG.
 */
public class DbSessionLog {

    /** Not-null value. */
    private String sessionId;
    /** Not-null value. */
    private String profileId;
    private boolean googleFitUploaded;
    /** Not-null value. */
    private java.util.Date startTime;
    /** Not-null value. */
    private java.util.Date endTime;
    private long activeTimeMs;
    private double maxSpeedKmh;
    private int maxCadence;
    private int maxHeartrate;
    private double sumDistanceKm;
    private Double calories;
    private Double exercise;
    private Double sumAltitude;
    private byte[] extraPayload;

    public DbSessionLog() {
    }

    public DbSessionLog(String sessionId) {
        this.sessionId = sessionId;
    }

    public DbSessionLog(String sessionId, String profileId, boolean googleFitUploaded, java.util.Date startTime, java.util.Date endTime, long activeTimeMs, double maxSpeedKmh, int maxCadence, int maxHeartrate, double sumDistanceKm, Double calories, Double exercise, Double sumAltitude, byte[] extraPayload) {
        this.sessionId = sessionId;
        this.profileId = profileId;
        this.googleFitUploaded = googleFitUploaded;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activeTimeMs = activeTimeMs;
        this.maxSpeedKmh = maxSpeedKmh;
        this.maxCadence = maxCadence;
        this.maxHeartrate = maxHeartrate;
        this.sumDistanceKm = sumDistanceKm;
        this.calories = calories;
        this.exercise = exercise;
        this.sumAltitude = sumAltitude;
        this.extraPayload = extraPayload;
    }

    /** Not-null value. */
    public String getSessionId() {
        return sessionId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /** Not-null value. */
    public String getProfileId() {
        return profileId;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public boolean getGoogleFitUploaded() {
        return googleFitUploaded;
    }

    public void setGoogleFitUploaded(boolean googleFitUploaded) {
        this.googleFitUploaded = googleFitUploaded;
    }

    /** Not-null value. */
    public java.util.Date getStartTime() {
        return startTime;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setStartTime(java.util.Date startTime) {
        this.startTime = startTime;
    }

    /** Not-null value. */
    public java.util.Date getEndTime() {
        return endTime;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setEndTime(java.util.Date endTime) {
        this.endTime = endTime;
    }

    public long getActiveTimeMs() {
        return activeTimeMs;
    }

    public void setActiveTimeMs(long activeTimeMs) {
        this.activeTimeMs = activeTimeMs;
    }

    public double getMaxSpeedKmh() {
        return maxSpeedKmh;
    }

    public void setMaxSpeedKmh(double maxSpeedKmh) {
        this.maxSpeedKmh = maxSpeedKmh;
    }

    public int getMaxCadence() {
        return maxCadence;
    }

    public void setMaxCadence(int maxCadence) {
        this.maxCadence = maxCadence;
    }

    public int getMaxHeartrate() {
        return maxHeartrate;
    }

    public void setMaxHeartrate(int maxHeartrate) {
        this.maxHeartrate = maxHeartrate;
    }

    public double getSumDistanceKm() {
        return sumDistanceKm;
    }

    public void setSumDistanceKm(double sumDistanceKm) {
        this.sumDistanceKm = sumDistanceKm;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Double getExercise() {
        return exercise;
    }

    public void setExercise(Double exercise) {
        this.exercise = exercise;
    }

    public Double getSumAltitude() {
        return sumAltitude;
    }

    public void setSumAltitude(Double sumAltitude) {
        this.sumAltitude = sumAltitude;
    }

    public byte[] getExtraPayload() {
        return extraPayload;
    }

    public void setExtraPayload(byte[] extraPayload) {
        this.extraPayload = extraPayload;
    }

}
