# BigBattleShips

A two-player online battleship game with attack cards, written in Java with a JavaFX interface.

## Requirements

- **Java 21 or newer** (a JDK, not just a JRE). Check with `java -version`.

That's all. You don't need to install JavaFX or Gradle: the included Gradle wrapper downloads both the first time you run it.

## How to play

**1. Start the server** (one player, or a separate machine):

```
./gradlew runServer        # Linux / macOS
gradlew.bat runServer      # Windows
```

**2. Start the game window** (each player, in a new terminal):

```
./gradlew run              # Linux / macOS
gradlew.bat run            # Windows
```

**3. Connect:**
- One player clicks **Create game** and shares the 6-character code.
- The other player types the code and clicks **Join game**.
- Both click **Ready**, place ships, then take turns playing a card and choosing a cell in enemy waters.

### Server IP
- Same computer: leave it as `localhost`.
- Same Wi-Fi: use the local IP of the computer running the server (e.g. `192.168.1.20`).
- Over the internet: the host must forward port **1234** on their router, or both players can join a VPN like Tailscale and use that IP.

## Cards

Each turn you get 4 random cards and play one.

| Card | Hits |
|---|---|
| Single Shot | the chosen cell |
| Row Strike | the whole row (only once every row has been attacked) |
| Column Strike | the whole column (only once every column has been attacked) |
| Block Strike | a 2x2 square, chosen cell top-left |
| Cross Strike | the chosen cell and the 4 next to it |
| Diagonal Strike | the chosen cell and its 4 diagonals |
| Scatter Shot | the chosen cell plus 2 random cells |
| Horizontal Triple | the chosen cell and one either side |
| Vertical Triple | the chosen cell and one above and below |
| Nuke | a 3x3 square (only after 20+ enemy cells have been attacked) |

## Project layout

```
src/main/java/     all game source code
  Server, ClientHandler, SessionManager, GameSession   server side
  UIController, GameClient, GameEventListener          JavaFX client
  Grid, Ship, Player, Deck, *Card                      game rules
build.gradle       build settings (Java version, JavaFX, main classes)
gradlew, gradle/   Gradle wrapper (downloads Gradle automatically)
```

## Building

```
./gradlew build    # compile everything; output goes to build/
./gradlew clean    # delete build/
```
