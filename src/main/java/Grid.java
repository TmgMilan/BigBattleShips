import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Grid {
    public static final int[] FLEET_LENGTHS = {5, 4, 3, 3, 2};

    private int size = 10;
    private List<int[]> attackedCells = new ArrayList<>();
    private List<Ship> fleet = new ArrayList<>();

    public boolean canPlaceShip(Ship s, int row, int col) {
        int stillNeeded = 0;
        for (int length : FLEET_LENGTHS) {
            if (length == s.getLength()) {
                stillNeeded++;
            }
        }
        for (Ship placed : fleet) {
            if (placed.getLength() == s.getLength()) {
                stillNeeded--;
            }
        }
        if (stillNeeded <= 0) {
            return false;
        }

        for (int i = 0; i < s.getLength(); i++) {
            int r = (s.getOrientation() == Orientation.VERTICAL) ? row + i : row;
            int c = (s.getOrientation() == Orientation.HORIZONTAL) ? col + i : col;
            if (r < 0 || r >= size || c < 0 || c >= size) {
                return false;
            }
            for (Ship other : fleet) {
                int[] p = other.getPosition();
                for (int j = 0; j < other.getLength(); j++) {
                    int otherR = (other.getOrientation() == Orientation.VERTICAL) ? p[0] + j : p[0];
                    int otherC = (other.getOrientation() == Orientation.HORIZONTAL) ? p[1] + j : p[1];
                    if (otherR == r && otherC == c) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void placeShip(Ship s, int row, int col) {
        if (!canPlaceShip(s, row, col)) {
            throw new IllegalArgumentException("Ship can't be placed there");
        }
        s.setPosition(row, col);
        fleet.add(s);
    }

    public boolean isAlreadyAttacked(int row, int col) {
        for (int[] cell : attackedCells) {
            if (cell[0] == row && cell[1] == col) {
                return true;
            }
        }
        return false;
    }

    public AttackResult receiveAttack(int row, int col) {
        List<int[]> hits = new ArrayList<>();
        List<int[]> misses = new ArrayList<>();
        if (row < 0 || row >= size || col < 0 || col >= size || isAlreadyAttacked(row, col)) {
            return new AttackResult(hits, misses, false);
        }
        attackedCells.add(new int[] {row, col});

        for (Ship s : fleet) {
            if (s.applyHit(row, col)){
                hits.add(new int[] {row, col});
                return new AttackResult(hits, misses, s.isDestroyed());
            }
        }
        misses.add(new int[] {row, col});
        return new AttackResult(hits, misses, false);
    }
    public boolean allShipsPlaced() {
        int[] placed = new int[fleet.size()];
        for (int i = 0; i < fleet.size(); i++) {
            placed[i] = fleet.get(i).getLength();
        }
        int[] needed = FLEET_LENGTHS.clone();
        Arrays.sort(placed);
        Arrays.sort(needed);
        return Arrays.equals(placed, needed);
    }

    public boolean allShipsDestroyed() {
        if (fleet.isEmpty()) {
            return false;
        }
        for (Ship s : fleet) {
            if (!s.isDestroyed()) {
                return false;
            }
        }
        return true;
    }

    public List<Ship> getFleet() {
        return fleet;
    }
}
