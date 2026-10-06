package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import config.ConnectionManager;
import model.VitalSigns;

public class VitalSignsDao implements Dao<VitalSigns, Long> {

    @Override
    public synchronized VitalSigns save(VitalSigns vs) throws Exception {
        Connection con = ConnectionManager.getInstance().getConnection();
        String sql = "INSERT INTO vital_signs (patient_id, hr, spo2, systolic, temp, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vs.getPatientId());
            ps.setDouble(2, vs.getHeartRate());
            ps.setDouble(3, vs.getOxygenSaturation());
            ps.setDouble(4, vs.getSystolicPressure());
            ps.setDouble(5, vs.getTemperature());
            ps.setLong(6, vs.getTimestamp());
            ps.executeUpdate();
        }
        con.close();
        return vs;
    }

    public synchronized VitalSigns findLatestByPatientId(String patientId) throws Exception {
        Connection con = ConnectionManager.getInstance().getConnection();
        String sql = "SELECT * FROM vital_signs WHERE patient_id = ? ORDER BY timestamp DESC LIMIT 1";
        VitalSigns vs = null;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    vs = new VitalSigns(
                            rs.getString("patient_id"),
                            rs.getDouble("hr"),
                            rs.getDouble("spo2"),
                            rs.getDouble("systolic"),
                            rs.getDouble("temp")
                    );
                    vs.setTimestamp(rs.getLong("timestamp"));
                }
            }
        }
        con.close();
        return vs;
    }

    @Override
    public VitalSigns findById(Long id) throws Exception {
        return null;
    }

    @Override
    public List<VitalSigns> findAll() throws Exception {
        return new ArrayList<>();
    }

    @Override
    public void update(VitalSigns entity) throws Exception {}

    @Override
    public void delete(Long id) throws Exception {}
}
