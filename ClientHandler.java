import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;

public class ClientHandler implements Runnable {

  private Socket socket;
  private BufferedWriter writer;
  private BufferedReader reader;
  private final SessionManager sessionManager;
  private GameSession session;

  public ClientHandler(Socket socket, SessionManager sessions) throws IOException {
    this.socket = socket;
    this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    this.sessionManager = sessions;
  }

  @Override
  public void run(){

    try (Socket clientSocket = socket){

      String message;
      while ((message = reader.readLine()) != null){

        try {

          Message msg = Message.parse(message);

          if (msg.is(Protocol.PING)){
            send(Message.of(Protocol.PONG));
          } else {
            send(Message.of(Protocol.ERROR));
          }
        } catch (IllegalArgumentException e){
          send(Message.of(Protocol.ERROR));
        }
        System.out.println("Client: " + message);


      }

    } catch (IOException e) {

      System.out.println("Client disconnected: " + e.getMessage());

    }
  }

  public synchronized void send(Message message){
    try {
      writer.write(message.toString());
      writer.newLine();
      writer.flush();
    } catch (IOException e){
      System.out.println("Failed to send: " + e.getMessage());
    }
  }
};

