import java.util.List;

public class HorizontalTripleCard extends Card {

    public HorizontalTripleCard() {
        super(7, "Horizontal Triple");
    }

    @Override
    public List<int[]> getTargetCells(int row, int col) {
        return List.of(
            new int[] {row, col - 1},
            new int[] {row, col},
            new int[] {row, col + 1}
        );
    }

    @Override
    public String getDescription() {
        return "Hits 3 cells in a row: the cell you pick plus the cells to its left and right.";
    }

    @Override
    public boolean meetsUsageConditions(Grid grid) {
        return true;
    }
}
