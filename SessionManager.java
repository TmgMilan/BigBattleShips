import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SessionManager {

  private final Map<String, GameSession> sessions = new HashMap<>();
  private final Random random = new Random();

  public synchronized GameSession createSession(ClientHandler host){
    String code = generateCode();
    GameSession session = new GameSession(code, host);
    sessions.put(code, session);
    return session;
  }

  public synchronized GameSession joinSession(String code, ClientHandler guest){
  
    GameSession currSession = sessions.get(code);
    if (currSession != null){
      if (!currSession.isFull()){
        currSession.addGuest(guest);
        return currSession;
      }
      return null;
    }
    return null;

  }

  private String generateCode(){
    String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String code = "";

    for(int i = 0; i < 6; i++){
      int currIndex = random.nextInt(chars.length());
      code += chars.charAt(currIndex);
    }
    
    return code;
  }
} 
