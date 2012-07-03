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
package at.sti2.spark.network;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.google.common.base.Stopwatch;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFVariable;
import at.sti2.spark.core.triple.TripleCondition;
import at.sti2.spark.core.triple.TripleConstantTest;
import at.sti2.spark.grammar.pattern.GroupGraphPattern;
import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.spark.input.N3FileInput;

public class SparkWeaveFirstBSBMTest extends TestCase {

	private List <RDFTriple> triples = null;
	private SparkwaveNetwork sparkWeaveNetwork = null;
	private File ontologyFile = null;
	
	static Logger logger = Logger.getLogger(SparkWeaveFirstBSBMTest.class);
	
	protected void setUp() throws Exception {
		super.setUp();
		
		/*
   			?x			http://www.w3.org/1999/02/22-rdf-syntax-ns#type 						http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Offer .
   			?x 			http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/product 		?product .
   			?x 			http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/vendor 		?vendor .
   			?x 			http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/price 		?price .
   			?x 			http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/validFrom 	?from .
   			?x 			http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/validTo 		?to .
   			?x 			http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/deliveryDays 	?delivery .
   			?x 			http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/offerWebpage 	?webpage .
   			?x 			http://purl.org/dc/elements/1.1/publisher 								?publisher .
   			?x 			http://purl.org/dc/elements/1.1/date 									?date .
   			?product 	http://www.w3.org/1999/02/22-rdf-syntax-ns#type 						http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType2 .
		 */
		
		/*
		 * ?x
		 * http://www.w3.org/1999/02/22-rdf-syntax-ns#type
		 * http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Offer
		 */
		
		RDFTriple triple1 = new RDFTriple();
		triple1.setSubject(new RDFVariable("?x"));
		triple1.setPredicate(new RDFURIReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
		triple1.setObject(new RDFURIReference("http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Offer"));
		
		TripleConstantTest tripleConstantTest1 = new TripleConstantTest("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", RDFTriple.Field.PREDICATE);
		TripleConstantTest tripleConstantTest2 = new TripleConstantTest("http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/Offer", RDFTriple.Field.OBJECT);
		
		TripleCondition tripleCondition1 = new TripleCondition();
		tripleCondition1.setConditionTriple(triple1);
		tripleCondition1.addConstantTest(tripleConstantTest1);
		tripleCondition1.addConstantTest(tripleConstantTest2);
		
		/*
		 * ?x
		 * http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/product
		 * ?product
		 */
		
		RDFTriple triple2 = new RDFTriple();
		triple2.setSubject(new RDFVariable("?x"));
		triple2.setPredicate(new RDFURIReference("http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/product"));
		triple2.setObject(new RDFVariable("?product"));
		
		TripleConstantTest tripleConstantTest3 = new TripleConstantTest("http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/product", RDFTriple.Field.PREDICATE);
		
		TripleCondition tripleCondition2 = new TripleCondition();
		tripleCondition2.setConditionTriple(triple2);
		tripleCondition2.addConstantTest(tripleConstantTest3);
		
		/*
		 * ?product
		 * http://www.w3.org/1999/02/22-rdf-syntax-ns#type
		 * http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType2 .
		 */
		
		RDFTriple triple3 = new RDFTriple();
		triple3.setSubject(new RDFVariable("?product"));
		triple3.setPredicate(new RDFURIReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
		triple3.setObject(new RDFURIReference("http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType2"));
		
		TripleConstantTest tripleConstantTest4 = new TripleConstantTest("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", RDFTriple.Field.PREDICATE);
		TripleConstantTest tripleConstantTest5 = new TripleConstantTest("http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/ProductType2", RDFTriple.Field.OBJECT);
		
		TripleCondition tripleCondition3 = new TripleCondition();
		tripleCondition3.setConditionTriple(triple3);
		tripleCondition3.addConstantTest(tripleConstantTest4);
		tripleCondition3.addConstantTest(tripleConstantTest5);
		
		// ----- BUILDING A TRIPLE GRAPH PATTERN -----
		
		GroupGraphPattern wherePattern = new GroupGraphPattern();
		wherePattern.addWhereCondition(tripleCondition1);
		wherePattern.addWhereCondition(tripleCondition2);
		wherePattern.addWhereCondition(tripleCondition3);
		
		// --- SET TIME WINDOW ---
		wherePattern.setTimeWindowLength(1000);
		
		Pattern patternGraph = new Pattern();
		patternGraph.setWherePattern(wherePattern);
		
		ontologyFile = new File("target/classes/BSBM/epsilon-PT2-DS329.owl");
		
		sparkWeaveNetwork = new SparkwaveNetwork(patternGraph, ontologyFile);
		sparkWeaveNetwork.buildNetwork();
		
		//Print Rete network structure
		sparkWeaveNetwork.getReteNetwork().printNetworkStructure();
		
		triples = new ArrayList <RDFTriple> ();
		
		//Setup data
		N3FileInput n3FileInput = new N3FileInput("target/classes/BSBM/offers-trunc_10.nt");
		n3FileInput.parseTriples();
		triples = n3FileInput.getTriples();
	}
	
	public void testNetworkProcessing(){
		
		Stopwatch stopWatch = new Stopwatch();
		stopWatch.start();
		
		logger.info("Processing " + triples.size() + " triples.");
		
		for (RDFTriple triple : triples){
			Triple sTriple = new Triple(triple, (new Date()).getTime(), false, 0l);
			sparkWeaveNetwork.activateNetwork(sTriple);
		}
		
		stopWatch.stop();
		StringBuffer timeBuffer = new StringBuffer();
		timeBuffer.append("Streaming took ["+ stopWatch.elapsedTime(TimeUnit.MILLISECONDS) + "ms] ");
		logger.info(timeBuffer.toString());
		logger.info("Processed " + triples.size() + " triples.");
		long numMatches = sparkWeaveNetwork.getReteNetwork().getNumMatches();
		logger.info("Pattern has been matched "+ numMatches+ " times.");
		
		assertTrue(numMatches == 2);
	}
}
