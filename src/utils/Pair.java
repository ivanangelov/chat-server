package bg.uni.sofia.fmi.mjt.finals.utils;

/**
 * Represents a Pair of two values.
 * @param <K> the type of the first value
 * @param <V> the type of the second value
 */
public class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }
}
