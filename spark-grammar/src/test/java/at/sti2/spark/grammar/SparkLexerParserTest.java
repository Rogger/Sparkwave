package at.sti2.spark.grammar;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import at.sti2.spark.core.condition.TriplePatternGraph;

public class SparkLexerParserTest extends TestCase {
	
	static protected Logger logger = Logger.getLogger(SparkLexerParserTest.class);
	
	public void testPatternSelect() throws IOException{

		SparkPatternParser parser = new SparkPatternParser("target/classes/patternSelect.tpg");
		TriplePatternGraph patternGraph = parser.parse();
		
		assertNotNull(patternGraph);
		assertTrue(patternGraph.getPrefixes().size() > 0);
		assertTrue(patternGraph.getSelectConditions().size() > 0);
		assertTrue(patternGraph.getTimeWindowLength() > 0);
	}
	
	public void testPatternConstruct() throws IOException{
		
		SparkPatternParser parser = new SparkPatternParser("target/classes/patternConstruct.tpg");
		TriplePatternGraph patternGraph = parser.parse();
		
		assertNotNull(patternGraph);
		assertTrue(patternGraph.getPrefixes().size() > 0);
		assertTrue(patternGraph.getConstructConditions().size() > 0);
		assertTrue(patternGraph.getTimeWindowLength() > 0);
		assertTrue( !patternGraph.getInvokerProperties().getInvokerBaseURL().equals("") );
		assertTrue( !patternGraph.getInvokerProperties().getInvokerClass().equals("") );
	}

}
