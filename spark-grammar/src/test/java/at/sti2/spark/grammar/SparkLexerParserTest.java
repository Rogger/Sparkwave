package at.sti2.spark.grammar;

import java.io.IOException;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import at.sti2.spark.grammar.SparkPatternParser;
import at.sti2.spark.grammar.pattern.Pattern;

public class SparkLexerParserTest extends TestCase {
	
	static protected Logger logger = Logger.getLogger(SparkLexerParserTest.class);
	
	public void testPatternSelect() throws IOException{

		SparkPatternParser parser = new SparkPatternParser("target/classes/patternSelect.tpg");
		Pattern patternGraph = parser.parse();
		
		assertNotNull(patternGraph);
		assertTrue(patternGraph.getPrefixes().size() > 0);
		assertTrue(patternGraph.getWherePattern().getWhereConditions().size() > 0);
		assertTrue(patternGraph.getWherePattern().getTimeWindowLength() > 0);
	}
	
	public void testPatternConstruct() throws IOException{
		
		SparkPatternParser parser = new SparkPatternParser("target/classes/patternConstruct.tpg");
		Pattern patternGraph = parser.parse();
		
		assertNotNull(patternGraph);
		assertTrue(patternGraph.getPrefixes().size() > 0);
		assertTrue(patternGraph.getConstructConditions().size() > 0);
		assertTrue(patternGraph.getWherePattern().getTimeWindowLength() > 0);
		assertTrue(patternGraph.getHandlers().size() > 0 );
	}
	
	public void testPatternFilter() throws IOException{
		
		SparkPatternParser parser = new SparkPatternParser("target/classes/patternFilter.tpg");
		Pattern patternGraph = parser.parse();
		
		assertNotNull(patternGraph);
		assertTrue(patternGraph.getPrefixes().size() > 0);
		assertTrue(patternGraph.getWherePattern().getTimeWindowLength() > 0);
		assertTrue(patternGraph.getWherePattern().getFilter().size() > 0);
	}

}
