package at.sti2.spark.invoke;

import java.util.Hashtable;

import junit.framework.TestCase;
import at.sti2.spark.core.condition.TriplePatternGraph;
import at.sti2.spark.core.invoker.InvokerProperties;
import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.language.query.SparkPatternParser;

public class InfoObjectSubmissionTest extends TestCase {

	private Match match = null;
	private InvokerProperties invokerProperties = null;
	
	public void setUp(){
		
		SparkPatternParser parser = new SparkPatternParser("./resources/support_pattern2.tpg");
		TriplePatternGraph patternGraph = parser.parse();
		
		invokerProperties = new InvokerProperties(patternGraph);
		invokerProperties.setInvokerBaseURL("http://impactorium.epn.foi.se:7070/impact");
		invokerProperties.setInvokerClass(null);
		
		Hashtable<String, RDFValue> variableBindings = new Hashtable<String, RDFValue>();
		variableBindings.put("?sensor1", new RDFURIReference("http://www.foi.se/support/wp4demo#PAT_1"));
		variableBindings.put("?sensor2", new RDFURIReference("http://www.foi.se/support/wp4demo#PET_3"));
		variableBindings.put("?detection1", new RDFURIReference("http://www.foi.se/support/wp4demo#Observation_20110830_102125_943_PAT_01021_1"));
		variableBindings.put("?detection2", new RDFURIReference("http://www.foi.se/support/wp4demo#Observation_20110830_102125_943_PET_01021_3"));
		
		match = new Match();
		match.setVariableBindings(variableBindings);
		
	}
	
	public void testInfoObjectSubmission(){
		ImpactoriumInvoker invoker = new ImpactoriumInvoker();
		try {
			invoker.invoke(match, invokerProperties);
		} catch (SparkweaveInvokerException e) {
			e.printStackTrace();
		}
	}
}
