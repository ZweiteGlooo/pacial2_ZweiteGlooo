package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import config.ConnectionManager;
import model.Patient;
import model.Thresholds;

public class PatientDao implements Dao<Patient, String> {

    @Override
    public synchronized Patient save(Patient patient) throws Exception {
        Connection con = ConnectionManager.getInstance().getConnection();
        String sql = "MERGE INTO patients (id, name, bed_number, admitted) KEY(id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patient.getId());
            ps.setString(2, patient.getName());
            ps.setString(3, patient.getBedNumber());
            ps.setBoolean(4, patient.isAdmitted());
            ps.executeUpdate();
        }

        if (patient.getThresholds() != null) {
            String sqlTh = "MERGE INTO thresholds (patient_id, min_hr, max_hr, min_spo2, max_temp) KEY(patient_id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psTh = con.prepareStatement(sqlTh)) {
                psTh.setString(1, patient.getId());
                psTh.setDouble(2, patient.getThresholds().getMinHeartRate());
                psTh.setDouble(3, patient.getThresholds().getMaxHeartRate());
                psTh.setDouble(4, patient.getThresholds().getMinOxygenSaturation());
                psTh.setDouble(5, patient.getThresholds().getMaxTemperature());
                psTh.executeUpdate();
            }
        }
        con.close();
        return patient;
    }

    @Override
    public synchronized Patient findById(String id) throws Exception {
        Connection con = ConnectionManager.getInstance().getConnection();
        String sql = "SELECT * FROM patients WHERE id = ?";
        Patient patient = null;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    patient = new Patient();
                    patient.setId(rs.getString("id"));
                    patient.setName(rs.getString("name"));
                    patient.setBedNumber(rs.getString("bed_number"));
                    patient.setAdmitted(rs.getBoolean("admitted"));
                }
            }
        }

        if (patient != null) {
            String sqlTh = "SELECT * FROM thresholds WHERE patient_id = ?";
            try (PreparedStatement psTh = con.prepareStatement(sqlTh)) {
                psTh.setString(1, id);
                try (ResultSet rsTh = psTh.executeQuery()) {
                    if (rsTh.next()) {
                        Thresholds th = new Thresholds(
                                id,
                                rsTh.getDouble("min_hr"),
                                rsTh.getDouble("max_hr"),
                                rsTh.getDouble("min_spo2"),
                                rsTh.getDouble("max_temp")
                        );
                        patient.setThresholds(th);
                    }
                }
            }
        }
        con.close();
        return patient;
    }

    @Override
    public synchronized List<Patient> findAll() throws Exception {
        Connection con = ConnectionManager.getInstance().getConnection();
        String sql = "SELECT * FROM patients WHERE admitted = true";
        List<Patient> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Patient p = new Patient();
                p.setId(rs.getString("id"));
                p.setName(rs.getString("name"));
                p.setBedNumber(rs.getString("bed_number"));
                p.setAdmitted(rs.getBoolean("admitted"));
                list.add(p);
            }
        }
        con.close();
        return list;
    }

    @Override
    public synchronized void update(Patient patient) throws Exception {
        save(patient);
    }

    @Override
    public synchronized void delete(String id) throws Exception {
        Connection con = ConnectionManager.getInstance().getConnection();
        String sql = "UPDATE patients SET admitted = false WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
        con.close();
    }
}
