public class Main {

    public static void main(String[] args) {

        System.out.println("Searching for Bingo server...");
        // descoper serveru
        UDPDiscovery.ServerInfo info = UDPDiscovery.discoverServer();

        if (info == null) {
            System.out.println("No server found.");
            return;
        }

        System.out.println("Server discovered at IP " + info.ip +
                " on TCP port " + info.tcpPort);

        UI ui = new UI();
        ui.connectToServer(info.ip, info.tcpPort); // ma conectez  la server (conexiunea tcp)
    }
}
