import java.util.List;

public interface GameEventListener {
    void onSessionCreated(String code);
    void onOpponentJoined();
    void onPhaseChanged(GamePhase phase);
    void onFleetAccepted();
    void onTurn(boolean myTurn);
    void onCards(List<Integer> cardIds);
    void onAttackResult(boolean byMe, List<int[]> hits, List<int[]> misses, boolean sunk);
    void onError(String reason);
    void onGameOver(boolean won);
    void onOpponentDisconnected();
    void onDisconnected();
}
