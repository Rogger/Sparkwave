/*
 * Copyright (c) 2012, University of Innsbruck, Austria.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * You should have received a copy of the GNU Lesser General Public License along
 * with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package at.sti2.sparkwave.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;

import com.google.common.base.Stopwatch;

public class StreamParserThread implements Runnable {

	static Logger logger = Logger.getLogger(StreamParserThread.class);

	private List<BlockingQueue<Triple>> queues;
	private boolean run = true;
	private BufferedReader streamReader;

	public StreamParserThread(InputStream streamReader, List<BlockingQueue<Triple>> queues) {
		this.queues = queues;
		this.streamReader = new BufferedReader(new InputStreamReader(streamReader));
	}

	public void run() {

		long tripleCounter = 0;

		try {
			
			Stopwatch stopWatch = new Stopwatch();
			stopWatch.start();
			
			while (run) {

				// Get triple from the stream
				Triple triple = getTriple();
				tripleCounter++;

				if (!triple.isPoisonTriple()) {

					long currentTimeMillis = System.currentTimeMillis();
					triple.setTimestamp(currentTimeMillis);
					
					// put triple in all queues
					for(BlockingQueue<Triple> queue : queues){
						queue.put(triple);
					}
					
				} else {
					run = false;
				}
			}
			
			stopWatch.stop();

			StringBuffer timeBuffer = new StringBuffer();
			timeBuffer.append("Streaming took ["+ stopWatch.elapsedTime(TimeUnit.MILLISECONDS) + "ms] ");
			timeBuffer.append(stopWatch.elapsedTime(TimeUnit.MINUTES));
			timeBuffer.append(" min ");
			timeBuffer.append(stopWatch.elapsedTime(TimeUnit.SECONDS));
			timeBuffer.append(" s ");
			timeBuffer.append(stopWatch.elapsedTime(TimeUnit.MILLISECONDS));
			timeBuffer.append(" ms.");

			logger.info(timeBuffer.toString());
			logger.info("Processed " + tripleCounter + " triples.");
//			logger.info("Pattern has been matched "+ sparkwaveNetwork.getReteNetwork().getNumMatches()+ " times.");

		} catch (InterruptedException e) {
			logger.error(e);
		}
	}
	
	private Triple getTriple(){
		
		char[] charBuf = new char[256];
		int c=0;
		
		try{
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
				
				// search for triple end
				while ((c=getChar()) != '.'){
					
					// datatype
					if(c == '^'){
						while ((c=getChar()) != '<')
							continue;
						
						pos=0;
						while ((c = getChar()) != '>') {
							charBuf[pos] = (char)c;
							pos++;
						}
						char[] dataTypeChar = Arrays.copyOf(charBuf, pos);
						RDFURIReference datatypeURI = new RDFURIReference(String.valueOf(dataTypeChar));
					}
				}
				tripObject = RDFLiteral.Factory.createLiteral(String.valueOf(lexicalChar),datatypeURI,null);
				
			}
			
			if(run){
				RDFTriple rdfTriple = new RDFTriple(tripSubject,tripPredicate,tripObject);
				return new Triple(rdfTriple,0, false, 0l);
			}
		} catch (IOException e) {
			logger.error(e);
			run = false;
		} catch (InterruptedException e) {
			logger.error(e);
			run = false;
		} catch (EndOfStreamException e) {
			run = false;
		}
		
		return getPoisonTriple();
	}

	private int getChar() throws IOException, InterruptedException, EndOfStreamException{
		int c;
		if((c = streamReader.read()) != -1){
			return c;
		}else{
			throw new EndOfStreamException();			
		}
	}
	
	
	private Triple getPoisonTriple(){
		Triple poisonTriple = new Triple();
		poisonTriple.setStopTriple(true);
		return poisonTriple;
	}
}
