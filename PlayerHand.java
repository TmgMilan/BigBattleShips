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

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public java.util.List<Card> getCards() {
        return cards;
    }
}
