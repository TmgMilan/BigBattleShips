import java.util.ArrayList;
import java.util.List;

public class BlockStrikeCard extends Card {

    public BlockStrikeCard() {
        super(3, "Block Strike");
    }

    @Override
    public List<int[]> getTargetCells(int row, int col) {
        List<int[]> cells = new ArrayList<>();
        for (int r = row; r <= row + 1; r++) {
            for (int c = col; c <= col + 1; c++) {
                cells.add(new int[] {r, c});
            }
        }
        return cells;
    }

    @Override
    public boolean meetsUsageConditions(Grid grid) {
        return true;
    }
}
