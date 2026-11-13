public class GameState {
    private GamePhase phase;
    private String currentTurn;
    private String winner;
    private java.util.List<Player> players;

    public GameState(java.util.List<Player> players) {
        this.players = players;
        this.phase = GamePhase.LOBBY;
    } 

    public Player getCurrentPlayer() {
        for (Player p : players) {
            if (p.getPlayerId().equals(currentTurn)) {
                return p;
            }
        }
        return null;
    }

    public void switchTurn() {
        for (Player p : players) {
            if (!p.getPlayerId().equals(currentTurn)) {
                currentTurn = p.getPlayerId();
                return;
            }
        }
    }

    public Player selectStartingPlayer() {
        Player first = players.get(new java.util.Random().nextInt(players.size()));
        currentTurn = first.getPlayerId();
        return first;
    }

    public void declareWinner(Player p) {
        winner = p.getPlayerId();
        phase = GamePhase.GAME_OVER;
    }

    public boolean isGameOver() {
        return phase == GamePhase.GAME_OVER;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public java.util.List<Player> getPlayers() {
        return players;
    }
}
