import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class UIController extends Application implements GameEventListener {
    private static final int SIZE = 10, CELL = 32;
    private static final int[] FLEET = {5, 4, 3, 3, 2};
    private static final int WATER = 0, SHIP = 1, HIT = 2, MISS = 3;
    private static final String[] CELL_COLOURS = {"#1b3a5c", "#7a8a99", "#d9534f", "#2b5278"};
    private static final String[] CELL_TEXT = {"", "", "X", "•"};
    private static final String BG = "#0f2236", PANEL = "#16314d", TEXT = "#e8eef3", ACCENT = "#f0b429";

    private static final String[][] CARDS = {
        {"Single Shot", "the chosen cell"},
        {"Row Strike", "the whole row"},
        {"Column Strike", "the whole column"},
        {"Block Strike", "a 2x2 square (chosen cell is top-left)"},
        {"Cross Strike", "the chosen cell and the 4 around it"},
        {"Diagonal Strike", "the chosen cell and its 4 diagonals"},
        {"Scatter Shot", "the chosen cell plus 2 random cells"},
        {"Horizontal Triple", "the chosen cell and one either side"},
        {"Vertical Triple", "the chosen cell and one above and below"},
        {"Nuke", "a 3x3 square (needs 20+ enemy cells attacked)"}
    };

    private GameClient client;
    private BorderPane root;
    private final Label status = label("");
    private GamePhase phase = GamePhase.LOBBY;
    private String lobbyCode;

    private int[][] myBoard = new int[SIZE][SIZE], enemyBoard = new int[SIZE][SIZE];
    private Button[][] myCells, enemyCells;

    private final List<List<int[]>> placedShips = new ArrayList<>();
    private boolean horizontal = true, fleetSubmitted;
    private Label nextShipLabel;
    private Button rotateButton, resetButton, submitButton;

    private boolean myTurn, waitingForResult;
    private Integer selectedCard;
    private List<Integer> hand = new ArrayList<>();
    private Label turnLabel;
    private FlowPane handBox;
    private TextArea log;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        client = new GameClient(this, Platform::runLater);
        root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + ";");

        Label title = text("My BattleShip", 22, ACCENT);
        root.setTop(padded(new HBox(title), BG));
        root.setBottom(padded(new HBox(status), PANEL));
        showMenu("");

        stage.setTitle("My BattleShip");
        stage.setScene(new Scene(root, 1000, 780));
        stage.setOnCloseRequest(e -> client.disconnect());
        stage.show();
    }

    private void showMenu(String message) {
        TextField ip = new TextField("localhost"), port = new TextField("1234"), code = new TextField();
        code.setPromptText("Game code");

        Button create = button("Create game", () -> {
            if (connect(ip.getText().trim(), port.getText().trim())) client.createGame();
        });
        Button join = button("Join game", () -> {
            String c = code.getText().trim().toUpperCase();
            if (c.isEmpty()) setStatus("Enter a game code to join");
            else if (connect(ip.getText().trim(), port.getText().trim())) client.joinGame(c);
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, label("Server IP"), ip);
        form.addRow(1, label("Port"), port);

        show(panel(heading("Play"), form, create, label("or join a friend's game:"), new HBox(10, code, join)));
        setStatus(message);
    }

    private void showLobby(boolean opponentPresent) {
        VBox box = panel(heading("Lobby"));
        if (lobbyCode != null) {
            box.getChildren().addAll(label("Share this code with your opponent:"), text(lobbyCode, 34, ACCENT));
        }
        if (opponentPresent) {
            Button ready = button("Ready", null);
            ready.setOnAction(e -> {
                client.sendReady();
                ready.setDisable(true);
                ready.setText("Waiting for opponent...");
            });
            box.getChildren().addAll(label("Opponent joined!"), ready);
        } else {
            box.getChildren().add(label("Waiting for an opponent to join..."));
        }
        show(box);
    }

    private void showPlacement() {
        myBoard = new int[SIZE][SIZE];
        placedShips.clear();
        horizontal = true;
        fleetSubmitted = false;

        myCells = new Button[SIZE][SIZE];
        GridPane grid = buildGrid(myCells, this::placeShip);
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int row = r, col = c;
                myCells[r][c].setOnMouseEntered(e -> previewShip(row, col));
                myCells[r][c].setOnMouseExited(e -> refreshPlacement());
                myCells[r][c].setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.SECONDARY) {
                        rotate();
                        previewShip(row, col);
                    }
                });
            }
        }

        nextShipLabel = label("");
        rotateButton = button("", this::rotate);
        resetButton = button("Reset", () -> {
            myBoard = new int[SIZE][SIZE];
            placedShips.clear();
            refreshPlacement();
        });
        submitButton = button("Confirm fleet", this::submitFleet);

        VBox side = panel(heading("Place your ships"),
            label("Click a cell to place the next ship.\nRight-click or use the button to rotate."),
            nextShipLabel, rotateButton, resetButton, submitButton);
        side.setMaxWidth(300);

        HBox layout = new HBox(24, titled("Your fleet", grid), side);
        layout.setAlignment(Pos.CENTER);
        show(layout);
        refreshPlacement();
        setStatus("Place your 5 ships.");
    }

    private void showBattle() {
        enemyBoard = new int[SIZE][SIZE];
        myTurn = waitingForResult = false;
        selectedCard = null;
        hand = new ArrayList<>();

        myCells = new Button[SIZE][SIZE];
        enemyCells = new Button[SIZE][SIZE];
        HBox boards = new HBox(32,
            titled("Your fleet", buildGrid(myCells, null)),
            titled("Enemy waters", buildGrid(enemyCells, this::fireAt)));
        boards.setAlignment(Pos.CENTER);

        turnLabel = text("", 18, ACCENT);
        handBox = new FlowPane(8, 8);
        log = new TextArea();
        log.setEditable(false);
        log.setPrefRowCount(4);
        log.setStyle("-fx-control-inner-background: " + PANEL + "; -fx-text-fill: " + TEXT + ";");

        VBox layout = new VBox(14, boards, turnLabel, handBox, log);
        layout.setAlignment(Pos.TOP_CENTER);
        show(layout);
        refreshBattle();
        renderHand();
        setStatus("Battle started!");
    }

    private void showGameOver(boolean won) {
        show(panel(text(won ? "You won!" : "You lost", 40, won ? ACCENT : "#d9534f"),
            label(won ? "Every enemy ship is sunk." : "Your whole fleet was sunk."),
            button("Back to menu", () -> backToMenu(""))));
        setStatus("");
    }

    private void backToMenu(String message) {
        phase = GamePhase.LOBBY;
        lobbyCode = null;
        showMenu(message);
    }

    private List<int[]> shipCells(int row, int col, int length) {
        List<int[]> cells = new ArrayList<>();
        for (int k = 0; k < length; k++) {
            cells.add(horizontal ? new int[] {row, col + k} : new int[] {row + k, col});
        }
        return cells;
    }

    private boolean fits(List<int[]> cells) {
        for (int[] c : cells) {
            if (!inBounds(c[0], c[1]) || myBoard[c[0]][c[1]] != WATER) return false;
        }
        return true;
    }

    private boolean placing() {
        return !fleetSubmitted && placedShips.size() < FLEET.length;
    }

    private void placeShip(int row, int col) {
        if (!placing()) return;
        List<int[]> cells = shipCells(row, col, FLEET[placedShips.size()]);
        if (!fits(cells)) {
            setStatus("That ship doesn't fit there.");
            return;
        }
        for (int[] c : cells) myBoard[c[0]][c[1]] = SHIP;
        placedShips.add(cells);
        setStatus("");
        refreshPlacement();
    }

    private void previewShip(int row, int col) {
        refreshPlacement();
        if (!placing()) return;
        List<int[]> cells = shipCells(row, col, FLEET[placedShips.size()]);
        String colour = fits(cells) ? "#3fa66b" : "#a83f3f";
        for (int[] c : cells) {
            if (inBounds(c[0], c[1])) myCells[c[0]][c[1]].setStyle(cellStyle(colour));
        }
    }

    private void rotate() {
        horizontal = !horizontal;
        refreshPlacement();
    }

    private void submitFleet() {
        fleetSubmitted = true;
        client.placeFleet(placedShips);
        refreshPlacement();
        setStatus("Fleet sent...");
    }

    private void refreshPlacement() {
        paint(myCells, myBoard);
        boolean done = placedShips.size() == FLEET.length;
        nextShipLabel.setText(done ? "All ships placed." : "Next ship: length " + FLEET[placedShips.size()]);
        rotateButton.setText("Direction: " + (horizontal ? "Horizontal" : "Vertical"));
        rotateButton.setDisable(fleetSubmitted);
        resetButton.setDisable(fleetSubmitted);
        submitButton.setDisable(!done || fleetSubmitted);
    }

    private void fireAt(int row, int col) {
        if (!myTurn) setStatus("It's not your turn.");
        else if (selectedCard == null) setStatus("Pick a card first.");
        else if (!waitingForResult) {
            waitingForResult = true;
            client.attack(selectedCard, row, col);
        }
    }

    private void renderHand() {
        handBox.getChildren().clear();
        if (!myTurn || hand.isEmpty()) {
            handBox.getChildren().add(label(myTurn ? "Dealing cards..." : "Waiting for your opponent..."));
            return;
        }
        ToggleGroup group = new ToggleGroup();
        for (int id : hand) {
            String name = id < CARDS.length ? CARDS[id][0] : "Card " + id;
            String info = id < CARDS.length ? "Hits " + CARDS[id][1] : "";
            // Show the card's name and what it hits on the card itself
            Label nameLabel = new Label(name);
            Label infoLabel = new Label(info);
            infoLabel.setWrapText(true);
            VBox face = new VBox(4, nameLabel, infoLabel);
            face.setAlignment(Pos.TOP_LEFT);
            styleCardText(nameLabel, infoLabel, false);

            ToggleButton card = new ToggleButton();
            card.setGraphic(face);
            card.setToggleGroup(group);
            card.setTooltip(new Tooltip(info));
            card.setPrefSize(190, 90);
            card.setAlignment(Pos.TOP_LEFT);
            card.setStyle(cardStyle(false));
            card.selectedProperty().addListener((obs, was, selected) -> {
                card.setStyle(cardStyle(selected));
                styleCardText(nameLabel, infoLabel, selected);
                if (selected) {
                    selectedCard = id;
                    setStatus(name + ": " + info + ". Now click a cell in enemy waters.");
                } else if (selectedCard != null && selectedCard == id) {
                    selectedCard = null;
                }
            });
            handBox.getChildren().add(card);
        }
    }

    private void refreshBattle() {
        paint(myCells, myBoard);
        paint(enemyCells, enemyBoard);
    }

    @Override
    public void onSessionCreated(String code) {
        lobbyCode = code;
        showLobby(false);
    }

    @Override
    public void onOpponentJoined() {
        showLobby(true);
        setStatus("Press Ready when you're ready to play.");
    }

    @Override
    public void onPhaseChanged(GamePhase newPhase) {
        phase = newPhase;
        if (newPhase == GamePhase.SHIP_PLACEMENT) showPlacement();
        else if (newPhase == GamePhase.ATTACK) showBattle();
    }

    @Override
    public void onFleetAccepted() {
        setStatus("Fleet accepted. Waiting for your opponent to place theirs...");
    }

    @Override
    public void onTurn(boolean mine) {
        myTurn = mine;
        waitingForResult = false;
        selectedCard = null;
        hand = new ArrayList<>();
        turnLabel.setText(mine ? "Your turn" : "Opponent's turn");
        renderHand();
        setStatus(mine ? "Pick a card, then click a cell in enemy waters." : "");
    }

    @Override
    public void onCards(List<Integer> cardIds) {
        hand = new ArrayList<>(cardIds);
        renderHand();
    }

    @Override
    public void onAttackResult(boolean byMe, List<int[]> hits, List<int[]> misses, boolean sunk) {
        int[][] board = byMe ? enemyBoard : myBoard;
        for (int[] c : hits) board[c[0]][c[1]] = HIT;
        for (int[] c : misses) board[c[0]][c[1]] = MISS;
        refreshBattle();
        log.appendText((byMe ? "You" : "Opponent") + ": " + hits.size() + " hit(s), " + misses.size() + " miss(es)"
            + (sunk ? (byMe ? " - you sank a ship!" : " - they sank one of your ships!") : "") + "\n");
    }

    @Override
    public void onError(String reason) {
        setStatus("Error: " + reason);
        waitingForResult = false;
        if (phase == GamePhase.SHIP_PLACEMENT && fleetSubmitted) {
            fleetSubmitted = false;
            refreshPlacement();
        }
    }

    @Override
    public void onGameOver(boolean won) {
        phase = GamePhase.GAME_OVER;
        showGameOver(won);
    }

    @Override
    public void onOpponentDisconnected() {
        backToMenu("Your opponent left the game.");
    }

    @Override
    public void onDisconnected() {
        backToMenu("Lost connection to the server.");
    }

    private interface CellAction {
        void run(int row, int col);
    }

    private GridPane buildGrid(Button[][] cells, CellAction onClick) {
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        for (int i = 0; i < SIZE; i++) {
            grid.add(axis(i), i + 1, 0);
            grid.add(axis(i), 0, i + 1);
        }
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                Button cell = new Button();
                cell.setMinSize(CELL, CELL);
                cell.setMaxSize(CELL, CELL);
                cell.setFocusTraversable(false);
                int row = r, col = c;
                if (onClick != null) cell.setOnAction(e -> onClick.run(row, col));
                cells[r][c] = cell;
                grid.add(cell, c + 1, r + 1);
            }
        }
        return grid;
    }

    private void paint(Button[][] cells, int[][] board) {
        if (cells == null) return;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                cells[r][c].setText(CELL_TEXT[board[r][c]]);
                cells[r][c].setStyle(cellStyle(CELL_COLOURS[board[r][c]]));
            }
        }
    }

    private boolean connect(String ip, String portText) {
        if (client.isConnected()) return true;
        try {
            int port = Integer.parseInt(portText);
            if (client.connect(ip, port)) return true;
            setStatus("Couldn't connect to " + ip + ":" + port + ". Is the server running?");
        } catch (NumberFormatException e) {
            setStatus("Port must be a number.");
        }
        return false;
    }

    private boolean inBounds(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    private void show(Node node) {
        BorderPane.setMargin(node, new Insets(16));
        BorderPane.setAlignment(node, Pos.TOP_CENTER);
        root.setCenter(node);
    }

    private void setStatus(String message) {
        status.setText(message);
    }

    private static String cellStyle(String colour) {
        return "-fx-background-color: " + colour + "; -fx-background-radius: 3; -fx-padding: 0;"
            + "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;";
    }

    private static String cardStyle(boolean selected) {
        return "-fx-background-color: " + (selected ? ACCENT : PANEL) + "; -fx-text-fill: " + (selected ? BG : TEXT)
            + "; -fx-border-color: " + ACCENT + "; -fx-border-radius: 6; -fx-background-radius: 6;"
            + "-fx-font-size: 13px; -fx-font-weight: bold;";
    }

    // Card text colours flip when the card is selected (dark text on the yellow background)
    private static void styleCardText(Label name, Label info, boolean selected) {
        String colour = selected ? BG : TEXT;
        name.setStyle("-fx-text-fill: " + colour + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        info.setStyle("-fx-text-fill: " + colour + "; -fx-font-size: 12px;");
    }

    private static Button button(String text, Runnable action) {
        Button b = new Button(text);
        if (action != null) b.setOnAction(e -> action.run());
        b.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: " + BG + "; -fx-font-weight: bold;"
            + "-fx-font-size: 14px; -fx-background-radius: 6; -fx-padding: 8 16;");
        return b;
    }

    private static Label text(String value, int size, String colour) {
        Label l = new Label(value);
        l.setWrapText(true);
        l.setStyle("-fx-text-fill: " + colour + "; -fx-font-size: " + size + "px; -fx-font-weight: bold;");
        return l;
    }

    private static Label label(String value) {
        Label l = new Label(value);
        l.setWrapText(true);
        l.setStyle("-fx-text-fill: " + TEXT + "; -fx-font-size: 14px;");
        return l;
    }

    private static Label heading(String value) {
        return text(value, 20, TEXT);
    }

    private static Label axis(int i) {
        Label l = new Label(String.valueOf(i));
        l.setMinSize(CELL, CELL);
        l.setAlignment(Pos.CENTER);
        l.setStyle("-fx-text-fill: #8fa6bd; -fx-font-size: 12px;");
        return l;
    }

    private static HBox padded(HBox box, String background) {
        box.setPadding(new Insets(10, 16, 10, 16));
        box.setStyle("-fx-background-color: " + background + ";");
        return box;
    }

    private static VBox panel(Node... children) {
        VBox box = new VBox(14, children);
        box.setPadding(new Insets(20));
        box.setMaxWidth(380);
        box.setMaxHeight(Region.USE_PREF_SIZE);
        box.setStyle("-fx-background-color: " + PANEL + "; -fx-background-radius: 10;");
        return box;
    }

    private static VBox titled(String title, Node content) {
        VBox box = new VBox(8, text(title, 16, TEXT), content);
        box.setAlignment(Pos.TOP_CENTER);
        return box;
    }
}
