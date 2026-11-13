public class Ship {
    private int length;
    private Orientation orientation;
    private int[] position;
    private boolean[] health;

    public Ship(int length, Orientation orientation) {
        this.length = length;
        this.orientation = orientation;
        this.health = new boolean[length];
        java.util.Arrays.fill(health, true);
    }

    public void rotate() {
        if (orientation == Orientation.HORIZONTAL) {
            orientation = Orientation.VERTICAL;
        } else {
            orientation = Orientation.HORIZONTAL;
        }
    }

    public boolean applyHit(int row, int col) {
        if (position == null) {
            return false;
        }
        int segment;
        if (orientation == Orientation.HORIZONTAL) {
            segment = (row == position[0]) ? col - position[1] : -1;
        } else {
            segment = (col == position[1]) ? row - position[0] : -1;
        }
        if (segment < 0 || segment >= length) {
            return false;
        }
        health[segment] = false;
        return true;
    }

    public boolean isDestroyed() {
        for (boolean intact : health) {
            if (intact) {
                return false;
            }
        }
        return true;
    }

    public int getLength() {
        return length;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int row, int col) {
        position = new int[] {row, col};
    }
}
