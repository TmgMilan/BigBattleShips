


public class GameSession {
  
  private final String code;
  private final ClientHandler host;
  private ClientHandler guest;
  private boolean ended = false;
  private boolean hostReady = false;
  private boolean guestReady = false;
  private GamePhase phase;

  public GameSession (String code, ClientHandler host){
    this.host = host;
    this.code = code;
    this.phase = GamePhase.LOBBY;
  }
  
  public boolean isEnded(){
    return ended;
  }

  public boolean isFull(){
    return guest != null;
  }

  public void addGuest(ClientHandler guest){
    this.guest = guest;
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
        phase = GamePhase.SHIP_PLACEMENT;
        guest.send(Message.of(Protocol.PHASE, phase.name()));
        host.send(Message.of(Protocol.PHASE, phase.name()));
      }
    } else {
      from.send(Message.of(Protocol.ERROR));
    }
  }
}
