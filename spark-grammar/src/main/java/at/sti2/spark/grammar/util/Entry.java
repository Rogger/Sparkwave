package at.sti2.spark.grammar.util;


/**
 * Immutable Entry for arbitrary types of pairs
 * @author michaelrogger
 *
 * @param <K>
 * @param <V>
 */
public final class Entry <K,V>{

	private final K k;
	private final V v;
	
	public Entry(K k, V v) {
		this.k = k;
		this.v = v;
	}
	
	public K getKey() {
		return k;
	}

	public V getValue() {
		return v;
	}

}
