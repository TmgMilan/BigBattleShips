


public class GameSession {
  
  private final String code;
  private final ClientHandler host;
  private ClientHandler guest;
  private boolean ended = false;

  public GameSession (String code, ClientHandler host){
    this.host = host;
    this.code = code;
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
}
