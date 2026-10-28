import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;


public class Server{
  
  private volatile boolean running;
  private ServerSocket serverSocket;
  private SessionManager sessionManager;

  public Server(){
    this.sessionManager = new SessionManager();
  }

  public static void main(String[] args) {
    
    Server server = new Server();
    server.start();

  }

  public void start(){

    running = true;
    try (ServerSocket ss = new ServerSocket(1234)) {

      serverSocket = ss;

      while (running){

        Socket socket = serverSocket.accept();

        Thread thread = new Thread(
            new ClientHandler(socket, sessionManager)
            );

        thread.start();

      }

    } catch (IOException e){

      if(running){ 
        e.printStackTrace();
      }
    }

  }
  public void stop(){

    running = false;

    try {
      if (serverSocket != null){
        serverSocket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

}
