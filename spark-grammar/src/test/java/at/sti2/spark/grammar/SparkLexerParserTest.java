package at.sti2.spark.grammar;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import at.sti2.spark.grammar.pattern.GraphPattern;
import at.sti2.spark.grammar.pattern.GroupGraphPattern;
import at.sti2.spark.grammar.pattern.LogicAndGraphPattern;
import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.spark.grammar.pattern.TemporalBeforeGraphPattern;
import at.sti2.spark.grammar.pattern.expression.FilterExpression;
import at.sti2.spark.grammar.pattern.expression.FilterOperator;

public class SparkLexerParserTest {
	
	static protected Logger logger = Logger.getLogger(SparkLexerParserTest.class);
	
	@Test
	public void testPatternSelect() throws IOException{

		SparkPatternParser parser = new SparkPatternParser("target/test-classes/patternSelect.tpg");
		Pattern patternGraph = parser.parse();
		
		Assert.assertNotNull(patternGraph);
		Assert.assertTrue(patternGraph.getPrefixes().size() > 0);
		
		GraphPattern whereClause = patternGraph.getWhereClause();
		Assert.assertTrue(whereClause instanceof GroupGraphPattern);
		Assert.assertTrue(((GroupGraphPattern)whereClause).getConditions().size()>0);
		Assert.assertTrue(((GroupGraphPattern)whereClause).getTimeWindowLength() > 0);
	}
	
	@Test
	public void testPatternConstruct() throws IOException{
		
		SparkPatternParser parser = new SparkPatternParser("target/test-classes/patternConstruct.tpg");
		Pattern patternGraph = parser.parse();
		
		Assert.assertNotNull(patternGraph);
		Assert.assertTrue(patternGraph.getPrefixes().size() > 0);
		Assert.assertTrue(patternGraph.getConstruct().getConditions().size() > 0);
		Assert.assertTrue(patternGraph.getHandlers().size() > 0 );
		
		GraphPattern whereClause = patternGraph.getWhereClause();
		Assert.assertTrue(((GroupGraphPattern)whereClause).getTimeWindowLength() > 0);
	}
	
	@Test
	public void testPatternFilter() throws IOException{
		
		SparkPatternParser parser = new SparkPatternParser("target/test-classes/patternFilter.tpg");
		Pattern patternGraph = parser.parse();
		
		Assert.assertNotNull(patternGraph);
		Assert.assertTrue(patternGraph.getPrefixes().size() > 0);
		
		GraphPattern whereClause = patternGraph.getWhereClause();
		Assert.assertTrue(((GroupGraphPattern)whereClause).getTimeWindowLength() > 0);
		
		List<FilterExpression> filters = ((GroupGraphPattern)whereClause).getFilters();
		Assert.assertTrue(filters.size() == 6);
		
		Assert.assertTrue(filters.get(0).getFilterOperator() == FilterOperator.EQUAL);
		Assert.assertTrue(filters.get(1).getFilterOperator() == FilterOperator.NOT_EQUAL);
		Assert.assertTrue(filters.get(2).getFilterOperator() == FilterOperator.LESS);
		Assert.assertTrue(filters.get(3).getFilterOperator() == FilterOperator.LESS_EQUAL);
		Assert.assertTrue(filters.get(4).getFilterOperator() == FilterOperator.GREATER);
		Assert.assertTrue(filters.get(5).getFilterOperator() == FilterOperator.GREATER_EQUAL);
	}
	
	@Test
	public void testPatternLogicAndOperator() throws IOException{
		SparkPatternParser parser = new SparkPatternParser("target/test-classes/patternLogicOperator.tpg");
		Pattern patternGraph = parser.parse();
		
		Assert.assertTrue(patternGraph.getPrefixes().size() > 0);
		
		LogicAndGraphPattern whereClause = (LogicAndGraphPattern) patternGraph.getWhereClause();
		GroupGraphPattern left = (GroupGraphPattern) whereClause.getLeft();
		GroupGraphPattern right = (GroupGraphPattern) whereClause.getRight();
		
		Assert.assertTrue(left.getConditions().size() > 0);
		Assert.assertTrue(left.getTimeWindowLength() > 0);
		Assert.assertTrue(right.getConditions().size() > 0);
		Assert.assertTrue(right.getTimeWindowLength() > 0);
		
	}
	
	@Test
	public void testPatternTemporalBefore() throws Exception{
		SparkPatternParser parser = new SparkPatternParser("target/test-classes/patternTemporalBefore.tpg");
		Pattern patternGraph = parser.parse();
		
		Assert.assertTrue(patternGraph.getPrefixes().size() > 0);
		
		TemporalBeforeGraphPattern temporalBeforeGraphPattern = (TemporalBeforeGraphPattern) patternGraph.getWhereClause();
		GroupGraphPattern left = (GroupGraphPattern) temporalBeforeGraphPattern.getLeft();
		GroupGraphPattern right = (GroupGraphPattern) temporalBeforeGraphPattern.getRight();
		int lowerBound = temporalBeforeGraphPattern.getLowerBound();
		int upperBound = temporalBeforeGraphPattern.getUpperBound();
		
		Assert.assertTrue(lowerBound > 0);
		Assert.assertTrue(upperBound > 0);
		Assert.assertTrue(left.getConditions().size() > 0);
		Assert.assertTrue(left.getTimeWindowLength() > 0);
		Assert.assertTrue(right.getConditions().size() > 0);
		Assert.assertTrue(right.getTimeWindowLength() > 0);
		
	}
	
	@Test
	public void testPatternEOandSI() throws Exception{
		SparkPatternParser parser = new SparkPatternParser("target/test-classes/patternEOandSI.tpg");
		Pattern patternGraph = parser.parse();
		
		String epsilonOntology = patternGraph.getEpsilonOntology();
		String staticInstances = patternGraph.getStaticInstances();
		
		Assert.assertTrue( !epsilonOntology.equals("") );
		Assert.assertTrue( !staticInstances.equals("") );
	}

}
