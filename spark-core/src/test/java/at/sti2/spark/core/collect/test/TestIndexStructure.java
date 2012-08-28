package at.sti2.spark.core.collect.test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import junit.framework.TestCase;

import at.sti2.spark.core.collect.IndexStructure;
import at.sti2.spark.core.collect.Removable;
import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;

public class TestIndexStructure {
	
//	public void testGuavaMap() throws InterruptedException{
//		LinkedHashMultimap<WeakReference<RDFValue>, WeakReference<WorkingMemoryElement>> subjectItems = LinkedHashMultimap.create();
//		LinkedHashMultimap<WeakReference<RDFValue>, WeakReference<WorkingMemoryElement>> predicateItems = LinkedHashMultimap.create();
//		
//		//create object
//		RDFValue subject = new RDFURIReference("http://www.test.com/subject");
//		RDFValue subject1 = new RDFURIReference("http://www.test.com/subject");
//		WeakReference<RDFValue> weakSubject = new WeakReference<RDFValue>(subject1);
//	
//		RDFValue predicate = new RDFURIReference("http://www.test.com/predicate");
//		RDFValue predicate1 = new RDFURIReference("http://www.test.com/predicate");
//		WeakReference<RDFValue> weakPredicate = new WeakReference<RDFValue>(predicate1);
//		
//		RDFValue object = new RDFURIReference("http://www.test.com/object");
//		
//		WorkingMemoryElement wme = new WorkingMemoryElement(new Triple(new RDFTriple(subject,predicate,object),12,1));
//		WeakReference<WorkingMemoryElement> weakWME = new WeakReference<WorkingMemoryElement>(wme);
//		
//		//add object to subjectItems
//		subjectItems.put(weakSubject, weakWME);
//		//add object to predicateItems
//		predicateItems.put(weakPredicate, weakWME);
//		
//		System.out.println("subject items");
//		System.out.println(subjectItems);
//
//		System.out.println("predicate items");
//		System.out.println(predicateItems);
//		
//		//remove object from subjectItems
//		System.out.println("removing from predicate items");
//		System.out.println(weakWME);
//		predicateItems.values().remove(weakWME);
//		weakWME = null;
//		
//		while(true){
//			
//			Thread.sleep(3000);
//			System.gc();
//			
//			//is object in predicateItems?
//			System.out.println("subject items");
//			System.out.println(subjectItems);
//			
//			System.out.println("predicate items");
//			System.out.println(predicateItems);
//		}
//		
//	}
//	
//	
//	public void testWeakHashMap() throws InterruptedException{
//		WeakHashMap<RDFValue, Object> subMap = new WeakHashMap<RDFValue, Object>();
//		WeakHashMap<RDFValue, Object> predMap = new WeakHashMap<RDFValue, Object>();
//		
//		//create object
//		RDFURIReference subject = new RDFURIReference("http://www.test.com/subject");
//		RDFURIReference subjectKey = new RDFURIReference("http://www.test.com/subject");
//
//		RDFURIReference predicate = new RDFURIReference("http://www.test.com/predicate");
//		RDFURIReference predicateKey = new RDFURIReference("http://www.test.com/predicate");
//
//		RDFURIReference object = new RDFURIReference("http://www.test.com/object");
//		
//		WorkingMemoryElement wme = new WorkingMemoryElement(new Triple(new RDFTriple(subject,predicate,object),12,1));
//		Object o1 = new Object();
//		Object o2 = new Object();
//		
//		//add object to subjectItems
//		subMap.put(subject, o1);
//		//add object to predicateItems
//		predMap.put(subject, o2);
//		
//		System.out.println("subject items");
//		System.out.println(subMap);
//
//		System.out.println("predicate items");
//		System.out.println(predMap);
//		
//		//remove object from subjectItems
//		System.out.println("removing from predicate items");
//		System.out.println(wme);
//		predMap.remove(subject);
//		
//		for(int i = 0; i < 100000 ; i++){
//			
//			Thread.sleep(1000);
//			System.gc();
//			
//			//is object in predicateItems?
//			System.out.println("subject items");
//			System.out.println(subMap);
//			
//			System.out.println("predicate items");
//			System.out.println(predMap);
//		}
//		
//	}
	
	@Test
	public void testIndex() throws InterruptedException{
		
		Removable removableObject = new Removable() {
			
			@Override
			public void remove() {
				System.out.println("Got removed");
				
			}
		};
		
		IndexStructure<Removable> subjectIndex = new IndexStructure<Removable>();
		subjectIndex.setSubjectIndexing(true);
		subjectIndex.setWindowInMillis(5000);
		
		for(int i = 0; i < 1000 ; i++){

			//create object
			long now = System.currentTimeMillis();
			RDFURIReference subject = new RDFURIReference("http://www.test.com/subject");
			RDFURIReference predicate = new RDFURIReference("http://www.test.com/predicate");
			RDFURIReference object = new RDFURIReference("http://www.test.com/object");
			RDFTriple rdfTriple = new RDFTriple(subject, predicate, object);
			subjectIndex.addElement(rdfTriple, removableObject, now);

			Set<Removable> elementFromSubject = subjectIndex.getElementsFromSubjectIndex(subject);
//			System.out.println(elementFromSubject.size());
			
//			Thread.sleep(100);
		}
		
	}
	
	@Test
	public void testIndexWithoutIndexing() throws InterruptedException{
		
		
		IndexStructure<Removable> index = new IndexStructure<Removable>();
		index.setWindowInMillis(5000);
		
		for(int i = 0; i < 1000 ; i++){

			//create object
			long now = System.currentTimeMillis();
			RDFURIReference subject = new RDFURIReference("http://www.test.com/subject");
			RDFURIReference predicate = new RDFURIReference("http://www.test.com/predicate");
			RDFURIReference object = new RDFURIReference("http://www.test.com/object");
			RDFTriple rdfTriple = new RDFTriple(subject, predicate, object);
			
			Removable removableObject = new Removable() {
				
				@Override
				public void remove() {
					System.out.println("Got removed");
					
				}
			};
			
			index.addElement(rdfTriple, removableObject, now);

			ArrayList<Removable> elementsFromTripleQueue = index.getElementsFromTokenQueue();
			System.out.println(elementsFromTripleQueue.size());
			
//			index.remove(removableObject);
			
			Thread.sleep(100);
		}
		
	}
	

}
