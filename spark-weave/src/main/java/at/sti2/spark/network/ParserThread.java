package at.sti2.spark.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;

public class ParserThread implements Runnable{

	BufferedReader streamReader;
	BlockingQueue<Triple> queue;
	boolean run=true;
	SparkWeaveNetworkServerThread sparkThread;
	
	static Logger logger = Logger.getLogger(ParserThread.class);
	
	public ParserThread(BufferedReader streamReader, BlockingQueue<Triple> queue,SparkWeaveNetworkServerThread sparkThread) {
		this.streamReader = streamReader;
		this.queue = queue;
		this.sparkThread = sparkThread;
	}
	
	@Override
	public void run() {
		char[] charBuf = new char[256];
		int c=0;
		
		try {
			
			while(run){
				
				// ----------------------------------------------
				// Parse subject RDF node
				// ----------------------------------------------
				while (run && getChar() != '<')
					continue;

				// Copy URI value
				short pos=0;
				while (run && (c = getChar()) != '>') {
					charBuf[pos] = (char)c;
					pos++;
				}
				char[] subjectChar = Arrays.copyOf(charBuf, pos);
				RDFURIReference tripSubject = new RDFURIReference(String.valueOf(subjectChar));
				
				// ----------------------------------------------
				// Parse predicate RDF node
				// ----------------------------------------------
				while (run && getChar() != '<')
					continue;
				
				// Copy URI value
				pos=0;
				while (run && (c = getChar()) != '>') {
					charBuf[pos] = (char)c;
					pos++;
				}
				char[] predicateChar = Arrays.copyOf(charBuf, pos);
				RDFURIReference tripPredicate = new RDFURIReference(String.valueOf(predicateChar));
				
				
				// ----------------------------------------------
				// Parse object RDF node
				// ----------------------------------------------
				RDFValue tripObject = null;
				while (run && (c=getChar()) != '<' && c != '"')
					continue;
				
				if(run && c == '<'){
					
					// Copy URI value
					pos=0;
					while ((c = getChar()) != '>') {
						charBuf[pos] = (char)c;
						pos++;
					}
					char[] objectChar = Arrays.copyOf(charBuf, pos);
					tripObject = new RDFURIReference(String.valueOf(objectChar));
					
				}else if(run && c == '"'){

					pos=0;
					while ((c = getChar()) != '"') {
						charBuf[pos] = (char)c;
						pos++;
					}
					char[] lexicalChar = Arrays.copyOf(charBuf, pos);
					
					while (getChar() != '<')
						continue;
					
					pos=0;
					while ((c = getChar()) != '>') {
						charBuf[pos] = (char)c;
						pos++;
					}
					char[] dataTypeChar = Arrays.copyOf(charBuf, pos);
					
					RDFURIReference datatypeURI = new RDFURIReference(String.valueOf(dataTypeChar));
					tripObject = new RDFLiteral(String.valueOf(lexicalChar),datatypeURI,null);
				}
				
				if(run){
					RDFTriple rdfTriple = new RDFTriple(tripSubject,tripPredicate,tripObject);
					Triple triple = new Triple(rdfTriple,0, false, 0l);
					queue.put(triple);
				}
				
			}
				
		} catch (IOException e) {
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private int getChar() throws IOException, InterruptedException{
		int c;
		if((c = streamReader.read()) != -1){
//			System.out.print((char)c);
			return c;
		}
		
		stopProcessing();
		return -1;
	}
	
	
	public void stopProcessing() throws InterruptedException{
		run = false;
		
		Triple poisonTriple = new Triple();
		poisonTriple.setStopTriple(true);
		queue.put(poisonTriple);
	}
}
