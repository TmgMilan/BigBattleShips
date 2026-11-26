import java.util.List;
import java.util.ArrayList;

public class RowStrikeCard extends Card {

    public RowStrikeCard() {
        super(1, "Row Strike");
    }

    @Override
    public List<int[]> getTargetCells(int row, int col) {
        List<int[]> cells = new ArrayList<>();
        for (int c = 0; c < 10; c++) {
            cells.add(new int[] {row, c});
        }
        return cells;
    }

    @Override
    public boolean meetsUsageConditions(Grid grid) {
        for (int r = 0; r < 10; r++) {
            boolean rowAttacked = false;
            for (int c = 0; c < 10; c++) {
                if (grid.isAlreadyAttacked(r, c)) {
                    rowAttacked = true;
                    break;
                }
            }
            if (!rowAttacked) {
                return false;
            }
        }
        return true;
    }
}
