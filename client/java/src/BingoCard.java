public class BingoCard {

    public int[][] grid = new int[5][5];
    public boolean[][] marked = new boolean[5][5];

    // Reconstruiește tabela din string primit de la server
    public void loadFromString(String csv) {
        String[] parts = csv.split(",");
        int index = 0;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                grid[r][c] = Integer.parseInt(parts[index]);
                marked[r][c] = false;
                index++;
            }
        }
    }

    public void markNumber(int num) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (grid[r][c] == num) {
                    marked[r][c] = true;
                }
            }
        }
    }
    public boolean hasBingo() {
    // verifică liniile
        for (int r = 0; r < 5; r++) {
            boolean ok = true;
            for (int c = 0; c < 5; c++) {
                if (!marked[r][c]) {
                    ok = false;
                    break;
                }
            }
            if (ok) return true;
        }

        // verifică coloanele
        for (int c = 0; c < 5; c++) {
            boolean ok = true;
            for (int r = 0; r < 5; r++) {
                if (!marked[r][c]) {
                    ok = false;
                    break;
                }
            }
            if (ok) return true;
        }

        // diagonala principală
        boolean diag1 = true;
        for (int i = 0; i < 5; i++) {
            if (!marked[i][i]) diag1 = false;
        }
        if (diag1) return true;

        // diagonala secundară
        boolean diag2 = true;
        for (int i = 0; i < 5; i++) {
            if (!marked[i][4 - i]) diag2 = false;
        }
        if (diag2) return true;

        return false;
    }

}
