import java.util.ArrayList;
import java.util.List;
 

public class GameSession {
  
  private final String code;
  private final ClientHandler host;
  private ClientHandler guest;
  private boolean ended = false;
  private boolean hostReady = false;
  private boolean guestReady = false;
  private GamePhase phase;
  private Player hostPlayer;
  private Player guestPlayer;
  private GameState state;
  private final ServerLogic logic = new ServerLogic();
  private final Deck deck = new Deck();

  public GameSession (String code, ClientHandler host){
    this.host = host;
    this.code = code;
    this.phase = GamePhase.LOBBY;
    this.hostPlayer = new Player("host", true);
  }
  
  public boolean isEnded(){
    return ended;
  }

  public boolean isFull(){
    return guest != null;
  }

  public void addGuest(ClientHandler guest){
    this.guest = guest;
    this.guestPlayer = new Player("guest", false);
    this.state = new GameState(List.of(hostPlayer, guestPlayer));
    host.send(Message.of(Protocol.OPPONENT_JOINED));
    guest.send(Message.of(Protocol.OPPONENT_JOINED));
  }

  public String getCode(){
    return code;
  }

  public ClientHandler getHost(){
    return host;
  }

  public ClientHandler opponentOf(ClientHandler client){
    if (client == host){
      return guest;
    }
    return host;
  }

  public void handle(ClientHandler from, Message msg){
    if (phase == GamePhase.LOBBY && msg.is(Protocol.READY)){
      handleReady(from);
    } else if (phase == GamePhase.SHIP_PLACEMENT && msg.is(Protocol.PLACE_FLEET)){
      handleFleet(from, msg);
    } else if (phase == GamePhase.ATTACK && msg.is(Protocol.ATTACK)){
      handleAttack(from, msg);
    } else {
      from.send(Message.of(Protocol.ERROR));
    }
  }


  public void handleReady(ClientHandler from){
    if (from == host){
      if (guest == null){
        from.send(Message.of(Protocol.ERROR));
        return;
      }
      hostReady = true;

    } else {
      guestReady = true;
    }
  
    if (hostReady == true && guestReady == true) {
      state.setPhase(GamePhase.SHIP_PLACEMENT);
      phase = GamePhase.SHIP_PLACEMENT;
      guest.send(Message.of(Protocol.PHASE, GamePhase.SHIP_PLACEMENT.name()));
      host.send(Message.of(Protocol.PHASE, GamePhase.SHIP_PLACEMENT.name()));
    }
  }


  public void handleFleet(ClientHandler from, Message msg){
    if (state.getPhase() != GamePhase.SHIP_PLACEMENT) {
      from.send(Message.of(Protocol.ERROR, "Not placing ships right now"));
    return;
    }
    Player player = playerFor(from);
    if (player.getGrid().allShipsPlaced()) {
      from.send(Message.of(Protocol.ERROR, "Fleet already placed"));
     return;
    }

    List<Ship> fleet = new ArrayList<>();
    Grid trial = new Grid();
    for (int i = 0; i < msg.size(); i++) {
        List<int[]> cells = Protocol.decodeCells(msg.get(i));
        if (cells.isEmpty()) {
            from.send(Message.of(Protocol.ERROR, "Empty ship"));
            return;
        }
        boolean horizontal = cells.size() > 1 && cells.get(1)[0] == cells.get(0)[0];
        Ship ship = new Ship(cells.size(), horizontal ? Orientation.HORIZONTAL : Orientation.VERTICAL);
        if (!trial.canPlaceShip(ship, cells.get(0)[0], cells.get(0)[1])) {
            from.send(Message.of(Protocol.ERROR, "Invalid ship placement"));
            return;
        }
        trial.placeShip(ship, cells.get(0)[0], cells.get(0)[1]);
        fleet.add(ship);
    }
    if (!trial.allShipsPlaced()) {
        from.send(Message.of(Protocol.ERROR, "Fleet must be ships of length 5, 4, 3, 3 and 2"));
        return;
    }

    for (Ship ship : fleet) {
        int[] pos = ship.getPosition();
        if (logic.validateShipPlacement(player, ship, pos[0], pos[1])) {
            player.placeShip(ship, pos[0], pos[1]);
        }
    }
    from.send(Message.of(Protocol.FLEET_OK));

    if (hostPlayer.getGrid().allShipsPlaced() && guestPlayer.getGrid().allShipsPlaced()) {
        phase = GamePhase.ATTACK;
        state.setPhase(GamePhase.ATTACK);
        host.send(Message.of(Protocol.PHASE, GamePhase.ATTACK.name()));
        guest.send(Message.of(Protocol.PHASE, GamePhase.ATTACK.name()));
        state.selectStartingPlayer();
        startTurn();
    }
  }


  private void handleAttack(ClientHandler from, Message msg){
    Player attacker = playerFor(from);
    if (!(state.getCurrentPlayer() == attacker)){
      from.send(Message.of(Protocol.ERROR, "Not your turn"));
      return;
    }
    if (msg.size() != 3){
      from.send(Message.of(Protocol.ERROR, "Wrong number of arguments"));
      return;
    }
    int cardID, row, col;
    try {
      cardID = msg.getInt(0);
      row = msg.getInt(1);
      col = msg.getInt(2);
    } catch (NumberFormatException e){
      from.send(Message.of(Protocol.ERROR, "Invalid row or column"));
      return;
    }

    if (!inBounds(row, col)){
      from.send(Message.of(Protocol.ERROR, "Invalid row or column"));
      return;
    }

    Player opponent = playerFor(opponentOf(from));

    Grid targetGrid = opponent.getGrid();
    Card card = attacker.getHand().getCard(cardID);

    if (card == null){
      from.send(Message.of(Protocol.ERROR, "You don't have that card"));
      return;
    }

    if (!card.meetsUsageConditions(targetGrid)){
      from.send(Message.of(Protocol.ERROR, "Can't play that card right now"));
      return;
    }

    List<int[]> targets = new ArrayList<>();
    for (int[] cell : card.getTargetCells(row, col)){
      if (inBounds(cell[0], cell[1]) && !targetGrid.isAlreadyAttacked(cell[0], cell[1])){
        targets.add(cell);
      }
    }
    if (targets.isEmpty()){
      from.send(Message.of(Protocol.ERROR, "ALREADY ATTACKED"));
      return;
    }

    attacker.playCard(card, row, col);

    List<int[]> hits = new ArrayList<>();
    List<int[]> misses = new ArrayList<>();
    boolean sunkAny = false;
    for (int[] cell : targets){
      AttackResult result = targetGrid.receiveAttack(cell[0], cell[1]);
      hits.addAll(result.getHits());
      misses.addAll(result.getMisses());
      if (result.isShipDestroyed()){
        sunkAny = true;
      }
    }

    String hitText = Protocol.encodeCells(hits);
    String missText = Protocol.encodeCells(misses);
    String sunk = sunkAny ? "SUNK" : "NONE";

    from.send(Message.of(Protocol.RESULT, "YOU", hitText, missText, sunk));
    handlerFor(opponent).send(Message.of(Protocol.RESULT, "OPPONENT", hitText, missText, sunk));

    if (!opponent.hasShipsRemaining()) {
      endGame(playerFor(from));
      return;
    } 

    state.switchTurn();
    startTurn();
  }


  private Player playerFor(ClientHandler client) {
      if (client == host) {
          return hostPlayer;
      }
      if (client == guest) {
          return guestPlayer;
      }
      return null;
  }
  
  private ClientHandler handlerFor(Player player){
    if (player == hostPlayer){
      return host;
    }
    else if (player == guestPlayer){
      return guest;
    }
    return null;
  }


  private void startTurn() {
    Player currPlayer = state.getCurrentPlayer();
    currPlayer.setHand(deck.dealHand());

    ClientHandler current = handlerFor(currPlayer);
    ClientHandler opponent = opponentOf(current);

    current.send(Message.of(Protocol.TURN, "YOU"));
    current.send(Message.of(Protocol.CARDS, cardIds(currPlayer.getHand())));
    opponent.send(Message.of(Protocol.TURN, "OPPONENT"));
  }

  private void endGame(Player winner) {
    state.declareWinner(winner);
    phase = GamePhase.GAME_OVER;
    ended = true;

    ClientHandler winnerHandler = handlerFor(winner);
    winnerHandler.send(Message.of(Protocol.GAME_OVER, "WIN"));
    opponentOf(winnerHandler).send(Message.of(Protocol.GAME_OVER, "LOSE"));

    host.leaveSession();
    guest.leaveSession();
  }


  private boolean inBounds(int row, int col){
    return row >= 0 && row < 10 && col >= 0 && col < 10;
  }

  private String[] cardIds(PlayerHand hand){
    List<Card> cards = hand.getCards();
    String[] ids = new String[cards.size()];
    for (int i = 0; i < cards.size(); i++){
      ids[i] = String.valueOf(cards.get(i).getCardId());
    }
    return ids;
  }
}
