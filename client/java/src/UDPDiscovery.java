import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPDiscovery {

    public static class ServerInfo {
        public String ip;
        public int tcpPort;

        public ServerInfo(String ip, int tcpPort) {
            this.ip = ip;
            this.tcpPort = tcpPort;
        }
    }


    private static String getWSLAddress() {
        try {
            Process p = new ProcessBuilder("wsl.exe", "hostname", "-I").start();
            p.waitFor();

            byte[] output = p.getInputStream().readAllBytes();
            String raw = new String(output).trim();

            // hostname -I poate returna mai multe IP-uri → luăm primul
            String ip = raw.split(" ")[0];

            System.out.println("[DEBUG] WSL IP detected: " + ip);
            return ip;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static ServerInfo discoverServer() {
        try {
            // =============================
            // 1. Luăm IP-ul real al WSL
            // =============================
            String serverIP = getWSLAddress();
            if (serverIP == null) {
                System.out.println("[ERROR] Could not detect WSL IP.");
                return null;
            }

            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            String message = "DISCOVER_BINGO_SERVER";
            byte[] sendData = message.getBytes();

            // trimitem broadcast
            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length,
                            InetAddress.getByName(serverIP), 5555);

            socket.send(sendPacket);

            // =============================
            // 2. Așteptăm răspunsul serverului din WSL
            // =============================
            byte[] recvBuf = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

            socket.receive(receivePacket);
            String response = new String(receivePacket.getData()).trim();

            if (response.startsWith("SERVER_HERE")) {
                String tcpPart = response.split("\\|")[1];
                int tcpPort = Integer.parseInt(tcpPart.split("=")[1]);

                socket.close();
                return new ServerInfo(serverIP, tcpPort);
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
