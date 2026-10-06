import controllers.TCPController;
import controllers.UDPController;
import services.UciService;

/**
 * Servidor Principal del Sistema de Monitoreo UCI.
 */
public class Server {

    private UciService uciService;
    private TCPController tcpController;
    private UDPController udpController;

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.init(5000, 5001);
    }

    public void init(int tcpPort, int udpPort) throws Exception {
        this.uciService = new UciService();
        this.tcpController = new TCPController(uciService, tcpPort);
        this.udpController = new UDPController(uciService, udpPort);
        this.tcpController.startService();
        this.udpController.startService();
        System.out.println("UCI Monitoring Server initialized on TCP: " + tcpPort + ", UDP: " + udpPort);
    }

    public void stop() {
        if (tcpController != null) tcpController.stopService();
        if (udpController != null) udpController.stopService();
    }

    public UciService getUciService() {
        return uciService;
    }

    public TCPController getTcpController() {
        return tcpController;
    }

    public UDPController getUdpController() {
        return udpController;
    }
}
