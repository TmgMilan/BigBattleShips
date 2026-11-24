import java.util.ArrayList;
import java.util.List;

public class ColumnStrikeCard extends Card {
    public ColumnStrikeCard() {
        super(2, "Column Strike");
    }

    @Override
    public List<int[]> getTargetCells(int row, int col) {
        List<int[]> cells = new ArrayList<>();
        for (int r = 0; r < 10; r++) {
            cells.add(new int[] {r, col});
        }
        return cells;
    }

    @Override
    public String getDescription() {
        return "Hits every cell in the column you pick (row is ignored). Only usable once every column on the enemy grid has been attacked at least once.";
    }

    @Override
    public boolean meetsUsageConditions(Grid grid) {
        for (int c = 0; c < 10; c++) {
            boolean columnAttacked = false;
            for (int r = 0; r < 10; r++) {
                if (grid.isAlreadyAttacked(r, c)) {
                    columnAttacked = true;
                    break;
                }
            }
            if (!columnAttacked) {
                return false;
            }
        }
        return true;
    }
}
