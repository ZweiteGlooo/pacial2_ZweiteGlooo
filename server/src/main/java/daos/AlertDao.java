package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import config.ConnectionManager;
import model.Alert;

public class AlertDao implements Dao<Alert, Long> {

    @Override
    public synchronized Alert save(Alert alert) throws Exception {
        Connection con = ConnectionManager.getInstance().getConnection();
        String sql = "INSERT INTO alerts (patient_id, type, message, timestamp, acknowledged) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, alert.getPatientId());
            ps.setString(2, alert.getType());
            ps.setString(3, alert.getMessage());
            ps.setLong(4, alert.getTimestamp());
            ps.setBoolean(5, alert.isAcknowledged());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    alert.setId(rs.getLong(1));
                }
            }
        }
        con.close();
        return alert;
    }

    public synchronized List<Alert> findActiveByPatientId(String patientId) throws Exception {
        Connection con = ConnectionManager.getInstance().getConnection();
        String sql = "SELECT * FROM alerts WHERE patient_id = ? AND acknowledged = false ORDER BY timestamp DESC";
        List<Alert> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alert a = new Alert();
                    a.setId(rs.getLong("id"));
                    a.setPatientId(rs.getString("patient_id"));
                    a.setType(rs.getString("type"));
                    a.setMessage(rs.getString("message"));
                    a.setTimestamp(rs.getLong("timestamp"));
                    a.setAcknowledged(rs.getBoolean("acknowledged"));
                    list.add(a);
                }
            }
        }
        con.close();
        return list;
    }

    public synchronized void acknowledge(long id) throws Exception {
        Connection con = ConnectionManager.getInstance().getConnection();
        String sql = "UPDATE alerts SET acknowledged = true WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
        con.close();
    }

    @Override
    public Alert findById(Long id) throws Exception {
        return null;
    }

    @Override
    public List<Alert> findAll() throws Exception {
        return new ArrayList<>();
    }

    @Override
    public void update(Alert entity) throws Exception {}

    @Override
    public void delete(Long id) throws Exception {}
}
