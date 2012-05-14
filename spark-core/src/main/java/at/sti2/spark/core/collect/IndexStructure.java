package at.sti2.spark.core.collect;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFValue;

import com.google.common.collect.HashMultimap;

/**
 * Index Structure for indexing RDF triple, can be dynamically adjusted to index
 * subject,predicate,object. Each indexed object can have a time to live value,
 * if object is older than specified window for index structure the element is
 * automatically removed from index.
 * 
 * @author michaelrogger
 * 
 * @param <Value>
 */
public class IndexStructure<Value extends Removable> {

	long windowInMillis;

	boolean subjectIndexing;
	boolean predicateIndexing;
	boolean objectIndexing;

	final ConcurrentLinkedQueue<TTLEntry<RDFValue, Value>> expireSubjectQueue;
	final ConcurrentLinkedQueue<TTLEntry<RDFValue, Value>> expirePredicateQueue;
	final ConcurrentLinkedQueue<TTLEntry<RDFValue, Value>> expireObjectQueue;

	private final HashMultimap<RDFValue, Value> subjectMap;
	private final HashMultimap<RDFValue, Value> predicateMap;
	private final HashMultimap<RDFValue, Value> objectMap;

	public IndexStructure() {
		this.subjectIndexing = false;
		this.predicateIndexing = false;
		this.objectIndexing = false;
		this.windowInMillis = 0;

		subjectMap = HashMultimap.create();
		predicateMap = HashMultimap.create();
		objectMap = HashMultimap.create();

		expireSubjectQueue = new ConcurrentLinkedQueue<TTLEntry<RDFValue, Value>>();
		expirePredicateQueue = new ConcurrentLinkedQueue<TTLEntry<RDFValue, Value>>();
		expireObjectQueue = new ConcurrentLinkedQueue<TTLEntry<RDFValue, Value>>();
	}

	public void setObjectIndexing(boolean objectIndexing) {
		this.objectIndexing = objectIndexing;
	}

	public void setPredicateIndexing(boolean predicateIndexing) {
		this.predicateIndexing = predicateIndexing;
	}

	public void setSubjectIndexing(boolean subjectIndexing) {
		this.subjectIndexing = subjectIndexing;
	}

	public void setWindowInMillis(long windowInMillis) {
		this.windowInMillis = windowInMillis;
	}

	public void addElement(RDFTriple e, Value value, long timestamp) {

		if (subjectIndexing) {
			RDFValue subject = e.getSubject();
			// WeakReference<RDFValue> weakSubject = new
			// WeakReference<RDFValue>(subject);
			subjectMap.put(subject, value);

			if(timestamp != 0){
				TTLEntry<RDFValue, Value> ttlEntry = new TTLEntry<RDFValue, Value>(subject, value, timestamp + windowInMillis);
				expireSubjectQueue.add(ttlEntry);
			}
		}
		
		if (predicateIndexing) {
			RDFValue predicate = e.getPredicate();
			predicateMap.put(predicate, value);

			if(timestamp != 0){
				TTLEntry<RDFValue, Value> ttlEntry = new TTLEntry<RDFValue, Value>(predicate, value, timestamp + windowInMillis);
				expirePredicateQueue.add(ttlEntry);
			}					
		}
		
		if (objectIndexing) {
			RDFValue object = e.getObject();
			objectMap.put(object, value);

			if(timestamp != 0){
				TTLEntry<RDFValue, Value> ttlEntry = new TTLEntry<RDFValue, Value>(object, value, timestamp + windowInMillis);
				expireObjectQueue.add(ttlEntry);
			}
		}
	}

	public Set<Value> getElementsFromSubjectIndex(RDFValue e) {
		removeExpiredEntries();
		return subjectMap.get(e);
	}

	public Set<Value> getElementsFromPredicateIndex(RDFValue e) {
		removeExpiredEntries();
		return predicateMap.get(e);
	}
	
	public Set<Value> getElementsFromObjectIndex(RDFValue e) {
		removeExpiredEntries();
		return objectMap.get(e);
	}

	private long getNow() {
		return System.currentTimeMillis();
	}
	
	private void removeExpiredEntries(){
		
		if(subjectIndexing)
			removeEntries(expireSubjectQueue,subjectMap);
		
		if(predicateIndexing)
			removeEntries(expirePredicateQueue, predicateMap);
		
		if(objectIndexing)
			removeEntries(expireObjectQueue, objectMap);
		
	}

	private void removeEntries(
			ConcurrentLinkedQueue<TTLEntry<RDFValue, Value>> queue,
			HashMultimap<RDFValue, Value> map) {

		long now = getNow();

		Iterator<TTLEntry<RDFValue, Value>> iterator = queue.iterator();

		for (; iterator.hasNext();) {
			TTLEntry<RDFValue, Value> next = iterator.next();
			if (next.getTTL() < now) {
				RDFValue rdfValue = next.getKey();
				Value value = next.getValue();
				
//				System.out.println("Removed "+value);
				// remove procedure
				value.remove();
				map.remove(rdfValue, value);
				iterator.remove();

			} else {
				break;
			}
		}

	}

}

class TTLEntry<Key, Value> {

	private final Key key;
	private final Value value;
	private final long ttl;

	public TTLEntry(Key key, Value value, long ttl) {
		this.key = key;
		this.value = value;
		this.ttl = ttl;
	}

	public long getTTL() {
		return ttl;
	}

	public Value getValue() {
		return value;
	}

	public Key getKey() {
		return key;
	}

}
