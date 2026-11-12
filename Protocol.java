

public class Protocol {
  //external to server or client
  public static final String SEPARATOR = "|"; 
  public static final String ERROR = "ERROR";

  // Client -> sending to server
  public static final String PING = "PING";
  public static final String CREATE_SESSION = "CREATE_SESSION";
  public static final String JOIN = "JOIN";
  public static final String READY = "READY";

  // Server to client
  public static final String PONG = "PONG";
  public static final String SESSION_CREATED = "SESSION_CREATED";
  public static final String OPPONENT_JOINED = "OPPONENT_JOINED";
  public static final String OPPONENT_DISCONNECTED = "OPPONENT_DISCONNECTED";
  public static final String PHASE = "PHASE";
    
}
