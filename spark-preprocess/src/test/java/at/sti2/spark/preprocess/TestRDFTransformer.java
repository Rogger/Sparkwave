package at.sti2.spark.preprocess;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;


public class TestRDFTransformer {
	
	@Before
	public void init() {

	}
	
	@Test
	public void testNTToRDFXML() throws FileNotFoundException{
		FileInputStream in = new FileInputStream("target/test-classes/Example.n3");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		RDFFormatTransformer formatTransformer = new RDFFormatTransformer();
		formatTransformer.init(in, baos);
		formatTransformer.setProperty("from", "N3");
		formatTransformer.setProperty("to", "RDF/XML-ABBREV");
		formatTransformer.process();
		
		System.out.println(baos.toString());
		
	}
	
	@Test
	public void testRDFXMLToNT() throws FileNotFoundException{
		FileInputStream in = new FileInputStream("target/test-classes/Example.rdf.xml");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		RDFFormatTransformer formatTransformer = new RDFFormatTransformer();
		formatTransformer.init(in, baos);
		formatTransformer.setProperty("from", "RDF/XML-ABBREV");
		formatTransformer.setProperty("to", "N-TRIPLE");
		formatTransformer.process();
		
		System.out.println(baos.toString());
		
	}

}
