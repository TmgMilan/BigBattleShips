import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
  private final Socket socket;
  private final BufferedWriter writer;
  private final BufferedReader reader;
  private final SessionManager sessionManager;

  private volatile GameSession session;

  public ClientHandler(Socket socket, SessionManager sessions) throws IOException {
    this.socket = socket;
    this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    this.sessionManager = sessions;
  }

  @Override
  public void run() {
    try (Socket clientSocket = socket) {
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println("Client: " + line);
        try {
          Message msg = Message.parse(line);

          if (msg.is(Protocol.PING)) {
            send(Message.of(Protocol.PONG));
          } else if (session == null) {
            handleLobbyMessage(msg);
          } else {
            GameSession current = session;
            current.handle(this, msg);
            if (current.isEnded()) {
              sessionManager.removeSession(current.getCode());
            }
          }
        } catch (IllegalArgumentException e) {
          send(Message.of(Protocol.ERROR, "Bad message"));
        }
      }
    } catch (IOException e) {
      System.out.println("Client disconnected: " + e.getMessage());
    } finally {
      disconnect();
    }
  }

  private void handleLobbyMessage(Message msg) {
    if (msg.is(Protocol.CREATE_SESSION)) {
      session = sessionManager.createSession(this);
      send(Message.of(Protocol.SESSION_CREATED, session.getCode()));
    } else if (msg.is(Protocol.JOIN)) {
      if (msg.size() == 0) {
        send(Message.of(Protocol.ERROR, "Usage: JOIN|code"));
        return;
      }
      GameSession joined = sessionManager.joinSession(msg.get(0), this);
      if (joined == null) {
        send(Message.of(Protocol.ERROR, "No such game, or it is full"));
        return;
      }
      session = joined;
    } else {
      send(Message.of(Protocol.ERROR, "Create or join a game first"));
    }
  }

  public synchronized void send(Message message) {
    try {
      writer.write(message.toString());
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      System.out.println("Failed to send: " + e.getMessage());
    }
  }

  public void leaveSession() {
    session = null;
  }

  private void disconnect() {
    GameSession current = session;
    if (current == null) {
      return;
    }
    ClientHandler opponent = current.opponentOf(this);
    if (opponent != null) {
      opponent.send(Message.of(Protocol.OPPONENT_DISCONNECTED));
      opponent.leaveSession();
    }
    sessionManager.removeSession(current.getCode());
    session = null;
  }
}
