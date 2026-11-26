import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class GameClient {
    private final GameEventListener listener;
    private final Executor callbacks;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private volatile boolean connected;

    public GameClient(GameEventListener listener, Executor callbacks) {
        this.listener = listener;
        this.callbacks = callbacks;
    }

    public boolean connect(String serverIp, int port) {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverIp, port), 3000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connected = true;

            Thread thread = new Thread(this::listen, "server-listener");
            thread.setDaemon(true);
            thread.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    public void createGame() {
        send(Protocol.CREATE_SESSION);
    }

    public void joinGame(String code) {
        send(Protocol.JOIN, code);
    }

    public void sendReady() {
        send(Protocol.READY);
    }

    public void placeFleet(List<List<int[]>> fleet) {
        String[] ships = new String[fleet.size()];
        for (int i = 0; i < fleet.size(); i++) {
            ships[i] = Protocol.encodeCells(fleet.get(i));
        }
        send(Protocol.PLACE_FLEET, ships);
    }

    public void attack(int cardId, int row, int col) {
        send(Protocol.ATTACK, String.valueOf(cardId), String.valueOf(row), String.valueOf(col));
    }

    private synchronized void send(String type, String... fields) {
        if (!connected) {
            callbacks.execute(() -> listener.onError("Not connected to a server"));
            return;
        }
        Message message;
        try {
            message = Message.of(type, fields);
        } catch (IllegalArgumentException e) {
            callbacks.execute(() -> listener.onError("Invalid input"));
            return;
        }
        try {
            writer.write(message.toString());
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            callbacks.execute(() -> listener.onError("Failed to send: " + e.getMessage()));
        }
    }

    private void listen() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    dispatch(Message.parse(line));
                } catch (RuntimeException e) {
                    System.err.println("Ignored bad message from server: " + line);
                }
            }
        } catch (IOException ignored) {
        } finally {
            boolean wasConnected = connected;
            connected = false;
            if (wasConnected) {
                callbacks.execute(listener::onDisconnected);
            }
        }
    }

    private void dispatch(Message m) {
        if (m.is(Protocol.SESSION_CREATED)) {
            String code = m.get(0);
            callbacks.execute(() -> listener.onSessionCreated(code));

        } else if (m.is(Protocol.OPPONENT_JOINED)) {
            callbacks.execute(listener::onOpponentJoined);

        } else if (m.is(Protocol.PHASE)) {
            GamePhase phase = GamePhase.valueOf(m.get(0));
            callbacks.execute(() -> listener.onPhaseChanged(phase));

        } else if (m.is(Protocol.FLEET_OK)) {
            callbacks.execute(listener::onFleetAccepted);

        } else if (m.is(Protocol.TURN)) {
            boolean mine = "YOU".equals(m.get(0));
            callbacks.execute(() -> listener.onTurn(mine));

        } else if (m.is(Protocol.CARDS)) {
            List<Integer> ids = new ArrayList<>();
            for (int i = 0; i < m.size(); i++) {
                ids.add(m.getInt(i));
            }
            callbacks.execute(() -> listener.onCards(ids));

        } else if (m.is(Protocol.RESULT)) {
            boolean byMe = "YOU".equals(m.get(0));
            List<int[]> hits = cells(m.get(1));
            List<int[]> misses = cells(m.get(2));
            boolean sunk = "SUNK".equals(m.get(3));
            callbacks.execute(() -> listener.onAttackResult(byMe, hits, misses, sunk));

        } else if (m.is(Protocol.GAME_OVER)) {
            boolean won = "WIN".equals(m.get(0));
            callbacks.execute(() -> listener.onGameOver(won));

        } else if (m.is(Protocol.ERROR)) {
            String reason = m.size() > 0 ? m.get(0) : "Something went wrong";
            callbacks.execute(() -> listener.onError(reason));

        } else if (m.is(Protocol.OPPONENT_DISCONNECTED)) {
            callbacks.execute(listener::onOpponentDisconnected);
        }
    }

    private List<int[]> cells(String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return Protocol.decodeCells(text);
    }
}
