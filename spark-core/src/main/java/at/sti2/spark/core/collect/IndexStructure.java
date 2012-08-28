/*
 * Copyright (c) 2012, University of Innsbruck, Austria.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package at.sti2.spark.core.collect;

import java.util.ArrayList;
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
	final ConcurrentLinkedQueue<TTLEntrySingle<Value>> expireTokenQueue; 

	private final HashMultimap<RDFValue, Value> subjectMap;
	private final HashMultimap<RDFValue, Value> predicateMap;
	private final HashMultimap<RDFValue, Value> objectMap;
	private final ArrayList<Value> tokenList; 

	public IndexStructure() {
		this.subjectIndexing = false;
		this.predicateIndexing = false;
		this.objectIndexing = false;
		this.windowInMillis = 0;

		subjectMap = HashMultimap.create();
		predicateMap = HashMultimap.create();
		objectMap = HashMultimap.create();
		
		tokenList = new ArrayList<Value>();
		expireTokenQueue = new ConcurrentLinkedQueue<TTLEntrySingle<Value>>();

		expireSubjectQueue = new ConcurrentLinkedQueue<TTLEntry<RDFValue, Value>>();
		expirePredicateQueue = new ConcurrentLinkedQueue<TTLEntry<RDFValue, Value>>();
		expireObjectQueue = new ConcurrentLinkedQueue<TTLEntry<RDFValue, Value>>();
	}
	
	/**
	 * Remove value from all indexes, should be only called if automatic expiration is not enough
	 * @param value
	 */
	public void remove(Value value){
		if(subjectIndexing)
			removeEntry(expireSubjectQueue,subjectMap,value);
		
		if(predicateIndexing)
			removeEntry(expirePredicateQueue, predicateMap,value);
		
		if(objectIndexing)
			removeEntry(expireObjectQueue, objectMap,value);
		
		//no indexing => all tokens are needed
		if(!subjectIndexing && !predicateIndexing && !objectIndexing){
			removeEntry(expireTokenQueue, tokenList, value);
		}
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
		
		//no indexing => all tokens are needed
		if(!subjectIndexing && !predicateIndexing && !objectIndexing){
			
			tokenList.add(value);
			if(timestamp != 0){
				TTLEntrySingle<Value> ttlEntry = new TTLEntrySingle<Value>(value, timestamp + windowInMillis);
				expireTokenQueue.add(ttlEntry);		
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
	
	public ArrayList<Value> getElementsFromTokenQueue(){
		removeExpiredEntries();
		return tokenList;
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
		
		//no indexing => all tokens are needed
		if(!subjectIndexing && !predicateIndexing && !objectIndexing){
			removeEntries(expireTokenQueue, tokenList);
		}
		
	}

	private void removeEntries(
			ConcurrentLinkedQueue<TTLEntry<RDFValue, Value>> queue,
			HashMultimap<RDFValue, Value> map) {

		long now = getNow();

		Iterator<TTLEntry<RDFValue, Value>> iterator = queue.iterator();

		for (; iterator.hasNext();) {
			TTLEntry<RDFValue, Value> next = iterator.next();
			if (next.isExpired() || next.getTTL() < now) {
				RDFValue rdfValue = next.getKey();
				Value value = next.getValue();
				
//				System.out.println("Removed "+value);
				// remove procedure
				value.remove();
				map.remove(rdfValue, value);
				iterator.remove();

			}
		}

	}
	
	private void removeEntries(ConcurrentLinkedQueue<TTLEntrySingle<Value>> queue, ArrayList<Value> tokenList){
		long now = getNow();
		
		Iterator<TTLEntrySingle<Value>> iterator = queue.iterator();
		
		for(; iterator.hasNext();){
			TTLEntrySingle<Value> next = iterator.next();
			if (next.isExpired() || next.getTTL() < now) {
				Value value = next.getValue();
				
				value.remove();
				tokenList.remove(value);
				iterator.remove();

			}
		}
	}
	
	private void removeEntry(
			ConcurrentLinkedQueue<TTLEntry<RDFValue, Value>> queue,
			HashMultimap<RDFValue, Value> map, Value deleteValue) {

		Iterator<TTLEntry<RDFValue, Value>> iterator = queue.iterator();

		for (; iterator.hasNext();) {
			TTLEntry<RDFValue, Value> next = iterator.next();
			RDFValue rdfValue = next.getKey();
			Value value = next.getValue();
			
			if(value == deleteValue){
				next.setExpired();
				break;
			}

		}

	}
	
	private void removeEntry(ConcurrentLinkedQueue<TTLEntrySingle<Value>> queue, ArrayList<Value> tokenList, Value deleteValue){
		
		Iterator<TTLEntrySingle<Value>> iterator = queue.iterator();
		
		for(; iterator.hasNext();){
			TTLEntrySingle<Value> next = iterator.next();
				Value value = next.getValue();
				
				if(value == deleteValue){
					next.setExpired();
					break;
				}

		} 
	}

}

class TTLEntry<Key, Value> {

	private final Key key;
	private final Value value;
	private final long ttl;
	private boolean markedAsExpired = false;

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
	
	public void setExpired(){
		markedAsExpired = true;
	}
	
	public boolean isExpired(){
		return markedAsExpired;
	}

}

class TTLEntrySingle<Value> {

	private final Value value;
	private final long ttl;
	private boolean markedAsExpired = false;

	public TTLEntrySingle(Value value, long ttl) {
		this.value = value;
		this.ttl = ttl;
	}

	public long getTTL() {
		return ttl;
	}

	public Value getValue() {
		return value;
	}
	
	public void setExpired(){
		markedAsExpired = true;
	}
	
	public boolean isExpired(){
		return markedAsExpired;
	}

}
