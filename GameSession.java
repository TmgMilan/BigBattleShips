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
        state.setPhase(GamePhase.ATTACK);
        host.send(Message.of(Protocol.PHASE, GamePhase.ATTACK.name()));
        guest.send(Message.of(Protocol.PHASE, GamePhase.ATTACK.name()));
        state.selectStartingPlayer();
        // TODO: startTurn() not implemented yet
        // startTurn();
    }
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
}
