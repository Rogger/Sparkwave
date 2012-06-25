package at.sti2.spark.handler;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import junit.framework.TestCase;
import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.grammar.SparkPatternParser;
import at.sti2.spark.grammar.pattern.Handler;
import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.spark.handler.ImpactoriumHandler;
import at.sti2.spark.handler.SparkweaveHandlerException;

public class InfoObjectSubmissionTest extends TestCase {
	
	static Logger logger = Logger.getLogger(InfoObjectSubmissionTest.class);

	private Match match = null;
	private Handler handlerProperties = null;
	
	public void setUp(){
		
		String patternFileName = "target/classes/support_pattern2.tpg";
		SparkPatternParser parser = new SparkPatternParser(patternFileName);
		Pattern patternGraph = null;
		try {
			patternGraph = parser.parse();
		} catch (IOException e) {
			logger.error("Could not open pattern file "+patternFileName);
		}
		
		handlerProperties = new Handler(patternGraph);
		handlerProperties.addKeyValue("baseurl","http://impactorium.epn.foi.se:7070/impact");
		handlerProperties.setHandlerClass(null);
		
		Hashtable<String, RDFValue> variableBindings = new Hashtable<String, RDFValue>();
		variableBindings.put("?sensor1", new RDFURIReference("http://www.foi.se/support/wp4demo#PAT_1"));
		variableBindings.put("?sensor2", new RDFURIReference("http://www.foi.se/support/wp4demo#PET_3"));
		variableBindings.put("?detection1", new RDFURIReference("http://www.foi.se/support/wp4demo#Observation_20110830_102125_943_PAT_01021_1"));
		variableBindings.put("?detection2", new RDFURIReference("http://www.foi.se/support/wp4demo#Observation_20110830_102125_943_PET_01021_3"));
		
		match = new Match();
		match.setVariableBindings(variableBindings);
		
	}
	
	public void testInfoObjectSubmission(){
		ImpactoriumHandler invoker = new ImpactoriumHandler();
		invoker.init(handlerProperties);
		try {
			invoker.invoke(match);
		} catch (SparkweaveHandlerException e) {
			e.printStackTrace();
		}
	}
}
