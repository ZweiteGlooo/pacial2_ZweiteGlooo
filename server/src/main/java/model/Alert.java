package model;

public class Alert {
    private long id;
    private String patientId;
    private String type;      // "BRADICARDIA", "TAQUICARDIA", "HIPOXIA", "FIEBRE"
    private String message;
    private long timestamp;
    private boolean acknowledged;

    public Alert() {
        this.timestamp = System.currentTimeMillis();
        this.acknowledged = false;
    }

    public Alert(String patientId, String type, String message) {
        this.patientId = patientId;
        this.type = type;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.acknowledged = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }
}
