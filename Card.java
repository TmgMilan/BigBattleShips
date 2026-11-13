public abstract class Card {
    private int cardId;
    private String name;
    private boolean used;

    protected Card(int cardId, String name) {
        this.cardId = cardId;
        this.name = name;
    }

    public abstract java.util.List<int[]> getTargetCells(int row, int col);

    public abstract boolean meetsUsageConditions(Grid grid);

    public void markUsed() {
        used = true;
    }

    public int getCardId() {
        return cardId;
    }
}
