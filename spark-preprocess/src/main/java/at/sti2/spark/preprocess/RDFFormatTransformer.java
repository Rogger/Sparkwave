package at.sti2.spark.preprocess;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.RDFWriter;

/**
 * This class transforms data from inputstream from one RDF format to another RDF format. Both "from" and "to" properties need to be specified. Example values are RDF/XML-ABBREV","N-TRIPLE","N3".
 * @author michaelrogger
 *
 */
public class RDFFormatTransformer implements PreProcess,Runnable {
	
	private Logger logger = Logger.getLogger(RDFFormatTransformer.class);
	
	InputStream in;
	OutputStream out;
	String inLanguage;
	String outLanguage;
	
	@Override
	public void init(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}
	
	@Override
	public void setProperty(String key, String value) {
		if(key.equals("from")){
			inLanguage = value;
		}else if(key.equals("to")){
			outLanguage = value;
		}
	}
	
	@Override
	public void process() {
		transform(in, out, inLanguage, outLanguage);
	}

	public void transform(InputStream in, OutputStream out, String inLanguage, String outLanguage){
		
		if(in == null || out == null || inLanguage == null || outLanguage == null){
			throw new IllegalArgumentException();			
		}
		
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
