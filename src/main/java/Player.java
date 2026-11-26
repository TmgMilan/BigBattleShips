public class Player {
    private String playerId;
    private boolean isHost;
    private boolean ready;
    private Grid grid = new Grid();
    private PlayerHand hand = new PlayerHand();

    public Player(String playerId, boolean isHost) {
        this.playerId = playerId;
        this.isHost = isHost;
    }

    public boolean placeShip(Ship s, int row, int col) {
        if (!grid.canPlaceShip(s, row, col)) {
            return false;
        }
        grid.placeShip(s, row, col);
        return true;
    }

    public boolean playCard(Card c, int row, int col) {
        if (!hand.hasCard(c)) {
            return false;
        }
        c.markUsed();
        hand.removeCard(c);
        return true;
    }

    public void setReady(boolean r) {
        ready = r;
    }

    public boolean hasShipsRemaining() {
        return !grid.allShipsDestroyed();
    }

    public String getPlayerId() {
        return playerId;
    }

    public Grid getGrid() {
        return grid;
    }

    public PlayerHand getHand() {
        return hand;
    }

    public void setHand(PlayerHand hand) {
        this.hand = hand;
    }

    public boolean isReady() {
        return ready;
    }
}
