package model;

public class Thresholds {
    private String patientId;
    private double minHeartRate;
    private double maxHeartRate;
    private double minOxygenSaturation;
    private double maxTemperature;

    public Thresholds() {
        // Umbrales médicos por defecto
        this.minHeartRate = 50.0;
        this.maxHeartRate = 120.0;
        this.minOxygenSaturation = 90.0;
        this.maxTemperature = 38.0;
    }

    public Thresholds(String patientId, double minHeartRate, double maxHeartRate, double minOxygenSaturation, double maxTemperature) {
        this.patientId = patientId;
        this.minHeartRate = minHeartRate;
        this.maxHeartRate = maxHeartRate;
        this.minOxygenSaturation = minOxygenSaturation;
        this.maxTemperature = maxTemperature;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public double getMinHeartRate() {
        return minHeartRate;
    }

    public void setMinHeartRate(double minHeartRate) {
        this.minHeartRate = minHeartRate;
    }

    public double getMaxHeartRate() {
        return maxHeartRate;
    }

    public void setMaxHeartRate(double maxHeartRate) {
        this.maxHeartRate = maxHeartRate;
    }

    public double getMinOxygenSaturation() {
        return minOxygenSaturation;
    }

    public void setMinOxygenSaturation(double minOxygenSaturation) {
        this.minOxygenSaturation = minOxygenSaturation;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }
}
