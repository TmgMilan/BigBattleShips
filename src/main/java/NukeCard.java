import java.util.ArrayList;
import java.util.List;

public class NukeCard extends Card {

    private static final int CELLS_ATTACKED_NEEDED = 20;

    public NukeCard() {
        super(9, "Nuke");
    }

    @Override
    public List<int[]> getTargetCells(int row, int col) {
        List<int[]> cells = new ArrayList<>();
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = col - 1; c <= col + 1; c++) {
                cells.add(new int[] {r, c});
            }
        }
        return cells;
    }

    @Override
    public boolean meetsUsageConditions(Grid grid) {
        int attacked = 0;
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (grid.isAlreadyAttacked(r, c)) {
                    attacked++;
                }
            }
        }
        return attacked >= CELLS_ATTACKED_NEEDED;
    }
}
