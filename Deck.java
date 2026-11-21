import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private static final int HAND_SIZE = 4;
    private static final int CARD_TYPES = 3;
    public PlayerHand dealHand() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < CARD_TYPES; i++) {
            ids.add(i);
        }
        Collections.shuffle(ids);

        PlayerHand hand = new PlayerHand();
        for (int i = 0; i < Math.min(HAND_SIZE, ids.size()); i++) {
            hand.addCard(createCard(ids.get(i)));
        }
        return hand;
    }

    private Card createCard(int id) {
        switch (id) {
            case 0: return new SingleShotCard();
            case 1: return new RowStrikeCard();
            case 2: return new ColumnStrikeCard();
            default: throw new IllegalArgumentException("Unknown card id " + id);
        }
    }
}
