import javax.swing.*; // importam clase pentru interfate grafice
import java.awt.*;

public class UI {

    private JFrame frame; // fereastra prinicpala a aplicatiei
    private JButton[][] buttons = new JButton[5][5]; // matrice de 5/5 de butoane
    private BingoCard card = new BingoCard(); // tine minte  logica interna a tabelei bingo pe client  numere efective si matriceile lor
    private TCPClient client;

    public UI() {
        client = new TCPClient(this);
        buildUI();
    }

    private void buildUI() {
        frame = new JFrame("Bingo Client"); // creeaza fereastra cu titlu bingo client
        frame.setSize(400, 500);
        frame.setLayout(new BorderLayout());

        JPanel grid = new JPanel();
        grid.setLayout(new GridLayout(5, 5)); // matrice 5/5

        // creeaza fiecare buton, il pune in grid si il initializeaza u -
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                buttons[r][c] = new JButton("-");
                grid.add(buttons[r][c]);
            }
        }

        JButton bingoBtn = new JButton("BINGO!");
        bingoBtn.addActionListener(e -> client.sendBingo());

        frame.add(grid, BorderLayout.CENTER);
        frame.add(bingoBtn, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void loadCard(String csv) {
        card.loadFromString(csv);

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                buttons[r][c].setText("" + card.grid[r][c]);
            }
        }
    }

    public void markNumber(int num) {
        card.markNumber(num); // din BingoCard devine ala true

        // colorează numărul
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (card.grid[r][c] == num) {
                    buttons[r][c].setBackground(Color.GREEN);
                }
            }
        }
    }

    public void showWinner(String who) {
        JOptionPane.showMessageDialog(frame,
                "WINNER: " + who,
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void showInvalid() {
        JOptionPane.showMessageDialog(frame,
                "No Bingo detected!",
                "Invalid",
                JOptionPane.WARNING_MESSAGE);
    }

    public void connectToServer(String ip, int port) {
        client.connect(ip, port);
    }

    public BingoCard getCard() {
        return card;
    }
}
