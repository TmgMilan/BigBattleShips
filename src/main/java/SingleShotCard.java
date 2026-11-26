import java.util.List;

public class SingleShotCard extends Card {

    public SingleShotCard() {
        super(0, "Single Shot");
    }

    @Override
    public List<int[]> getTargetCells(int row, int col) {
        return List.of(new int[] {row, col});
    }

    @Override
    public boolean meetsUsageConditions(Grid grid) {
        return true;
    }
}
