package model;

import java.util.ArrayList;
import java.util.List;

public class Patient {
    private String id;
    private String name;
    private String bedNumber;
    private boolean admitted;
    private Thresholds thresholds;
    private VitalSigns latestVitals;
    private List<Alert> activeAlerts;

    public Patient() {
        this.admitted = true;
        this.thresholds = new Thresholds();
        this.activeAlerts = new ArrayList<>();
    }

    public Patient(String id, String name, String bedNumber) {
        this.id = id;
        this.name = name;
        this.bedNumber = bedNumber;
        this.admitted = true;
        this.thresholds = new Thresholds();
        this.thresholds.setPatientId(id);
        this.activeAlerts = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        if (this.thresholds != null) {
            this.thresholds.setPatientId(id);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBedNumber() {
        return bedNumber;
    }

    public void setBedNumber(String bedNumber) {
        this.bedNumber = bedNumber;
    }

    public boolean isAdmitted() {
        return admitted;
    }

    public void setAdmitted(boolean admitted) {
        this.admitted = admitted;
    }

    public Thresholds getThresholds() {
        return thresholds;
    }

    public void setThresholds(Thresholds thresholds) {
        this.thresholds = thresholds;
    }

    public VitalSigns getLatestVitals() {
        return latestVitals;
    }

    public void setLatestVitals(VitalSigns latestVitals) {
        this.latestVitals = latestVitals;
    }

    public List<Alert> getActiveAlerts() {
        return activeAlerts;
    }

    public void setActiveAlerts(List<Alert> activeAlerts) {
        this.activeAlerts = activeAlerts;
    }
}
