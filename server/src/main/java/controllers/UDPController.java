package controllers;

import java.net.DatagramSocket;

import com.google.gson.Gson;

import services.UciService;

/**
 * Controlador de Red UDP: Receptor de Telemetría de Signos Vitales.
 */
public class UDPController implements Runnable {

    private DatagramSocket socket;
    private UciService uciService;
    private int port;
    private volatile boolean running;
    private Gson gson;

    public UDPController(UciService uciService, int port) {
        //TODO
    }

    public void startService() {
        this.running = true;
        new Thread(this, "UDP-Telemetry-Worker").start();
    }

    public void stopService() {
        this.running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    @Override
    public void run() {
        System.out.println("UDP Telemetry Controller listening on port " + port);
        byte[] buffer = new byte[2048];
        while (running) {
            // TODO: Save vitals using this service: uciService.recordVitalSigns(vitals);
            
        }
    }
}
