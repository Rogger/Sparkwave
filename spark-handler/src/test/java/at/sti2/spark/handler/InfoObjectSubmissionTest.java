package at.sti2.spark.handler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.grammar.SparkParserResult;
import at.sti2.spark.grammar.SparkPatternParser;
import at.sti2.spark.grammar.pattern.Handler;
import at.sti2.spark.grammar.pattern.Pattern;

public class InfoObjectSubmissionTest {
	
	static Logger logger = LoggerFactory.getLogger(InfoObjectSubmissionTest.class);

	private Match match = null;
	private Handler handlerProperties = null;
	
	@Before
	public void init() throws Exception {
		
		final String host = "impactorium.epn.foi.se";
		final int port = 7070;
		final String path = "/impact";
		final String url = "http://"+host+":"+port+path;
		
		Assume.assumeTrue(checkServerAvailability(host,port));			
		
		File patternFileName = new File("target/test-classes/support_pattern2.tpg");
		SparkPatternParser parser = new SparkPatternParser();
		SparkParserResult parserResult = null;
		try {
			parserResult = parser.parse(patternFileName);
		} catch (IOException e) {
			logger.error("Could not open pattern file "+patternFileName);
		}
		
		Pattern patternGraph = parserResult.getPattern();
		
		handlerProperties = new Handler(patternGraph);
		handlerProperties.addKeyValue("baseurl",url);
		handlerProperties.setHandlerClass(null);
		
		Hashtable<String, RDFValue> variableBindings = new Hashtable<String, RDFValue>();
		variableBindings.put("?sensor1", RDFURIReference.Factory.createURIReference("http://www.foi.se/support/wp4demo#PAT_1"));
		variableBindings.put("?sensor2", RDFURIReference.Factory.createURIReference("http://www.foi.se/support/wp4demo#PET_3"));
		variableBindings.put("?detection1", RDFURIReference.Factory.createURIReference("http://www.foi.se/support/wp4demo#Observation_20110830_102125_943_PAT_01021_1"));
		variableBindings.put("?detection2", RDFURIReference.Factory.createURIReference("http://www.foi.se/support/wp4demo#Observation_20110830_102125_943_PET_01021_3"));
		
		match = new Match();
		match.setVariableBindings(variableBindings);
		
	}
	
	@Test
	public void testInfoObjectSubmission(){
		ImpactoriumHandler invoker = new ImpactoriumHandler();
		invoker.init(handlerProperties);
		try {
			invoker.invoke(match);
		} catch (SparkwaveHandlerException e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkServerAvailability(String host, int port){
		
		logger.info("Checking server (host:"+host+", port:"+port+") availability...");
		Socket socket = null;
		boolean reachable = false;
		try {
		    socket = new Socket(host, port);
		    reachable = true;
		} catch(Exception e){
			logger.warn("Host:"+host+" not available!");
		} finally {
		    if (socket != null) try { socket.close(); } catch(IOException e) {}
		}
		return reachable;
	}
}
