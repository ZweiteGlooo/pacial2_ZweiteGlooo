# Examen Práctico: Monitoreo de Cuidados Críticos (UCI Monitor)
### Computación en Internet I — Universidad Icesi

**Tiempo para desarrollarlo:** 1 hora y 40 minutos  
**Arquitectura del sistema:** Híbrida (TCP + UDP)  

---

## ⚠️ Consideraciones y restricciones


2. Está estrictamente prohibido el uso de herramientas de Inteligencia Artificial Generativa (ChatGPT, Copilot, Gemini, Claude, etc.).
3. De incurrir en alguna falta ética o violación a las normas, se anulará la evaluación y se remitirá a comité disciplinario.
4. En la raíz del repositorio cuentas con el archivo [`Notes.md`](Notes.md), que contiene fragmentos atómicos de sintaxis estándar (sockets TCP/UDP, streams I/O, serialización Gson y pools de hilos) para consulta durante el examen.

---

## 🏥 Contexto del sistema

Haces parte del equipo de ingeniería de un hospital universitario y estás a cargo de implementar la infraestructura de comunicación en red de **UCI Monitor**, una plataforma para la gestión de pacientes y la recepción de telemetría médica en unidades de cuidados intensivos.

El sistema debe operar a través de dos canales de red independientes y concurrentes:
1. **Canal de Telemetría (UDP - Puerto 5001):** Recibe ráfagas periódicas de signos vitales emitidas por los sensores de cada cama de hospital (frecuencia cardíaca, saturación de oxígeno, presión sistólica y temperatura).
2. **Canal de Control y Consultas (TCP - Puerto 5000):** Expone un protocolo orientado a conexión para que las estaciones de enfermería y personal médico puedan admitir pacientes, consultar su estado clínico actual, calibrar umbrales de alerta y dar altas médicas.

> ℹ️ **Nota sobre la arquitectura:**  
> La capa de **Lógica de Negocio (`UciService`)** y de **Persistencia (`daos` / Base de Datos H2)** ya se encuentra **completamente implementada y probada**.  
> Tu objetivo en este examen es construir la capa de **Controladores de Red** que expone y conecta estas capacidades hacia la red.

---

## 🎯 ¿Qué debes completar?

En el proyecto se suministran los esqueletos correspondientes dentro del paquete `server/src/main/java/`:

1. **Receptor de Telemetría UDP (`controllers/UDPController.java`):**
   * Poner en escucha el servicio en el puerto UDP configurado (por defecto `5001`).
   * Recibir continuamente los paquetes de telemetría enviados por los sensores médicos.
   * Procesar el contenido de los paquetes e integrarlo con la capa de negocio para registrar las mediciones y disparar las alertas clínicas pertinentes si los valores escapan de los rangos seguros.
   * Garantizar que la recepción de telemetría no se interrumpa ni bloquee la ejecución del sistema.

2. **Servidor de Control TCP (`controllers/TCPController.java`):**
   * Poner en escucha el servicio en el puerto TCP configurado (por defecto `5000`).
   * Atender múltiples clientes concurrentes de forma simultánea.
   * Procesar las solicitudes que llegan a través de conexiones TCP bajo el protocolo definido (mensajes JSON por línea).
   * Despachar cada acción hacia la lógica de negocio y retornar la respuesta correspondiente (`status: "ok"` o `status: "error"`).
   * Cerrar los recursos y conexiones de forma ordenada cuando corresponda.

3. **Orquestación del Servidor Principal (`Server.java`):**
   * Integrar y poner en marcha ambos canales (TCP y UDP) en el método `init(int tcpPort, int udpPort)`.
   * Proveer el mecanismo de apagado limpio de ambos controladores en `stop()`.

---

## 📡 Protocolo de Comunicación

### 1. Canal UDP: Telemetría de Signos Vitales (Puerto 5001)

Cada sensor envía un datagrama UDP que contiene una estructura JSON con las lecturas biomédicas del paciente:

```json
{
  "patientId": "P-101",
  "heartRate": 135.0,
  "oxygenSaturation": 88.0,
  "systolicPressure": 120.0,
  "temperature": 37.2
}
```

---

### 2. Canal TCP: Comandos y Consultas (Puerto 5000)

Las solicitudes y respuestas se transmiten a través de sockets TCP mediante una línea de texto JSON terminada en salto de línea (`\n`).

#### Estructura general de solicitud:
```json
{
  "action": "<NOMBRE_ACCION>",
  "data": { ... }
}
```

#### Estructura general de respuesta:
* **Operación exitosa:**
  ```json
  {
    "status": "ok",
    "data": { ... }
  }
  ```
* **Operación fallida o error de validación:**
  ```json
  {
    "status": "error",
    "data": {
      "message": "<Detalle del error>"
    }
  }
  ```

---

### Acciones del protocolo TCP:

#### 1. Admisión de Paciente (`ADMIT_PATIENT`)
Registra el ingreso de un paciente a una cama específica de la UCI.

* **Solicitud:**
  ```json
  {
    "action": "ADMIT_PATIENT",
    "data": {
      "id": "P-101",
      "name": "Carlos Gomez",
      "bedNumber": "UCI-03"
    }
  }
  ```
* **Respuesta esperada:**
  ```json
  {
    "status": "ok",
    "data": {
      "id": "P-101",
      "name": "Carlos Gomez",
      "bedNumber": "UCI-03",
      "admitted": true
    }
  }
  ```

#### 2. Consulta de Paciente (`GET_PATIENT`)
Retorna los datos del paciente, sus signos vitales más recientes y el listado de alertas clínicas activas.

* **Solicitud:**
  ```json
  {
    "action": "GET_PATIENT",
    "data": {
      "patientId": "P-101"
    }
  }
  ```
* **Respuesta esperada:**
  ```json
  {
    "status": "ok",
    "data": {
      "id": "P-101",
      "name": "Carlos Gomez",
      "bedNumber": "UCI-03",
      "admitted": true,
      "latestVitals": {
        "heartRate": 135.0,
        "oxygenSaturation": 88.0,
        "systolicPressure": 120.0,
        "temperature": 37.2
      },
      "activeAlerts": [
        {
          "type": "TAQUICARDIA",
          "message": "Frecuencia cardíaca elevada: 135.0 bpm (Máx: 120.0)"
        }
      ]
    }
  }
  ```

#### 3. Configuración de Umbrales de Alerta (`SET_THRESHOLDS`)
Ajusta los límites clínicos permitidos para el paciente (frecuencia cardíaca mínima y máxima, saturación mínima y temperatura máxima).

* **Solicitud:**
  ```json
  {
    "action": "SET_THRESHOLDS",
    "data": {
      "patientId": "P-101",
      "minHeartRate": 50.0,
      "maxHeartRate": 110.0,
      "minOxygenSaturation": 92.0,
      "maxTemperature": 38.0
    }
  }
  ```
* **Respuesta esperada:**
  ```json
  {
    "status": "ok",
    "data": {
      "patientId": "P-101",
      "minHeartRate": 50.0,
      "maxHeartRate": 110.0,
      "minOxygenSaturation": 92.0,
      "maxTemperature": 38.0
    }
  }
  ```

#### 4. Consulta de Alertas Activas (`GET_ACTIVE_ALERTS`)
Consulta únicamente las alertas médicas vigentes que requieren atención.

* **Solicitud:**
  ```json
  {
    "action": "GET_ACTIVE_ALERTS",
    "data": {
      "patientId": "P-101"
    }
  }
  ```
* **Respuesta esperada:**
  ```json
  {
    "status": "ok",
    "data": [
      {
        "id": 1,
        "patientId": "P-101",
        "type": "TAQUICARDIA",
        "message": "Frecuencia cardíaca elevada: 135.0 bpm (Máx: 120.0)",
        "acknowledged": false
      }
    ]
  }
  ```

#### 5. Alta Médica (`DISCHARGE_PATIENT`)
Da de alta al paciente y libera la cama correspondiente.

* **Solicitud:**
  ```json
  {
    "action": "DISCHARGE_PATIENT",
    "data": {
      "patientId": "P-101"
    }
  }
  ```
* **Respuesta esperada:**
  ```json
  {
    "status": "ok",
    "data": {
      "message": "Paciente dado de alta"
    }
  }
  ```

---

## 📝 Pregunta de Análisis y Comprensión Conceptual (15%)

Responde de manera clara y concisa la siguiente pregunta directamente al final de este archivo o en un archivo denominado `RESPUESTAS.md`:

> **Pregunta:**  
> Nombre al menos **dos elementos o decisiones de diseño** que se tienen en cuenta dentro de la implementación del sistema para garantizar un buen manejo de la concurrencia y evitar condiciones de carrera o degradación del servicio.  
> Justifique brevemente qué rol cumple cada uno.

### Espacio para tu respuesta:
* **Elemento 1 y Justificación:**
  *(Escribe aquí tu respuesta)*

* **Elemento 2 y Justificación:**
  *(Escribe aquí tu respuesta)*

---

## 🧪 Validación y Pruebas Automatizadas

La calificación práctica del examen se basa en la ejecución de pruebas automatizadas que simulan clientes TCP y emisores UDP en escenarios individuales y concurrentes.

Para ejecutar las pruebas:

```bash
./gradlew test
```

También puedes ejecutar la clase de prueba `UciIntegrationTest` directamente desde el IDE (IntelliJ IDEA o VS Code).

---

## 📊 Criterios de Evaluación

| Criterio | Descripción | Peso |
| :--- | :--- | :---: |
| **Controlador de Telemetría UDP** | Recepción continua de datagramas UDP, decodificación y registro de signos vitales en el sistema. | **20%** |
| **Controlador de Comandos TCP** | Comunicación sobre sockets TCP, cumplimiento riguroso del protocolo de solicitud/respuesta JSON y despacho de acciones. | **25%** |
| **Concurrencia y Estabilidad** | Capacidad del servidor para atender clientes simultáneos y procesar ráfagas de telemetría sin bloqueos ni caídas. | **15%** |
| **Pruebas Automatizadas** | Porcentaje de casos de prueba aprobados en la suite de integración (`./gradlew test`). | **25%** |
| **Pregunta Conceptual (Concurrencia)** | Identificación y justificación clara de los mecanismos de concurrencia y sincronización presentes en el sistema. | **15%** |
| **Total** | | **100%** |

