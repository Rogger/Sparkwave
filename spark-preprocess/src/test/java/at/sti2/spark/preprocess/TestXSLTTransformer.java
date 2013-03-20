package at.sti2.spark.preprocess;

import java.io.FileInputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;

public class TestXSLTTransformer {
	
	@Before
	public void init() {

	}
	
	@Test
	public void fromRDFtoEvent() throws Exception{
		
		FileInputStream in = new FileInputStream("target/test-classes/Example.rdf.xml");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XSLTransformer transformer = new XSLTransformer();
		transformer.init(in, baos);
		transformer.setProperty("xsltLocation", "target/test-classes/fromRDFToEvent.xslt");
		transformer.process();
		System.out.println(baos.toString());
		
	}
	
	@Test
	public void fromEventToRDF() throws Exception{
		
		FileInputStream in = new FileInputStream("target/test-classes/support/Event.xml");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XSLTransformer transformer = new XSLTransformer();
		transformer.init(in, baos);
		transformer.setProperty("xsltLocation", "target/test-classes/fromEventToRDF.xslt");
		transformer.process();
		System.out.println(baos.toString());
		
	}

}
