package at.sti2.spark.preprocess;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLTransformer implements PreProcess,Runnable{
	
	private Logger logger = LoggerFactory.getLogger(XSLTransformer.class);
	
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
			transformer.setErrorListener(errorListener);
			transformer.transform(xmlSource, result);
			
			logger.debug("XSLTransformer output:\n "+baos.toString());

			
		} catch ( Exception e) {
			// do nothing, error listener should handle it		
		}finally{
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		
	}

	@Override
	public void run() {
		process();
	}
	
	ErrorListener errorListener = new ErrorListener() {
		
		@Override
		public void warning(TransformerException exception)
				throws TransformerException {
			logger.warn(exception.getMessage());
		}
		
		@Override
		public void fatalError(TransformerException exception)
				throws TransformerException {
			logger.error(exception.getMessage());
		}
		
		@Override
		public void error(TransformerException exception)
				throws TransformerException {
			logger.error(exception.getMessage());
		}
	};

}
