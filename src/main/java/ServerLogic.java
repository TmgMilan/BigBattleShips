

public class ServerLogic {

  // TEMP: just asks the player's grid if the ship fits there
  public boolean validateShipPlacement(Player player, Ship ship, int row, int col) {
    return player.getGrid().canPlaceShip(ship, row, col);
  }
}
