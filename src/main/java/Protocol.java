

public class Protocol {
  //external to server or client
  public static final int PORT = 1234;
  public static final String SEPARATOR = "|";
  public static final String ERROR = "ERROR";

  // Client -> sending to server
  public static final String PING = "PING";
  public static final String CREATE_SESSION = "CREATE_SESSION";
  public static final String JOIN = "JOIN";
  public static final String READY = "READY";
  public static final String PLACE_FLEET = "PLACE_FLEET";
  public static final String ATTACK = "ATTACK";

  // Server to client
  public static final String PONG = "PONG";
  public static final String SESSION_CREATED = "SESSION_CREATED";
  public static final String OPPONENT_JOINED = "OPPONENT_JOINED";
  public static final String OPPONENT_DISCONNECTED = "OPPONENT_DISCONNECTED";
  public static final String PHASE = "PHASE";
  public static final String FLEET_OK = "FLEET_OK";
  public static final String TURN = "TURN";
  public static final String CARDS = "CARDS";
  public static final String RESULT = "RESULT";
  public static final String GAME_OVER = "GAME_OVER";

  // turns a list of cells into "row,col;row,col;..."
  public static String encodeCells(java.util.List<int[]> cells) {
    StringBuilder sb = new StringBuilder();
    for (int[] cell : cells) {
      if (sb.length() > 0) {
        sb.append(";");
      }
      sb.append(cell[0]).append(",").append(cell[1]);
    }
    return sb.toString();
  }

  // turns "row,col;row,col;..." back into a list of cells
  public static java.util.List<int[]> decodeCells(String text) {
    java.util.List<int[]> cells = new java.util.ArrayList<>();
    if (text == null || text.isEmpty()) {
      return cells;
    }
    for (String item : text.split(";")) {
      String[] rowCol = item.split(",");
      if (rowCol.length != 2) {
        throw new IllegalArgumentException("Bad cell: " + item);
      }
      cells.add(new int[] {Integer.parseInt(rowCol[0].trim()), Integer.parseInt(rowCol[1].trim())});
    }
    return cells;
  }
}
