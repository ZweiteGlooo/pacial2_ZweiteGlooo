import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import dtos.Request;
import dtos.Response;
import model.Alert;
import model.Patient;
import model.VitalSigns;
import services.UciService;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UciIntegrationTest {

    private static final int TCP_PORT = 5000;
    private static final int UDP_PORT = 5001;
    private static Gson gson = new Gson();
    private static Server server;

    @BeforeClass
    public static void setUp() throws Exception {
        System.out.println("Iniciando pruebas de integración UCI Monitor...");
        server = new Server();
        new Thread(() -> {
            try {
                server.init(TCP_PORT, UDP_PORT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(1500); // Esperar inicialización del servidor
    }

    @AfterClass
    public static void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Prueba 1: Verifica que la capa de lógica de negocio y persistencia
     * funcione correctamente de manera autónoma.
     */
    @Test
    public void A_testBusinessServiceDirectly() throws Exception {
        UciService service = new UciService();
        Patient p = service.admitPatient("P-DIRECT-1", "Paciente Prueba", "UCI-DIRECT");
        assertNotNull(p);
        assertEquals("P-DIRECT-1", p.getId());

        // Lectura normal
        VitalSigns normal = new VitalSigns("P-DIRECT-1", 75.0, 98.0, 120.0, 36.5);
        List<Alert> alerts = service.recordVitalSigns(normal);
        assertTrue("No deberían dispararse alertas con signos normales", alerts.isEmpty());

        // Lectura crítica (Frecuencia cardíaca alta: 145 bpm)
        VitalSigns critico = new VitalSigns("P-DIRECT-1", 145.0, 97.0, 130.0, 36.8);
        List<Alert> alertsCriticas = service.recordVitalSigns(critico);
        assertFalse("Debe dispararse al menos una alerta", alertsCriticas.isEmpty());
        assertEquals("TAQUICARDIA", alertsCriticas.get(0).getType());

        // Consulta de paciente completo
        Patient updated = service.getPatient("P-DIRECT-1");
        assertNotNull(updated.getLatestVitals());
        assertEquals(145.0, updated.getLatestVitals().getHeartRate(), 0.01);
    }

    private Response sendTcpRequest(Request request) throws Exception {
        try (Socket socket = new Socket("localhost", TCP_PORT);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            socket.setSoTimeout(4000);
            String jsonOut = gson.toJson(request);
            writer.write(jsonOut);
            writer.newLine();
            writer.flush();

            String line = reader.readLine();
            assertNotNull("El servidor TCP no devolvió respuesta", line);
            return gson.fromJson(line, Response.class);
        }
    }

    private void sendUdpTelemetry(VitalSigns vitals) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            String json = gson.toJson(vitals);
            byte[] buffer = json.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), UDP_PORT);
            socket.send(packet);
        }
    }

    /**
     * Prueba 2: Admisión de un paciente vía Socket TCP
     */
    @Test
    public void B_testAdmitPatientViaTCP() throws Exception {
        Request req = new Request("ADMIT_PATIENT", Map.of(
                "id", "P-101",
                "name", "Laura Restrepo",
                "bedNumber", "CAMA-05"
        ));
        Response resp = sendTcpRequest(req);
        assertEquals("ok", resp.status);

        // Consultar paciente
        Request getReq = new Request("GET_PATIENT", Map.of("patientId", "P-101"));
        Response getResp = sendTcpRequest(getReq);
        assertEquals("ok", getResp.status);

        JsonObject dataObj = gson.toJsonTree(getResp.data).getAsJsonObject();
        assertEquals("Laura Restrepo", dataObj.get("name").getAsString());
        assertEquals("CAMA-05", dataObj.get("bedNumber").getAsString());
    }

    /**
     * Prueba 3: Actualizar umbrales clínicos vía Socket TCP
     */
    @Test
    public void C_testSetThresholdsViaTCP() throws Exception {
        Request req = new Request("SET_THRESHOLDS", Map.of(
                "patientId", "P-101",
                "minHeartRate", 55.0,
                "maxHeartRate", 110.0,
                "minOxygenSaturation", 92.0,
                "maxTemperature", 37.8
        ));
        Response resp = sendTcpRequest(req);
        assertEquals("ok", resp.status);
    }

    /**
     * Prueba 4: Envío de telemetría de signos normales vía DatagramSocket UDP
     * y verificación de actualización del estado clínico vía TCP
     */
    @Test
    public void D_testNormalTelemetryViaUDP() throws Exception {
        VitalSigns signs = new VitalSigns("P-101", 72.0, 98.5, 115.0, 36.6);
        sendUdpTelemetry(signs);

        // Dar un instante breve para que el hilo UDP procese el datagrama
        Thread.sleep(300);

        Request getReq = new Request("GET_PATIENT", Map.of("patientId", "P-101"));
        Response getResp = sendTcpRequest(getReq);
        assertEquals("ok", getResp.status);

        JsonObject dataObj = gson.toJsonTree(getResp.data).getAsJsonObject();
        JsonObject vitalsObj = dataObj.getAsJsonObject("latestVitals");
        assertNotNull("Debe existir latestVitals en el paciente", vitalsObj);
        assertEquals(72.0, vitalsObj.get("heartRate").getAsDouble(), 0.01);
        assertEquals(98.5, vitalsObj.get("oxygenSaturation").getAsDouble(), 0.01);
    }

    /**
     * Prueba 5: Disparo de Alerta Crítica (Taquicardia e Hipoxia) recibida vía UDP
     * y validación de consulta de alertas activas vía TCP
     */
    @Test
    public void E_testCriticalTelemetryTriggerAlerts() throws Exception {
        // Enviar signos fuera de los umbrales (HR 130 > 110, SpO2 88 < 92)
        VitalSigns critico = new VitalSigns("P-101", 130.0, 88.0, 140.0, 37.0);
        sendUdpTelemetry(critico);

        Thread.sleep(300);

        Request alertsReq = new Request("GET_ACTIVE_ALERTS", Map.of("patientId", "P-101"));
        Response alertsResp = sendTcpRequest(alertsReq);
        assertEquals("ok", alertsResp.status);

        String jsonAlerts = gson.toJson(alertsResp.data);
        assertTrue("Debe registrarse alerta de TAQUICARDIA o HIPOXIA",
                jsonAlerts.contains("TAQUICARDIA") || jsonAlerts.contains("HIPOXIA"));
    }

    /**
     * Prueba 6: Concurrencia Masiva y Seguridad de Hilos.
     * 30 hilos enviando ráfagas UDP simultáneas mientras 20 hilos consultan por TCP.
     */
    @Test
    public void F_testConcurrencyUnderPressure() throws Exception {
        int nUdp = 30;
        int nTcp = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(nUdp + nTcp);

        List<Throwable> errors = new ArrayList<>();

        // Hilos emisores UDP
        for (int i = 0; i < nUdp; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    VitalSigns vs = new VitalSigns("P-101", 70.0 + (id % 20), 95.0, 120.0, 36.5);
                    sendUdpTelemetry(vs);
                } catch (Throwable t) {
                    synchronized (errors) {
                        errors.add(t);
                    }
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        // Hilos lectores TCP
        for (int i = 0; i < nTcp; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    Request req = new Request("GET_PATIENT", Map.of("patientId", "P-101"));
                    Response resp = sendTcpRequest(req);
                    assertEquals("ok", resp.status);
                } catch (Throwable t) {
                    synchronized (errors) {
                        errors.add(t);
                    }
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        // Iniciar todos simultáneamente
        startLatch.countDown();
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);

        assertTrue("La prueba concurrente tardó demasiado", finished);
        assertTrue("Ocurrieron errores durante la ejecución concurrente: " + errors, errors.isEmpty());
    }
}
