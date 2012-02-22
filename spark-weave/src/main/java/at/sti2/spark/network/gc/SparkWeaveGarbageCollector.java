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
package at.sti2.spark.network.gc;

import java.util.Iterator;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.epsilon.network.run.Token;
import at.sti2.spark.network.SparkWeaveNetwork;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.alpha.AlphaMemory;

/**
 * Sparkweave Garbage Collector implementation
 * 
 * v0.1 - Cleans Epsilon and Rete networks from WME/triples which are staled
 * v0.2 - Tests to see if triples are permanent. In case they are permanent they are not touched.
 * 
 * 
 * @author skomazec
 *
 */
public class SparkWeaveGarbageCollector extends Thread {

	private SparkWeaveNetwork sparkWeaveNetwork = null;
	private long gcSessionDelay = 0l;
	
	public SparkWeaveGarbageCollector(SparkWeaveNetwork sparkWeaveNetwork, long gcSessionDelay){
		this.sparkWeaveNetwork = sparkWeaveNetwork;
		this.gcSessionDelay = gcSessionDelay;
	}
	
	public void run(){
		
		while(true){
			
			/**
			 * GC sleeps for given amount of ms
			 */
			try {
				Thread.sleep(gcSessionDelay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/************************************************
			 *           CLEANING EPSILON NETWORK
			 ************************************************/
			
			synchronized(sparkWeaveNetwork.getEpsilonNetwork().getProcessedTriples()){
				
				for (Iterator <Triple> ptIter = sparkWeaveNetwork.getEpsilonNetwork().getProcessedTriples().iterator(); ptIter.hasNext(); ){
					
					Triple processedTriple = ptIter.next();
					
					for (Token token : sparkWeaveNetwork.getEpsilonNetwork().getTokenNodesByStreamedTriple(processedTriple))
						token.removeTokenFromNode();
					
					//Remove the list of tokens for given streamed triple
					sparkWeaveNetwork.getEpsilonNetwork().removeListByStreamedTriple(processedTriple);
					
					//Remove the streamed triple from the list
					ptIter.remove();
				}
			}
			
			/************************************************
			 *            CLEANING RETE NETWORK
			 ************************************************/
			
			//GC wakes up and goes through the list WorkingMemoryElements to clean them up
			long gcThresholdTimestamp = sparkWeaveNetwork.getLastTimestamp() - sparkWeaveNetwork.getTimeWindowLength();
			
			StringBuffer buffer = new StringBuffer("AM MEM ALLOC ");
			
			//Loop over all alpha memories in RETE and check WMEs which they hold
			for (AlphaMemory alphaMemory : sparkWeaveNetwork.getReteNetwork().getWorkingMemory().getAlphaMemories()){
				
				synchronized(alphaMemory.getItems()){
				
					buffer.append('[');
					buffer.append(alphaMemory.getItems().size());
					buffer.append(',');
					
					for (Iterator <WorkingMemoryElement> wmeIterator = alphaMemory.getItems().iterator(); wmeIterator.hasNext(); ){
				
						WorkingMemoryElement wme = wmeIterator.next();
						
						if ((!wme.getTriple().isPermanent()) && wme.getTriple().getTimestamp() < gcThresholdTimestamp){
							
							/**
							 * Here we need to delete all references to the WME:
							 *  1. AlphaMemory which points to this WME
							 *  2. Tokens which point to this WME 
							 *  3. WorkingMemory list of all WMEs
							 */
							wme.remove();
							
							//Removing the 
							wmeIterator.remove();
						}
					}
					
					buffer.append(alphaMemory.getItems().size());
					buffer.append(']');
					buffer.append(' ');
				}
			}
			
			buffer.append('\n');
			System.out.println(buffer.toString());
			System.out.println(sparkWeaveNetwork.getReteNetwork().getBetaMemoryLevels());
		}
	}
}
