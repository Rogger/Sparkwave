package at.sti2.spark.preprocess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.RDFWriter;

public class RDFFormatTransformer implements Runnable {
	
	private Logger logger = Logger.getLogger(RDFFormatTransformer.class);
	
	InputStream in;
	OutputStream out;
	String inLanguage;
	String outLanguage;
	
	/**
	 * 
	 * @param in the inputStream reading from
	 * @param out the outputStream writing to
	 * @param inLanguage language used for input stream
	 * @param outLanguage language the output stream is written
	 */
	public RDFFormatTransformer(InputStream in, OutputStream out, String inLanguage, String outLanguage) {
		super();
		
		if(in == null || out == null || inLanguage == null || outLanguage == null){
			throw new IllegalArgumentException();			
		}
		
		this.in = in;
		this.out = out;
		this.inLanguage = inLanguage;
		this.outLanguage = outLanguage;
	}

	public void process(){
		
		try{
			
		logger.info("Transforming RDF from "+inLanguage+" to "+outLanguage);
		
		Model model = ModelFactory.createDefaultModel();
		RDFReader rdfReader = model.getReader(inLanguage);
		rdfReader.read(model, in, null);
		
		
		RDFWriter rdfWriter = model.getWriter(outLanguage);
		rdfWriter.write(model, out, null);
		
		logger.debug(out.toString());
		
		}finally{
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		
	}

	@Override
	public void run() {
		process();
	}

}
