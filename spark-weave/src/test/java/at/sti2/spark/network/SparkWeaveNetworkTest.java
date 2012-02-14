package at.sti2.spark.network;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import at.sti2.spark.core.stream.StreamedTriple;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.variable.RDFVariable;
import at.sti2.spark.rete.N3FileInput;
import at.sti2.spark.rete.condition.TripleCondition;
import at.sti2.spark.rete.condition.TripleConstantTest;
import at.sti2.spark.rete.condition.TriplePatternGraph;

public class SparkWeaveNetworkTest extends TestCase {

	private List <RDFTriple> triples = null;
	private SparkWeaveNetwork sparkWeaveNetwork = null;
	private File ontologyFile = null;
	
	static Logger logger = Logger.getLogger(SparkWeaveNetworkTest.class);
	
	protected void setUp() throws Exception {
		super.setUp();
		
		/**
		 * A test case which examines how the network is built for the following graph pattern
		 * 
		 *     <?x     "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"						"http://knoesis.wright.edu/ssw/ont/weather.owl#WindObservation">
		 *     <?x     "http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result"		?y>
		 *     <?x     "http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#samplingTime"  ?z>
		 *   D  <?y     "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"						"http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#MeasureData">
		 *     <?y     "http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"	?floatValue>
		 *   D  <?z     "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"						"http://www.w3.org/2006/time#Instant">
		 *     <?z     "http://www.w3.org/2006/time#inXSDDateTime"								?instant>
		 */
		
		logger.debug("Building up representation for triple <?x \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" \"http://knoesis.wright.edu/ssw/ont/weather.owl#WindObservation\">");
		
		RDFTriple triple1 = new RDFTriple();
		triple1.setSubject(new RDFVariable("?x"));
		triple1.setPredicate(new RDFURIReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
		triple1.setObject(new RDFURIReference("http://knoesis.wright.edu/ssw/ont/weather.owl#WindObservation"));
		
		TripleConstantTest tripleConstantTest1 = new TripleConstantTest("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", RDFTriple.Field.PREDICATE);
		TripleConstantTest tripleConstantTest2 = new TripleConstantTest("http://knoesis.wright.edu/ssw/ont/weather.owl#WindObservation", RDFTriple.Field.OBJECT);
		
		TripleCondition tripleCondition1 = new TripleCondition();
		tripleCondition1.setConditionTriple(triple1);
		tripleCondition1.addConstantTest(tripleConstantTest1);
		tripleCondition1.addConstantTest(tripleConstantTest2);
		
		
		logger.debug("Building up representation for triple <?x \"http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result\" ?y>");
		
		RDFTriple triple2 = new RDFTriple();
		triple2.setSubject(new RDFVariable("?x"));
		triple2.setPredicate(new RDFURIReference("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result"));
		triple2.setObject(new RDFVariable("?y"));
		
		TripleConstantTest tripleConstantTest3 = new TripleConstantTest("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result", RDFTriple.Field.PREDICATE);
		
		TripleCondition tripleCondition2 = new TripleCondition();
		tripleCondition2.setConditionTriple(triple2);
		tripleCondition2.addConstantTest(tripleConstantTest3);
		
		logger.debug("Building up representation for triple <?x \"http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#samplingTime\" ?z>");
		
		RDFTriple triple3 = new RDFTriple();
		triple3.setSubject(new RDFVariable("?x"));
		triple3.setPredicate(new RDFURIReference("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#samplingTime"));
		triple3.setObject(new RDFVariable("?z"));
		
		TripleConstantTest tripleConstantTest4 = new TripleConstantTest("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#samplingTime", RDFTriple.Field.PREDICATE);
		
		TripleCondition tripleCondition3 = new TripleCondition();
		tripleCondition3.setConditionTriple(triple3);
		tripleCondition3.addConstantTest(tripleConstantTest4);
		
		/**
		 * <?y     "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"						"http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#MeasureData">
		 */
		RDFTriple triple4 = new RDFTriple();
		triple4.setSubject(new RDFVariable("?y"));
		triple4.setPredicate(new RDFURIReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
		triple4.setObject(new RDFURIReference("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#MeasureData"));
		
		TripleConstantTest tripleConstantTest5 = new TripleConstantTest("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", RDFTriple.Field.PREDICATE);
		TripleConstantTest tripleConstantTest6 = new TripleConstantTest("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#MeasureData", RDFTriple.Field.OBJECT);
		
		TripleCondition tripleCondition4 = new TripleCondition();
		tripleCondition4.setConditionTriple(triple4);
		tripleCondition4.addConstantTest(tripleConstantTest5);
		tripleCondition4.addConstantTest(tripleConstantTest6);
		
		
		/**
		 * <?y     "http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"	?floatValue>
		 */
		RDFTriple triple5 = new RDFTriple();
		triple5.setSubject(new RDFVariable("?y"));
		triple5.setPredicate(new RDFURIReference("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"));
		triple5.setObject(new RDFVariable("?floatValue"));
		
		TripleConstantTest tripleConstantTest7 = new TripleConstantTest("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue", RDFTriple.Field.PREDICATE);
		
		TripleCondition tripleCondition5 = new TripleCondition();
		tripleCondition5.setConditionTriple(triple5);
		tripleCondition5.addConstantTest(tripleConstantTest7);
		
		/**
		 * 	   <?z     "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"						"http://www.w3.org/2006/time#Instant">
		 */
		RDFTriple triple6 = new RDFTriple();
		triple6.setSubject(new RDFVariable("?z"));
		triple6.setPredicate(new RDFURIReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
		triple6.setObject(new RDFURIReference("http://www.w3.org/2006/time#Instant"));
		
		TripleConstantTest tripleConstantTest8 = new TripleConstantTest("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", RDFTriple.Field.PREDICATE);
		TripleConstantTest tripleConstantTest9 = new TripleConstantTest("http://www.w3.org/2006/time#Instant", RDFTriple.Field.OBJECT);
		
		TripleCondition tripleCondition6 = new TripleCondition();
		tripleCondition6.setConditionTriple(triple6);
		tripleCondition6.addConstantTest(tripleConstantTest8);
		tripleCondition6.addConstantTest(tripleConstantTest9);
		
		/**
		 *     <?z     "http://www.w3.org/2006/time#inXSDDateTime"								?instant>
		 */
		RDFTriple triple7 = new RDFTriple();
		triple7.setSubject(new RDFVariable("?z"));
		triple7.setPredicate(new RDFURIReference("http://www.w3.org/2006/time#inXSDDateTime"));
		triple7.setObject(new RDFVariable("?instant"));
		
		TripleConstantTest tripleConstantTest10 = new TripleConstantTest("http://www.w3.org/2006/time#inXSDDateTime", RDFTriple.Field.PREDICATE);
		
		TripleCondition tripleCondition7 = new TripleCondition();
		tripleCondition7.setConditionTriple(triple7);
		tripleCondition7.addConstantTest(tripleConstantTest10);
		
		// ----- BUILDING A TRIPLE GRAPH PATTERN -----
		
		TriplePatternGraph patternGraph = new TriplePatternGraph();
		patternGraph.addTripleCondition(tripleCondition1);
		patternGraph.addTripleCondition(tripleCondition2);
		patternGraph.addTripleCondition(tripleCondition3);
		//patternGraph.addTripleCondition(tripleCondition4);
		patternGraph.addTripleCondition(tripleCondition5);
		//patternGraph.addTripleCondition(tripleCondition6);
		patternGraph.addTripleCondition(tripleCondition7);
		
		// --- SET TIME WINDOW ---
		patternGraph.setTimeWindowLength(150);
		
		ontologyFile = new File("./resources/epsilon.owl");
		
		sparkWeaveNetwork = new SparkWeaveNetwork(patternGraph, ontologyFile, 200l);
		sparkWeaveNetwork.buildNetwork();
		
		//Print Rete network structure
		sparkWeaveNetwork.getReteNetwork().printNetworkStructure();
		
		triples = new ArrayList <RDFTriple> ();
		
		//Setup data
		N3FileInput n3FileInput = new N3FileInput("./resources/moretriples.n3");
		n3FileInput.parseTriples();
		triples = n3FileInput.getTriples();
	}
	
	public void testNetworkProcessing(){
		
		logger.info("Processing " + triples.size() + " triples.");
		
		for (RDFTriple triple : triples){
			StreamedTriple sTriple = new StreamedTriple(triple, (new Date()).getTime(), 0l);
			sparkWeaveNetwork.activateNetwork(sTriple);
		}
		
		assertTrue(true);
	}
}
