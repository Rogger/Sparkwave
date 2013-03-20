package at.sti2.spark.preprocess;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.log4j.Logger;

public class XSLTransformer implements PreProcess,Runnable{
	
	private Logger logger = Logger.getLogger(XSLTransformer.class);
	
	Source xslt = null;
	InputStream in = null;
	OutputStream out = null;
	
	public XSLTransformer() {
	}
	
	@Override
	public void init(InputStream in, OutputStream out) {
		this.in  = in;
		this.out = out;
	}
	
	@Override
	public void setProperty(String key, String value){
		if(key.equals("xsltLocation")){
			xslt = new StreamSource(new File(value));
		}
	}

	@Override
	public void process(){
		Source xmlSource = new StreamSource(in);
		transform(xslt, xmlSource, out);
	}
	
	private void transform(Source xsltSource, Source xmlSource, OutputStream out){
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		TeeOutputStream teeOut = new TeeOutputStream(out, baos);
		
		StreamResult result = new StreamResult(teeOut);
		
		if(xsltSource == null || xmlSource == null || result == null){
			throw new IllegalArgumentException();
		}
		
		logger.info("Performing XSLT transformation");
		
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setAttribute("indent-number", new Integer(2));
		
		Transformer transformer;
		
		try {
			transformer = factory.newTransformer(xsltSource);
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(xmlSource, result);
			
			logger.debug("XSLTransformer output:\n "+baos.toString());

			
		} catch (TransformerConfigurationException e) {
			logger.error(e);
		} catch (TransformerException e) {
			logger.error(e);
		} finally{			
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		
	}

	@Override
	public void run() {
		process();
	}

}
