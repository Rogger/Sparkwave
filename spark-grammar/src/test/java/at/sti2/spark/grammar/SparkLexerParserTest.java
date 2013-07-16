package at.sti2.spark.grammar;

import java.io.File;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.spark.grammar.pattern.GraphPattern;
import at.sti2.spark.grammar.pattern.GroupGraphPattern;
import at.sti2.spark.grammar.pattern.LogicAndGraphPattern;
import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.spark.grammar.pattern.TemporalBeforeGraphPattern;
import at.sti2.spark.grammar.pattern.expression.FilterExpression;
import at.sti2.spark.grammar.pattern.expression.FilterOperator;

public class SparkLexerParserTest {
	
	static protected Logger logger = LoggerFactory.getLogger(SparkLexerParserTest.class);
	
	@Test
	public void testPatternSelect() throws Exception{

		SparkPatternParser parser = new SparkPatternParser();
		SparkParserResult parserResult = parser.parse(new File("target/test-classes/patternSelect.tpg"));
		Pattern patternGraph = parserResult.getPattern();
		
		Assert.assertNotNull(patternGraph);
		Assert.assertTrue(patternGraph.getPrefixes().size() > 0);
		
		GraphPattern whereClause = patternGraph.getWhereClause();
		Assert.assertTrue(whereClause instanceof GroupGraphPattern);
		Assert.assertTrue(((GroupGraphPattern)whereClause).getConditions().size()>0);
		Assert.assertTrue(((GroupGraphPattern)whereClause).getTimeWindowLength() > 0);
	}
	
	@Test
	public void testPatternConstruct() throws Exception{
		
		SparkPatternParser parser = new SparkPatternParser();
		SparkParserResult parserResult = parser.parse(new File("target/test-classes/patternConstruct.tpg"));
		Pattern patternGraph = parserResult.getPattern();
		
		Assert.assertNotNull(patternGraph);
		Assert.assertTrue(patternGraph.getPrefixes().size() > 0);
		Assert.assertTrue(patternGraph.getConstruct().getConditions().size() > 0);
		Assert.assertTrue(patternGraph.getHandlers().size() > 0 );
		
		GraphPattern whereClause = patternGraph.getWhereClause();
		Assert.assertTrue(((GroupGraphPattern)whereClause).getTimeWindowLength() > 0);
	}
	
	@Test
	public void testPatternFilter() throws Exception{
		
		SparkPatternParser parser = new SparkPatternParser();
		SparkParserResult parserResult = parser.parse(new File("target/test-classes/patternFilter.tpg"));
		Pattern patternGraph = parserResult.getPattern();
		
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
	public void testPatternLogicAndOperator() throws Exception{
		SparkPatternParser parser = new SparkPatternParser();
		SparkParserResult parserResult = parser.parse(new File("target/test-classes/patternLogicOperator.tpg"));
		Pattern patternGraph = parserResult.getPattern();

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
		SparkPatternParser parser = new SparkPatternParser();
		SparkParserResult parserResult = parser.parse(new File("target/test-classes/patternTemporalBefore.tpg"));
		Pattern patternGraph = parserResult.getPattern();
		
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
		SparkPatternParser parser = new SparkPatternParser();
		SparkParserResult parserResult = parser.parse(new File("target/test-classes/patternEOandSI.tpg"));
		Pattern patternGraph = parserResult.getPattern();
		
		String epsilonOntology = patternGraph.getEpsilonOntology();
		String staticInstances = patternGraph.getStaticInstances();
		
		Assert.assertTrue( !epsilonOntology.equals("") );
		Assert.assertTrue( !staticInstances.equals("") );
	}
	
	@Test
	public void testStringPatternWithEscapingCharacters() throws Exception{
		SparkPatternParser parser = new SparkPatternParser();
		String str = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \nPREFIX support: <http://www.foi.se/support/wp4demo#> \n\nEPSILON_ONTOLOGY = \"target/test-classes/support/support_schema.owl\"\n\nSTATIC_INSTANCES = \"target/test-classes/support/instances.nt\"\n\nHANDLERS { \nHANDLER {\n\"class\" = \"at.sti2.spark.handler.SupportHandler\"\n\"twominfilter\" = \"true\"\n\"url\" = \"http://www.support-project.eu/SupportPlatform/SupportPlatformEventService.svc/event\"\n}\nHANDLER {\n\"class\" = \"at.sti2.spark.handler.ConsoleHandler\"\n\"verbose\" = \"true\"\n}\n}\n\nCONSTRUCT {\n<http://www.foi.se/support/wp4demo#EventX> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.foi.se/support/wp4demo#Event> .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.foi.se/support/wp4demo#PETPATDetection> .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#name> \"PETPATDetection\" .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#generated-by> <http://www.foi.se/support/wp4demo#StreamProcessor> .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#location> ?location1 .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#location> ?location2 .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#date> \"NOW()\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#report_based_on_service> ?sensor1 .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#report_based_on_service> ?sensor2 .\n}\n\nWHERE {\n?detection1 <http://www.foi.se/support/wp4demo#has_status> \"true\" .\n?detection1 <http://www.foi.se/support/wp4demo#has_sensor> ?sensor1 .\n?sensor1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.foi.se/support/wp4demo#PETSensor> .\n?sensor1 <http://www.foi.se/support/wp4demo#has_longitude> ?s1long .\n?sensor1 <http://www.foi.se/support/wp4demo#sensor_has_location> ?location1 .\n?location1 <http://www.foi.se/support/wp4demo#location_is_part_of_location> <http://www.foi.se/support/wp4demo#DockX> .\n?detection2 <http://www.foi.se/support/wp4demo#has_status> \"true\" .\n?detection2 <http://www.foi.se/support/wp4demo#has_sensor> ?sensor2 .\n?sensor2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.foi.se/support/wp4demo#PATSensor> .\n?sensor2 <http://www.foi.se/support/wp4demo#sensor_has_location> ?location2 .\n?location2 <http://www.foi.se/support/wp4demo#location_is_part_of_location> <http://www.foi.se/support/wp4demo#DockX> .\nTIMEWINDOW (100) \n}\n";
		CharStream input = new ANTLRStringStream(str); 
		SparkParserResult parserResult = parser.parse(input);
		Pattern patternGraph = parserResult.getPattern();
		
		String epsilonOntology = patternGraph.getEpsilonOntology();
		String staticInstances = patternGraph.getStaticInstances();
		Assert.assertTrue( !epsilonOntology.equals("") );
		Assert.assertTrue( !staticInstances.equals("") );
		
		Assert.assertNotNull(patternGraph);
		Assert.assertTrue(patternGraph.getPrefixes().size() > 0);
		Assert.assertTrue(patternGraph.getConstruct().getConditions().size() > 0);
		Assert.assertTrue(patternGraph.getHandlers().size() > 0 );
	}

}