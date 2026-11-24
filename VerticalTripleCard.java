import java.util.List;

public class VerticalTripleCard extends Card {

    public VerticalTripleCard() {
        super(8, "Vertical Triple");
    }

    @Override
    public List<int[]> getTargetCells(int row, int col) {
        return List.of(
            new int[] {row - 1, col},
            new int[] {row, col},
            new int[] {row + 1, col}
        );
    }

    @Override
    public String getDescription() {
        return "Hits 3 cells in a column: the cell you pick plus the cells above and below it.";
    }

    @Override
    public boolean meetsUsageConditions(Grid grid) {
        return true;
    }
}
