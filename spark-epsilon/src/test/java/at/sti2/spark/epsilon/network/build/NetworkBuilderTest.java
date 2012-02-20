package at.sti2.spark.epsilon.network.build;

import java.io.File;

import junit.framework.TestCase;
import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.epsilon.network.run.EpsilonNetwork;

public class NetworkBuilderTest extends TestCase {

	public void testBuilding(){
		
		NetworkBuilder builder = new NetworkBuilder(new File("resources/ontology.n3"));
		EpsilonNetwork epsilonNetwork = builder.buildNetwork();
		
		RDFTriple triple = new RDFTriple();
		RDFURIReference subject = new RDFURIReference("http://example.org/ns/family-ontology#Srdjan");
		RDFURIReference predicate = new RDFURIReference("http://example.org/ns/family-ontology#hasWife");
		RDFURIReference object = new RDFURIReference("http://example.org/ns/family-ontology#Irena");
		
		triple.setSubject(subject);
		triple.setPredicate(predicate);
		triple.setObject(object);
		
		Triple s1Triple = new Triple(triple, 0l, false, 0l);
		
		epsilonNetwork.activate(s1Triple);
		
		assertNotNull(epsilonNetwork);
		
	}
}
