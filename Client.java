import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.io.IOException;
import java.util.Scanner;

public class Client {

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
            System.out.println("Server" + message);
          }
        } catch (IOException e) {
          System.out.println("Disconnected FRom server");
        }

        });
      listener.start();

      while(scanner.hasNextLine()){

        String input = scanner.nextLine();
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
}  
