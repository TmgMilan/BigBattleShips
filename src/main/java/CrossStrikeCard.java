import java.util.List;

public class CrossStrikeCard extends Card {

    public CrossStrikeCard() {
        super(4, "Cross Strike");
    }

    @Override
    public List<int[]> getTargetCells(int row, int col) {
        return List.of(
            new int[] {row, col},
            new int[] {row - 1, col},
            new int[] {row + 1, col},
            new int[] {row, col - 1},
            new int[] {row, col + 1}
        );
    }

    @Override
    public boolean meetsUsageConditions(Grid grid) {
        return true;
    }
}
