package at.sti2.spark.input;

import java.io.BufferedReader;
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

private String fileName = null;
	
	private List <RDFTriple> triples = new ArrayList <RDFTriple> ();
	
	public NTripleStreamReader(String fileName){
		this.fileName = fileName;
	}
	
	public void parseTriples(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			
			String tripleLine = null;
			
			long statementCount = 0;
			
			while((tripleLine = reader.readLine()) != null){
				
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
//			    	if (!object.asLiteral().getLanguage().equals(""))
//			    		languageTag = object.asLiteral().getLanguage();
			    	
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
				
				RDFTriple triple = new RDFTriple(tripSubject, tripPredicate, tripObject);
				
				System.out.println(triple.toString());
				
				triples.add(triple);
				
				statementCount++;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<RDFTriple> getTriples() {
		return triples;
	}
}
