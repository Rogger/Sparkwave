package at.sti2.spark.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

import at.sti2.spark.core.stream.StreamedTriple;
import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;

public class SparkWeaveNetworkServerThread extends Thread{

	private SparkWeaveNetwork sparkWeaveNetwork = null;
	private Socket socket = null;
	
	public SparkWeaveNetworkServerThread(SparkWeaveNetwork sparkWeaveNetwork, Socket socket){
		this.sparkWeaveNetwork = sparkWeaveNetwork;
		this.socket = socket;
	}
	
	public void run(){
		
//		long tripleCounter = 0;
//		long timepoint = (new Date()).getTime();
		long startProcessingTime;
		long endProcessingTime;
		
		try {
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String tripleLine = null;
			
			startProcessingTime = (new Date()).getTime();
			
			while ((tripleLine = streamReader.readLine()) != null) {
				
				StreamedTriple sTriple = new StreamedTriple(parseTriple(tripleLine), (new Date()).getTime(), 0l);
				sparkWeaveNetwork.activateNetwork(sTriple);
//				tripleCounter++;				
//				if (tripleCounter%1000 == 0){
//					System.out.println(sparkWeaveNetwork.getEpsilonNetwork().getNetwork().getEpsilonMemoryLevels());
//					System.out.println(sparkWeaveNetwork.getReteNetwork().getWorkingMemory().getAlphaMemoryLevels());
//					System.out.println(sparkWeaveNetwork.getReteNetwork().getBetaMemoryLevels());
					
//					System.out.println("Processing " + (1000/(sTriple.getTimestamp() - timepoint)) + " triples/sec.");
//					timepoint = sTriple.getTimestamp();
//				}
			}

			endProcessingTime = new Date().getTime();
			
			streamReader.close();
			socket.close();
			
			StringBuffer timeBuffer = new StringBuffer();
			timeBuffer.append("Processing took ");
			timeBuffer.append((endProcessingTime - startProcessingTime)/1000);
			timeBuffer.append(" s ");
			timeBuffer.append((endProcessingTime - startProcessingTime)%1000);
			timeBuffer.append(" ms.");
			
			System.out.println(timeBuffer.toString());			
			System.out.println("Pattern has been matched " + sparkWeaveNetwork.getReteNetwork().getNumMatches() + " times.");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private RDFTriple parseTriple(String tripleLine){
		
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
}
