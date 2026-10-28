

public class Protocol {
  //external to server or client
  public static final String SEPARATOR = "|"; 
  public static final String ERROR = "ERROR";

  // Client -> sending to server
  public static final String PING = "PING";
  public static final String CREATE_SESSION = "CREATE_SESSION";
  public static final String JOIN_SESSION = "JOIN_SESSION";

  // Server to client
  public static final String PONG = "PONG";
    
}
