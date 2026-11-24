import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.io.IOException;
import java.util.Scanner;

public class Client {

  // myBoard = my fleet + where the opponent has shot me; enemyBoard = where I've shot them
  private static final BoardView myBoard = new BoardView();
  private static final BoardView enemyBoard = new BoardView();
  // the last PLACE_FLEET I sent, only drawn on my board once the server answers FLEET_OK
  private static volatile String pendingFleet = null;

  public static void main(String[] args) {
    
    try (
      Socket socket = new Socket("localhost", 1234);
      BufferedReader reader = new BufferedReader(
        new InputStreamReader(socket.getInputStream())
        );
      BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(socket.getOutputStream())
        );
      Scanner scanner = new Scanner(System.in)

      ){

      System.out.println("Connected to server");
      
      Thread listener = new Thread(() -> {
        try {
          String message;

          while((message = reader.readLine()) != null){
            System.out.println("Server " + message);
            if (message.startsWith(Protocol.CARDS + Protocol.SEPARATOR)){
              printHand(message);
            } else if (message.startsWith(Protocol.RESULT + Protocol.SEPARATOR)){
              handleResult(message);
            } else if (message.equals(Protocol.FLEET_OK)){
              handleFleetOk();
            }
          }
        } catch (IOException e) {
          System.out.println("Disconnected FRom server");
        }

        });
      listener.start();

      while(scanner.hasNextLine()){

        String input = scanner.nextLine();
        if (input.startsWith(Protocol.PLACE_FLEET + Protocol.SEPARATOR)){
          pendingFleet = input;
        }
        writer.write(input);
        writer.newLine();
        writer.flush();

        if (input.equalsIgnoreCase("Bye")){
          break;
        }
      }
  } catch (IOException e){
    System.out.println("Could not connect: " + e.getMessage());
  }
  }

  // RESULT|YOU|hits|misses|sunk  or  RESULT|OPPONENT|hits|misses|sunk
  private static void handleResult(String line){
    try {
      Message msg = Message.parse(line);
      if (msg.size() < 3){
        return;
      }
      boolean mine = msg.get(0).equals("YOU");
      BoardView board = mine ? enemyBoard : myBoard;
      board.markHits(Protocol.decodeCells(msg.get(1)));
      board.markMisses(Protocol.decodeCells(msg.get(2)));
      printBoards();
    } catch (IllegalArgumentException e){
      System.out.println("Couldn't read result: " + line);
    }
  }

  // the server accepted the fleet I sent, so draw my ships
  private static void handleFleetOk(){
    String fleet = pendingFleet;
    if (fleet == null){
      return;
    }
    try {
      Message msg = Message.parse(fleet);
      for (int i = 0; i < msg.size(); i++){
        myBoard.markShips(Protocol.decodeCells(msg.get(i)));
      }
      printBoards();
    } catch (IllegalArgumentException e){
      // shouldn't happen, the server already accepted it
    }
    pendingFleet = null;
  }

  private static synchronized void printBoards(){
    BoardView.printSideBySide("YOUR FLEET", myBoard, "ENEMY WATERS", enemyBoard);
  }

  // explains each card in a CARDS|id|id|... message so the player knows what the numbers mean
  private static void printHand(String line){
    Message msg = Message.parse(line);
    System.out.println("Your hand:");
    for (int i = 0; i < msg.size(); i++){
      try {
        Card card = Deck.createCard(msg.getInt(i));
        System.out.println("  [" + card.getCardId() + "] " + card.getName() + " - " + card.getDescription());
      } catch (IllegalArgumentException e){
        System.out.println("  [" + msg.get(i) + "] Unknown card");
      }
    }
    System.out.println("Play a card with: ATTACK|cardId|row|col  (rows and columns are 0-9)");
  }
}  
