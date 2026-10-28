import java.util.Arrays;


public class Message {

    private final String type;
    private final String[] fields;

    // Private: create messages with Message.of(...) or Message.parse(...)
    private Message(String type, String[] fields) {
        this.type = type;
        this.fields = fields;
    }

    // used To check if the incoming messages are valid and then turns them into a message object with the correct type and field
    public static Message of(String type, String... fields) {
        checkPart(type);
        for (String field : fields) {
            checkPart(field);
        }
        return new Message(type, fields);
    }

     // Parses a line received from the socket.

    public static Message parse(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Empty message");
        }
        // "\\|" because | is special in split's pattern syntax; -1 keeps empty trailing fields
        String[] parts = line.trim().split("\\|", -1);
        String type = parts[0];
        String[] fields = Arrays.copyOfRange(parts, 1, parts.length);
        return new Message(type, fields);
    }

    public String getType() {
        return type;
    }

    // returns the fields based on its index
    public String get(int i) {
        return fields[i];   
    }

    // returns a int of the field is an int
    public int getInt(int i) {
        return Integer.parseInt(fields[i]);
    }

    // How many fields this message has (not counting the type).
    public int size() {
        return fields.length;
    }

    // if the type matches
    public boolean is(String expectedType) {
        return type.equals(expectedType);
    }

    /** The exact text sent over the socket. */
    @Override
    public String toString() {
        if (fields.length == 0) {
            return type;
        }
        return type + Protocol.SEPARATOR + String.join(Protocol.SEPARATOR, fields);
    }

    // A field containing | or a line break would corrupt the message, so reject it early
    private static void checkPart(String part) {
        if (part == null || part.contains(Protocol.SEPARATOR)
                || part.contains("\n") || part.contains("\r")) {
            throw new IllegalArgumentException("Invalid message part: " + part);
        }
    }
}
