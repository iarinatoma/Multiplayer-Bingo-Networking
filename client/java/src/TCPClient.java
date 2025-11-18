import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private UI ui;   // legăm UI-ul de client

    public TCPClient(UI ui) {
        this.ui = ui;
    }

    // apelata in ui
    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port); // se stabileste conexiune windows - wsl
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // primeste mesaje de la server
            out = new PrintWriter(socket.getOutputStream(), true); // trimite mesaje la server

            // thread pentru recepția mesajelor
            new Thread(this::listen).start(); // deschid un thread separat scare sta si asteapta mesaje de la server  ca sa nu blocam uiy

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listen() {
        try {
            String line;

            while ((line = in.readLine()) != null) {
                System.out.println("[TCP] Received: " + line);

                if (line.startsWith("CARD|")) {
                    String csv = line.substring(5);
                    ui.loadCard(csv);

                } else if (line.startsWith("NUMBER|")) {
                    int num = Integer.parseInt(line.substring(7));
                    ui.markNumber(num);

                    if (ui.getCard().hasBingo()) {
                        sendBingo();
                    }

                } else if (line.startsWith("WINNER|")) {
                    ui.showWinner(line.substring(7));

                } else if (line.startsWith("INVALID")) {
                    ui.showInvalid();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendBingo() {
        out.println("BINGO");
    }
}
