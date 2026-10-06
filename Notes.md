# Guía Rápida de Sintaxis y Snippets de Red
### Computación en Internet I — Universidad Icesi

Esta hoja de referencia contiene los fragmentos de código atómicos y patrones sintácticos necesarios para la implementación de servicios en red. Su propósito es servir de apoyo para no tener que memorizar firmas de librerías estándar.

---

## 📑 Índice de Contenidos
1. [Sockets TCP](#1-sockets-tcp)
2. [Flujos de Entrada / Salida (I/O Streams)](#2-flujos-de-entrada--salida-io-streams)
3. [Sockets UDP (Datagramas)](#3-sockets-udp-datagramas)
4. [Serialización y Deserialización con Gson](#4-serialización-y-deserialización-con-gson)
5. [Concurrencia y Pool de Hilos](#5-concurrencia-y-pool-de-hilos)

---

## 1. Sockets TCP

### 1.1 Servidor: Apertura de puerto y aceptación de conexiones

```java
import java.net.ServerSocket;
import java.net.Socket;

// 1. Crear el ServerSocket en un puerto específico
ServerSocket serverSocket = new ServerSocket(5000);

// 2. Bucle para aceptar clientes continuamente
while (running) {
    // accept() es bloqueante: detiene la ejecución hasta que llega un cliente
    Socket clientSocket = serverSocket.accept();
    
    // Delegar la atención del socket a otro hilo o pool de hilos
}

// 3. Cerrar al apagar el servidor
serverSocket.close();
```

### 1.2 Cliente: Conexión saliente hacia un servidor

```java
import java.net.Socket;

// Conectar a localhost (o una IP) en el puerto indicado
Socket socket = new Socket("localhost", 5000);

// Configurar timeout opcional (en milisegundos) para lecturas
socket.setSoTimeout(3000);

// Cerrar al finalizar la comunicación
socket.close();
```

---

## 2. Flujos de Entrada / Salida (I/O Streams)

### 2.1 Lectura de texto por líneas (`BufferedReader`)

```java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

BufferedReader reader = new BufferedReader(
    new InputStreamReader(System.in, StandardCharsets.UTF_8)
);

// Leer una línea completa enviada por el cliente (bloqueante hasta encontrar '\n' o EOF)
String line = reader.readLine();

if (line != null && !line.trim().isEmpty()) {
    // Procesar el mensaje recibido
}
```

### 2.2 Escritura de texto por líneas (`BufferedWriter`)

```java
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

BufferedWriter writer = new BufferedWriter(
    new OutputStreamWriter(System.out, StandardCharsets.UTF_8)
);

// Escribir el contenido (ej. JSON)
writer.write(data);

// Enviar el delimitador de fin de mensaje
writer.newLine(); // Equivale a escribir "\n"

// IMPORTANTE: Vaciar el búfer inmediatamente para asegurar que el dato viaje por la red
writer.flush();
```

---

## 3. Sockets UDP (Datagramas)

### 3.1 Servidor/Receptor UDP

```java
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

// 1. Abrir el socket en el puerto UDP asignado
DatagramSocket udpSocket = new DatagramSocket(5001);

// 2. Preparar el búfer de recepción (tamaño suficiente para el payload esperado)
byte[] buffer = new byte[2048];

// 3. Crear el contenedor para recibir el paquete
DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

// 4. receive() bloquea hasta recibir un paquete
udpSocket.receive(packet);

// 5. Convertir a String usando ÚNICAMENTE los bytes recibidos (packet.getLength())
String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);



// 6. Cerrar el socket al detener el servicio
udpSocket.close();
```

### 3.2 Cliente/Emisor UDP

```java
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

DatagramSocket socket = new DatagramSocket();

String mensaje = "Hello";
byte[] bytes = mensaje.getBytes(StandardCharsets.UTF_8);

InetAddress host = InetAddress.getByName("localhost");
int port = 5001;

DatagramPacket packet = new DatagramPacket(bytes, bytes.length, host, port);
socket.send(packet);

socket.close();
```

---

## 4. Serialización y Deserialización con Gson

### 4.1 Instanciación
```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

Gson gson = new Gson();
// O si requieres formato legible para depurar:
// Gson gson = new GsonBuilder().setPrettyPrinting().create();
```

### 4.2 De Objeto Java a JSON String (Serialización)
```java
MyObject obj = new MyObject("Carlos", 25);
String json = gson.toJson(obj);
// Resultado: {"name":"Carlos","age":25}
```

### 4.3 De JSON String a Objeto Java (Deserialización)
```java
String json = "{\"name\":\"Carlos\",\"age\":25}";
MyObject obj = gson.fromJson(json, MyObject.class);
```


---

## 5. Concurrencia y Pool de Hilos

### 5.1 Uso de Thread Pool (`ExecutorService`)
* **Rol:** Reutiliza un número acotado de hilos para procesar conexiones de clientes, evitando la sobrecarga de crear hilos infinitos (`new Thread()`).
* **Ubicación típica:** Controlador TCP.

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 1. Crear un pool con un número fijo de hilos trabajadores
ExecutorService threadPool = Executors.newFixedThreadPool(15);

// 2. Enviar tareas al pool (no bloqueante)
while (running) {
    threadPool.execute(() -> System.out.println("Hello thread"));
}

// 3. Apagado ordenado
threadPool.shutdown();     // Deja de aceptar nuevas tareas
// o threadPool.shutdownNow(); // Intenta interrumpir tareas activas
```


