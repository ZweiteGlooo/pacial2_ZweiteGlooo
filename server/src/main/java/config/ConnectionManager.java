package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ConnectionManager {

    private String url;
    private String user;
    private String password;
    private static ConnectionManager instance;

    private ConnectionManager() {
        this.url = System.getenv("url") != null ? System.getenv("url") : "jdbc:h2:mem:uci_db;DB_CLOSE_DELAY=-1";
        this.user = System.getenv("user") != null ? System.getenv("user") : "sa";
        this.password = System.getenv("password") != null ? System.getenv("password") : "";
        initTables();
    }

    private ConnectionManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        initTables();
    }

    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public static synchronized ConnectionManager getInstance(String url, String user, String password) {
        if (instance == null) {
            instance = new ConnectionManager(url, user, password);
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initTables() {
        try (Connection con = DriverManager.getConnection(url, user, password);
             Statement stmt = con.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS patients (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "bed_number VARCHAR(50), " +
                    "admitted BOOLEAN" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS thresholds (" +
                    "patient_id VARCHAR(50) PRIMARY KEY, " +
                    "min_hr DOUBLE, " +
                    "max_hr DOUBLE, " +
                    "min_spo2 DOUBLE, " +
                    "max_temp DOUBLE" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS vital_signs (" +
                    "id IDENTITY PRIMARY KEY, " +
                    "patient_id VARCHAR(50), " +
                    "hr DOUBLE, " +
                    "spo2 DOUBLE, " +
                    "systolic DOUBLE, " +
                    "temp DOUBLE, " +
                    "timestamp BIGINT" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS alerts (" +
                    "id IDENTITY PRIMARY KEY, " +
                    "patient_id VARCHAR(50), " +
                    "type VARCHAR(50), " +
                    "message VARCHAR(255), " +
                    "timestamp BIGINT, " +
                    "acknowledged BOOLEAN" +
                    ")");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
