package model;

public class VitalSigns {
    private String patientId;
    private double heartRate;          // Latidos por minuto (bpm)
    private double oxygenSaturation;   // SpO2 en porcentaje (%)
    private double systolicPressure;   // Presión sistólica en mmHg
    private double temperature;        // Grados Celsius (°C)
    private long timestamp;

    public VitalSigns() {
        this.timestamp = System.currentTimeMillis();
    }

    public VitalSigns(String patientId, double heartRate, double oxygenSaturation, double systolicPressure, double temperature) {
        this.patientId = patientId;
        this.heartRate = heartRate;
        this.oxygenSaturation = oxygenSaturation;
        this.systolicPressure = systolicPressure;
        this.temperature = temperature;
        this.timestamp = System.currentTimeMillis();
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public double getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(double heartRate) {
        this.heartRate = heartRate;
    }

    public double getOxygenSaturation() {
        return oxygenSaturation;
    }

    public void setOxygenSaturation(double oxygenSaturation) {
        this.oxygenSaturation = oxygenSaturation;
    }

    public double getSystolicPressure() {
        return systolicPressure;
    }

    public void setSystolicPressure(double systolicPressure) {
        this.systolicPressure = systolicPressure;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
