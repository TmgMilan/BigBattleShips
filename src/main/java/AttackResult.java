public class AttackResult {
    private java.util.List<int[]> hits;
    private java.util.List<int[]> misses;
    private boolean shipDestroyed;

    public AttackResult(java.util.List<int[]> hits, java.util.List<int[]> misses, boolean shipDestroyed) {
        this.hits = hits;
        this.misses = misses;
        this.shipDestroyed = shipDestroyed;
    }

    public boolean isValid() {
        return !hits.isEmpty() || !misses.isEmpty();
    }

    public java.util.List<int[]> getHits() {
        return hits;
    }

    public java.util.List<int[]> getMisses() {
        return misses;
    }

    public boolean isShipDestroyed() {
        return shipDestroyed;
    }
}
