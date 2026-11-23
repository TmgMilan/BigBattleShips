import java.util.List;

public class DiagonalStrikeCard extends Card {

    public DiagonalStrikeCard() {
        super(5, "Diagonal Strike");
    }

    @Override
    public List<int[]> getTargetCells(int row, int col) {
        return List.of(
            new int[] {row, col},
            new int[] {row - 1, col - 1},
            new int[] {row - 1, col + 1},
            new int[] {row + 1, col - 1},
            new int[] {row + 1, col + 1}
        );
    }

    @Override
    public boolean meetsUsageConditions(Grid grid) {
        return true;
    }
}
