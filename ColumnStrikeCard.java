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
    public boolean meetsUsageConditions(Grid grid) {
        return true;
    }
}
