package at.sti2.spark.grammar;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import at.sti2.spark.grammar.pattern.Pattern;

public class SparkLexerParserTest {
	
	static protected Logger logger = Logger.getLogger(SparkLexerParserTest.class);
	
	@Test
	public void testPatternSelect() throws IOException{

		SparkPatternParser parser = new SparkPatternParser("target/classes/patternSelect.tpg");
		Pattern patternGraph = parser.parse();
		
		Assert.assertNotNull(patternGraph);
		Assert.assertTrue(patternGraph.getPrefixes().size() > 0);
		Assert.assertTrue(patternGraph.getWherePattern().getWhereConditions().size() > 0);
		Assert.assertTrue(patternGraph.getWherePattern().getTimeWindowLength() > 0);
	}
	
	@Test
	public void testPatternConstruct() throws IOException{
		
		SparkPatternParser parser = new SparkPatternParser("target/classes/patternConstruct.tpg");
		Pattern patternGraph = parser.parse();
		
		Assert.assertNotNull(patternGraph);
		Assert.assertTrue(patternGraph.getPrefixes().size() > 0);
		Assert.assertTrue(patternGraph.getConstructConditions().size() > 0);
		Assert.assertTrue(patternGraph.getWherePattern().getTimeWindowLength() > 0);
		Assert.assertTrue(patternGraph.getHandlers().size() > 0 );
	}
	
	@Test
	public void testPatternFilter() throws IOException{
		
		SparkPatternParser parser = new SparkPatternParser("target/classes/patternFilter.tpg");
		Pattern patternGraph = parser.parse();
		
		Assert.assertNotNull(patternGraph);
		Assert.assertTrue(patternGraph.getPrefixes().size() > 0);
		Assert.assertTrue(patternGraph.getWherePattern().getTimeWindowLength() > 0);
		Assert.assertTrue(patternGraph.getWherePattern().getFilter().size() > 0);
	}

}
