package at.sti2.spark.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;

public class NTripleStreamReader {

	private File inputFile = null;
	
	private List <RDFTriple> triples = new ArrayList <RDFTriple> ();
	
	private BufferedReader reader = null;
	
	public NTripleStreamReader(File inputFile){
		this.inputFile = inputFile;
	}
	
	public void parseTriples(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			
			String tripleLine = null;
			
			while((tripleLine = reader.readLine()) != null)
				triples.add(parseTriple(tripleLine));
				
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void openFile(){
		try {
			reader = new BufferedReader(new FileReader(inputFile));
		} catch (FileNotFoundException e) {
			System.out.println("Triple file not found.");
			e.printStackTrace();
		}
	}
	
	public void closeFile(){
		try {
			reader.close();
		} catch (IOException e) {
			System.out.println("Problem while closing triple file.");
			e.printStackTrace();
		}
	}
	
	public RDFTriple nextTriple(){
		String tripleLine = null;
		RDFTriple rdfTriple = null;
		
		try {
			if ((tripleLine = reader.readLine()) != null)
				rdfTriple = parseTriple(tripleLine);
			
		} catch (IOException e) {
			System.out.println("Problem while reading triple file.");
			e.printStackTrace();
		}
		
		return rdfTriple;
	}
	
	public String nextTripleLine(){
		String tripleLine = null;
		
		try {
			if ((tripleLine = reader.readLine()) != null)
				return tripleLine;
			
		} catch (IOException e) {
			System.out.println("Problem while reading triple file.");
			e.printStackTrace();
		}
		
		return tripleLine;
	}
	
	public RDFTriple parseTriple(String tripleLine){
		
		StringTokenizer tokenizer = new StringTokenizer(tripleLine);
		
		String subject = tokenizer.nextToken();
		String predicate = tokenizer.nextToken();
		String object = tokenizer.nextToken();
		
		RDFURIReference tripSubject = new RDFURIReference(subject.substring(subject.indexOf('<') + 1, subject.indexOf('>')));
		RDFURIReference tripPredicate = new RDFURIReference(predicate.substring(predicate.indexOf('<') + 1, predicate.indexOf('>')));
		
		RDFValue tripObject = null;
		
		if (object.startsWith("\"")){
			
			//This is literal
			String lexicalForm = null;
	    	String languageTag = null;
	    	RDFURIReference datatypeURI = null;
	    	
	    	//Extract language tag
	    	//TODO Extract properly language tag
//	    	if (!object.asLiteral().getLanguage().equals(""))
//	    		languageTag = object.asLiteral().getLanguage();
	    	
	    	//Extract lexical form
	    	StringTokenizer literalTokenizer = new StringTokenizer(object, "^^");
	    	
	    	lexicalForm = literalTokenizer.nextToken();
	    	lexicalForm = lexicalForm.substring(lexicalForm.indexOf('\"') + 1, lexicalForm.lastIndexOf('\"'));
	    	
	    	//Extract datatypeURI
	    	String datatypeToken = literalTokenizer.nextToken();
	    	if (datatypeToken != null)
	    		datatypeURI = new RDFURIReference(datatypeToken.substring(datatypeToken.indexOf('<') + 1, datatypeToken.indexOf('>')));
	    	
	    	tripObject = new RDFLiteral(lexicalForm, datatypeURI, languageTag);
			
		}else{
			//Is is URL
			tripObject = new RDFURIReference(object.substring(object.indexOf('<') + 1, object.indexOf('>')));
		}
		
		return new RDFTriple(tripSubject, tripPredicate, tripObject);
	}

	public List<RDFTriple> getTriples() {
		return triples;
	}
}
