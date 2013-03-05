package at.sti2.spark.preprocess;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;

public class TestXSLTTransformer {
	
	@Before
	public void init() {

	}
	
	@Test
	public void fromRDFtoEvent() throws Exception{
		
		Source xslt = new StreamSource(new File("target/test-classes/fromRDFToEvent.xslt"));
		FileInputStream in = new FileInputStream("target/test-classes/Example.rdf.xml");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XSLTransformer transformer = new XSLTransformer(xslt, in, baos);
		transformer.process();
		System.out.println(baos.toString());
		
	}
	
	@Test
	public void fromEventToRDF() throws Exception{
		
		Source xslt = new StreamSource(new File("target/test-classes/fromEventToRDF.xslt"));
		FileInputStream in = new FileInputStream("target/test-classes/Example.xml");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		XSLTransformer transformer = new XSLTransformer(xslt, in, baos);
		transformer.process();
		System.out.println(baos.toString());
		
	}

}
