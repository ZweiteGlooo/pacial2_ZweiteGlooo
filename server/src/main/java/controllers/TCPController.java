package controllers;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.google.gson.Gson;

import dtos.Request;
import dtos.Response;
import model.Alert;
import model.Thresholds;
import services.UciService;

/**
 * Controlador de Red TCP: Servidor de Comandos y Consultas Clínicas.
 */
public class TCPController implements Runnable {

    private ServerSocket serverSocket;
    private UciService uciService;
    private int port;
    private ExecutorService threadPool;
    private volatile boolean running;
    private Gson gson;

    public TCPController(UciService uciService, int port) {
        //TODO   
    }

    public void startService() {
        this.running = true;
        new Thread(this, "TCP-Server-Acceptor").start();
    }

    public void stopService() {
        this.running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (threadPool != null) {
                threadPool.shutdownNow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("TCP Command Controller listening on port " + port);
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleClient(clientSocket));
            } catch (SocketException e) {
                if (!running) break;
                e.printStackTrace();
            } catch (Exception e) {
                if (running) e.printStackTrace();
            }
        }
    }

    public void handleClient(Socket socket) {
        //TODO
    }

    public Response handleRequest(Request request) {
        if (request == null || request.action == null) {
            return Response.error("Petición inválida o acción ausente");
        }

        Map<String, Object> data = request.data;
        try {
            switch (request.action) {
                //TODO
                case "SET_THRESHOLDS": {
                    String patientId = getString(data, "patientId");
                    double minHr = getDouble(data, "minHeartRate", 50.0);
                    double maxHr = getDouble(data, "maxHeartRate", 120.0);
                    double minSpo2 = getDouble(data, "minOxygenSaturation", 90.0);
                    double maxTemp = getDouble(data, "maxTemperature", 38.0);
                    Thresholds th = new Thresholds(patientId, minHr, maxHr, minSpo2, maxTemp);
                    Thresholds updated = uciService.updateThresholds(patientId, th);
                    return Response.ok(updated);
                }
                case "GET_ACTIVE_ALERTS": {
                    String patientId = getString(data, "patientId");
                    List<Alert> alerts = uciService.getActiveAlerts(patientId);
                    return Response.ok(alerts);
                }
                default:
                    return Response.error("Acción desconocida: " + request.action);
            }
        } catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    private String getString(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private double getDouble(Map<String, Object> map, String key, double defaultVal) {
        if (map == null) return defaultVal;
        Object val = map.get(key);
        if (val == null) return defaultVal;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try {
            return Double.parseDouble(val.toString());
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
