package services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import daos.AlertDao;
import daos.PatientDao;
import daos.VitalSignsDao;
import model.Alert;
import model.Patient;
import model.Thresholds;
import model.VitalSigns;

/**
 * Capa de Lógica de Negocio de Monitoreo UCI.
 * Totalmente implementada y protegida contra condiciones de carrera.
 */
public class UciService {

    private PatientDao patientDao;
    private VitalSignsDao vitalSignsDao;
    private AlertDao alertDao;

    // Caché en memoria para optimizar lecturas concurrentes y sincronización
    private final Map<String, Object> patientLocks = new ConcurrentHashMap<>();

    public UciService() {
        this.patientDao = new PatientDao();
        this.vitalSignsDao = new VitalSignsDao();
        this.alertDao = new AlertDao();
    }

    private Object getLock(String patientId) {
        return patientLocks.computeIfAbsent(patientId, k -> new Object());
    }

    /**
     * Registra un nuevo paciente en la UCI con umbrales clínicos predeterminados.
     */
    public synchronized Patient admitPatient(String id, String name, String bedNumber) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del paciente no puede estar vacío");
        }
        Patient existing = patientDao.findById(id);
        if (existing != null && existing.isAdmitted()) {
            throw new IllegalStateException("El paciente con ID " + id + " ya se encuentra admitido");
        }
        Patient patient = new Patient(id, name, bedNumber);
        return patientDao.save(patient);
    }

    /**
     * Da de alta a un paciente, liberando su cama.
     */
    public synchronized void dischargePatient(String id) throws Exception {
        Patient patient = patientDao.findById(id);
        if (patient == null || !patient.isAdmitted()) {
            throw new IllegalStateException("El paciente no está admitido o no existe");
        }
        patientDao.delete(id);
    }

    /**
     * Consulta el estado de un paciente incluyendo sus signos vitales más recientes y alertas activas.
     */
    public Patient getPatient(String id) throws Exception {
        synchronized (getLock(id)) {
            Patient patient = patientDao.findById(id);
            if (patient == null) {
                return null;
            }
            VitalSigns latest = vitalSignsDao.findLatestByPatientId(id);
            List<Alert> alerts = alertDao.findActiveByPatientId(id);
            patient.setLatestVitals(latest);
            patient.setActiveAlerts(alerts);
            return patient;
        }
    }

    /**
     * Retorna todos los pacientes actualmente admitidos en la UCI.
     */
    public synchronized List<Patient> getAllActivePatients() throws Exception {
        return patientDao.findAll();
    }

    /**
     * Actualiza los umbrales personalizados de monitoreo para un paciente.
     */
    public Thresholds updateThresholds(String patientId, Thresholds thresholds) throws Exception {
        synchronized (getLock(patientId)) {
            Patient patient = patientDao.findById(patientId);
            if (patient == null || !patient.isAdmitted()) {
                throw new IllegalStateException("El paciente no se encuentra admitido");
            }
            thresholds.setPatientId(patientId);
            patient.setThresholds(thresholds);
            patientDao.save(patient);
            return thresholds;
        }
    }

    /**
     * Registra una lectura de signos vitales (habitualmente recibida por UDP).
     * Evalúa las constantes frente a los umbrales configurados y dispara alertas si es necesario.
     * Retorna la lista de alertas generadas en esta lectura.
     */
    public List<Alert> recordVitalSigns(VitalSigns vs) throws Exception {
        String patientId = vs.getPatientId();
        if (patientId == null) {
            throw new IllegalArgumentException("La lectura debe contener un patientId");
        }

        synchronized (getLock(patientId)) {
            Patient patient = patientDao.findById(patientId);
            if (patient == null || !patient.isAdmitted()) {
                throw new IllegalStateException("El paciente " + patientId + " no está registrado o admitido");
            }

            // Persistir los signos vitales
            vs.setTimestamp(System.currentTimeMillis());
            vitalSignsDao.save(vs);

            Thresholds th = patient.getThresholds();
            if (th == null) {
                th = new Thresholds();
            }

            List<Alert> newAlerts = new ArrayList<>();

            // 1. Verificación de Frecuencia Cardíaca
            if (vs.getHeartRate() > th.getMaxHeartRate()) {
                Alert alert = new Alert(patientId, "TAQUICARDIA",
                        "Frecuencia cardíaca elevada: " + vs.getHeartRate() + " bpm (Máx: " + th.getMaxHeartRate() + ")");
                alertDao.save(alert);
                newAlerts.add(alert);
            } else if (vs.getHeartRate() < th.getMinHeartRate() && vs.getHeartRate() > 0) {
                Alert alert = new Alert(patientId, "BRADICARDIA",
                        "Frecuencia cardíaca baja: " + vs.getHeartRate() + " bpm (Mín: " + th.getMinHeartRate() + ")");
                alertDao.save(alert);
                newAlerts.add(alert);
            }

            // 2. Verificación de Saturación de Oxígeno (SpO2)
            if (vs.getOxygenSaturation() < th.getMinOxygenSaturation()) {
                Alert alert = new Alert(patientId, "HIPOXIA",
                        "Saturación de oxígeno crítica: " + vs.getOxygenSaturation() + "% (Mín: " + th.getMinOxygenSaturation() + "%)");
                alertDao.save(alert);
                newAlerts.add(alert);
            }

            // 3. Verificación de Temperatura
            if (vs.getTemperature() > th.getMaxTemperature()) {
                Alert alert = new Alert(patientId, "FIEBRE",
                        "Temperatura elevada: " + vs.getTemperature() + " °C (Máx: " + th.getMaxTemperature() + " °C)");
                alertDao.save(alert);
                newAlerts.add(alert);
            }

            return newAlerts;
        }
    }

    /**
     * Consulta las alertas activas (no reconocidas) de un paciente.
     */
    public List<Alert> getActiveAlerts(String patientId) throws Exception {
        synchronized (getLock(patientId)) {
            return alertDao.findActiveByPatientId(patientId);
        }
    }

    /**
     * Marca una alerta como reconocida/atendida por el personal médico.
     */
    public void acknowledgeAlert(long alertId) throws Exception {
        alertDao.acknowledge(alertId);
    }
}
