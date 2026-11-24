import java.util.List;

// A client-side 10x10 picture of one board, built up from the messages the server sends.
public class BoardView {
    public static final int SIZE = 10;

    public static final char UNKNOWN = '.';
    public static final char SHIP = 'S';
    public static final char HIT = 'X';
    public static final char MISS = 'o';

    private final char[][] cells = new char[SIZE][SIZE];

    public BoardView() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                cells[r][c] = UNKNOWN;
            }
        }
    }

    public void markShips(List<int[]> shipCells) {
        mark(shipCells, SHIP);
    }

    public void markHits(List<int[]> hitCells) {
        mark(hitCells, HIT);
    }

    public void markMisses(List<int[]> missCells) {
        mark(missCells, MISS);
    }

    private void mark(List<int[]> list, char symbol) {
        for (int[] cell : list) {
            int r = cell[0];
            int c = cell[1];
            if (r >= 0 && r < SIZE && c >= 0 && c < SIZE) {
                cells[r][c] = symbol;
            }
        }
    }

    // Prints two boards next to each other, e.g. your own fleet and your shots at the enemy.
    public static void printSideBySide(String leftTitle, BoardView left, String rightTitle, BoardView right) {
        String gap = "      ";
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-23s", leftTitle)).append(gap).append(rightTitle).append("\n");
        sb.append(header()).append(gap).append(header()).append("\n");
        for (int r = 0; r < SIZE; r++) {
            sb.append(left.row(r)).append(gap).append(right.row(r)).append("\n");
        }
        sb.append("Legend: S = ship   X = hit   o = miss   . = unknown/water");
        System.out.println(sb);
    }

    private static String header() {
        StringBuilder sb = new StringBuilder("   ");
        for (int c = 0; c < SIZE; c++) {
            sb.append(c).append(' ');
        }
        return sb.toString();
    }

    private String row(int r) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%2d ", r));
        for (int c = 0; c < SIZE; c++) {
            sb.append(cells[r][c]).append(' ');
        }
        return sb.toString();
    }
}
