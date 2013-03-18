package at.sti2.spark.handler;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.grammar.SparkPatternParser;
import at.sti2.spark.grammar.pattern.Handler;
import at.sti2.spark.grammar.pattern.Pattern;

public class SupportHandlerTest {
	
	static Logger logger = Logger.getLogger(SupportHandlerTest.class);

	private Match match = null;
	private List<Handler> handlerProperties = null;
	
	@Before
	public void init(){
		
		String patternFileName = "target/test-classes/support/supportHandler_pattern.tpg";
		SparkPatternParser parser = new SparkPatternParser(patternFileName);
		Pattern patternGraph = null;
		try {
			patternGraph = parser.parse();
			handlerProperties = patternGraph.getHandlers();
		} catch (IOException e) {
			logger.error("Could not open pattern file "+patternFileName);
		}

		
		Hashtable<String, RDFValue> variableBindings = new Hashtable<String, RDFValue>();
		variableBindings.put("sensor1", new RDFURIReference("http://www.foi.se/support/wp4demo#PAT_1"));
		variableBindings.put("sensor2", new RDFURIReference("http://www.foi.se/support/wp4demo#PET_3"));
		variableBindings.put("detection1", new RDFURIReference("http://www.foi.se/support/wp4demo#Observation_20110830_102125_943_PAT_01021_1"));
		variableBindings.put("detection2", new RDFURIReference("http://www.foi.se/support/wp4demo#Observation_20110830_102125_943_PET_01021_3"));
		variableBindings.put("location1", new RDFURIReference("http://www.foi.se/support/wp4demo#Quay_A"));
		variableBindings.put("location2", new RDFURIReference("http://www.foi.se/support/wp4demo#Quay_B"));
		
		match = new Match();
		match.setVariableBindings(variableBindings);
		
	}
	
	@Test
	public void testSupportHandler() throws SparkwaveHandlerException{
		
		Handler handler = handlerProperties.get(0);
		
//		File logFile = new File(handler.getValue("path"));
//		if(logFile.exists())
//			logFile.delete();
//		
//		SparkwaveHandler fileHandler = new FileHandler();
//		fileHandler.init(handler);
//		fileHandler.invoke(match);
//		
//		logFile = new File(handler.getValue("path"));
//		Assert.assertTrue(logFile.exists());
		
		
		SupportHandler supportHandler = new SupportHandler();
		supportHandler.init(handler);
		supportHandler.invoke(match);
		
	}
}
