public class PlayerHand {
    private java.util.List<Card> cards = new java.util.ArrayList<>();

    public void addCard(Card c) {
        cards.add(c);
    }

    public void removeCard(Card c) {
        cards.remove(c);
    }

    public boolean hasCard(Card c) {
        return cards.contains(c);
    }

    public Card getCard(int cardId) {
        for (Card c : cards) {
            if (c.getCardId() == cardId) {
                return c;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public java.util.List<Card> getCards() {
        return cards;
    }
}
