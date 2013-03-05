package at.sti2.spark.preprocess;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

public class XSLTransformer implements Runnable{
	
	private Logger logger = Logger.getLogger(XSLTransformer.class);
	
	Source xslt = null;
	InputStream in;
	OutputStream out;
	
	public XSLTransformer(Source xslt, InputStream in, OutputStream out) {
		super();
		this.xslt = xslt;
		this.in = in;
		this.out = out;
	}

	public void process(){
		Source xmlSource = new StreamSource(in);
		StreamResult result = new StreamResult(out);
		transform(xslt, xmlSource, result);
	}
	
	private void transform(Source xsltSource, Source xmlSource, Result result){
		
		if(xsltSource == null || xmlSource == null || result == null){
			throw new IllegalArgumentException();
		}
		
//		StringWriter writer = new StringWriter();
		
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setAttribute("indent-number", new Integer(2));
		
		Transformer transformer;
		
		try {
			transformer = factory.newTransformer(xsltSource);
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(xmlSource, result);
			
			//TODO PROBLEM is the pipedinputstream is not closed, readLine will wait forever!!
			
//			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//			String line = null;
//			try {
//				while((line = reader.readLine()) != null) {
//					  System.out.println(line);
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.debug(out.toString());
		
	}

	@Override
	public void run() {
		process();
	}

}
