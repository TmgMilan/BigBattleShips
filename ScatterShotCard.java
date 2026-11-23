import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScatterShotCard extends Card {

    private static final int EXTRA_SHOTS = 2;
    private final Random random = new Random();

    public ScatterShotCard() {
        super(6, "Scatter Shot");
    }

    @Override
    public List<int[]> getTargetCells(int row, int col) {
        List<int[]> cells = new ArrayList<>();
        cells.add(new int[] {row, col});
        for (int i = 0; i < EXTRA_SHOTS; i++) {
            cells.add(new int[] {random.nextInt(10), random.nextInt(10)});
        }
        return cells;
    }

    @Override
    public boolean meetsUsageConditions(Grid grid) {
        return true;
    }
}
