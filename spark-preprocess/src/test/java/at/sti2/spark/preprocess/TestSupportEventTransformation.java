package at.sti2.spark.preprocess;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Test;

public class TestSupportEventTransformation {

	@Test
	/**
	 * Test the transformation: n-triple -> rdf/xml -> event xml
	 * @throws FileNotFoundException
	 */
	public void testEventNTtoEventXML() throws FileNotFoundException{
		FileInputStream in = new FileInputStream("target/test-classes/support/Event.nt");
		ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
		
		RDFFormatTransformer formatTransformer = new RDFFormatTransformer();
		formatTransformer.init(in, baos1);
		formatTransformer.setProperty("from", "N3");
		formatTransformer.setProperty("to", "RDF/XML-ABBREV");
		formatTransformer.process();
		System.out.println(baos1.toString());
		
		// copy output from rdftransformer to input for xslttransformer
		ByteArrayInputStream bridge = new java.io.ByteArrayInputStream(baos1.toByteArray()); 
		
		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		XSLTransformer transformer = new XSLTransformer();
		transformer.init(bridge, baos2);
		transformer.setProperty("xsltLocation", "target/test-classes/support/fromRDFToEvent.xslt");
		transformer.process();
		System.out.println(baos2.toString());
	}
	
	/**
	 * N3->EventXML 
	 */
	@Test
	public void testSensorsNTtoEventXML() throws FileNotFoundException{
//		FileInputStream in = new FileInputStream("target/test-classes/support/Event.nt");
//		ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
//		
//		RDFFormatTransformer formatTransformer = new RDFFormatTransformer();
//		formatTransformer.init(in, baos1);
//		formatTransformer.setProperty("from", "N3");
//		formatTransformer.setProperty("to", "RDF/XML-ABBREV");
//		formatTransformer.process();
//		System.out.println(baos1.toString());
//		
//		// copy output from rdftransformer to input for xslttransformer
//		ByteArrayInputStream bridge = new java.io.ByteArrayInputStream(baos1.toByteArray()); 
//				
//		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
//		XSLTransformer transformer = new XSLTransformer();
//		transformer.init(bridge, baos2);
//		transformer.setProperty("xsltLocation", "target/test-classes/support/fromRDFToEvent.xslt");
//		transformer.process();
//		System.out.println(baos2.toString());
	}
	
	/**
	 * EventXML -> N3
	 * @throws FileNotFoundException 
	 */
	@Test
	public void testSensorsEventToNT() throws FileNotFoundException{
		FileInputStream in = new FileInputStream("target/test-classes/support/sensors/Sensor_PAT_8.xml");
		ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
		
		XSLTransformer transformer = new XSLTransformer();
		transformer.init(in, baos1);
		transformer.setProperty("xsltLocation", "target/test-classes/support/fromEventToRDF.xslt");
		transformer.process();
//		System.out.println(baos1.toString());

		ByteArrayInputStream bridge = new java.io.ByteArrayInputStream(baos1.toByteArray()); 

		// copy output
		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		RDFFormatTransformer formatTransformer = new RDFFormatTransformer();
		formatTransformer.init(bridge, baos2);
		formatTransformer.setProperty("from", "RDF/XML-ABBREV");
		formatTransformer.setProperty("to", "N-TRIPLE");
		formatTransformer.process();
//		System.out.println(baos2.toString());
		
	}

}
