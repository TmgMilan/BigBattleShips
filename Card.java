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

    // plain-English explanation of what the card hits, shown to the player
    public abstract String getDescription();

    public void markUsed() {
        used = true;
    }

    public int getCardId() {
        return cardId;
    }

    public String getName() {
        return name;
    }
}
