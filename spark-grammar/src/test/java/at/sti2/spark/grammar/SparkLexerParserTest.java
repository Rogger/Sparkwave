package at.sti2.spark.grammar;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.spark.grammar.pattern.expression.FilterExpression;
import at.sti2.spark.grammar.pattern.expression.FilterOperator;

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
		
		List<FilterExpression> filters = patternGraph.getWherePattern().getFilters();
		Assert.assertTrue(filters.size() == 6);
		
		Assert.assertTrue(filters.get(0).getFilterOperator() == FilterOperator.EQUAL);
		Assert.assertTrue(filters.get(1).getFilterOperator() == FilterOperator.NOT_EQUAL);
		Assert.assertTrue(filters.get(2).getFilterOperator() == FilterOperator.LESS);
		Assert.assertTrue(filters.get(3).getFilterOperator() == FilterOperator.LESS_EQUAL);
		Assert.assertTrue(filters.get(4).getFilterOperator() == FilterOperator.GREATER);
		Assert.assertTrue(filters.get(5).getFilterOperator() == FilterOperator.GREATER_EQUAL);
	}

}
