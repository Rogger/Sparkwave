package at.sti2.spark.network.gc;

import java.util.Iterator;

import at.sti2.spark.core.stream.StreamedTriple;
import at.sti2.spark.epsilon.network.run.Token;
import at.sti2.spark.network.SparkWeaveNetwork;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.alpha.AlphaMemory;

public class SparkWeaveGarbageCollector extends Thread {

	private SparkWeaveNetwork sparkWeaveNetwork = null;
	private long gcSessionDelay = 0l;
	
	public SparkWeaveGarbageCollector(SparkWeaveNetwork sparkWeaveNetwork, long gcSessionDelay){
		this.sparkWeaveNetwork = sparkWeaveNetwork;
		this.gcSessionDelay = gcSessionDelay;
	}
	
	public void run(){
		
		while(true){
			
			//GC sleeps for given amount of time
			try {
				this.currentThread().sleep(gcSessionDelay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			/************************************************
			 *           CLEANING EPSILON NETWORK
			 ************************************************/
			
			synchronized(sparkWeaveNetwork.getEpsilonNetwork().getProcessedTriples()){
				
//				System.out.println("Number of processed triples before gc " + sparkWeaveNetwork.getEpsilonNetwork().getProcessedTriples().size());
				
				for (Iterator <StreamedTriple> ptIter = sparkWeaveNetwork.getEpsilonNetwork().getProcessedTriples().iterator(); ptIter.hasNext(); ){
					
					StreamedTriple processedTriple = ptIter.next();
					
					for (Token token : sparkWeaveNetwork.getEpsilonNetwork().getTokenNodesByStreamedTriple(processedTriple))
						token.removeTokenFromNode();
					
					//Remove the list of tokens for given streamed triple
					sparkWeaveNetwork.getEpsilonNetwork().removeListByStreamedTriple(processedTriple);
					
					//Remove the streamed triple from the list
					ptIter.remove();
				}
				
//				System.out.println("Number of processed triples after gc " + sparkWeaveNetwork.getEpsilonNetwork().getProcessedTriples().size());
			}
			
			/************************************************
			 *            CLEANING RETE NETWORK
			 ************************************************/
			
			//GC wakes up and goes through the list WorkingMemoryElements to clean them up
			long gcThresholdTimestamp = sparkWeaveNetwork.getLastTimestamp() - sparkWeaveNetwork.getTimeWindowLength();
			
			//Loop over all alpha memories in RETE and check WMEs which they hold
			for (AlphaMemory alphaMemory : sparkWeaveNetwork.getReteNetwork().getWorkingMemory().getAlphaMemories()){
				
				synchronized(alphaMemory.getItems()){
				
					for (Iterator <WorkingMemoryElement> wmeIterator = alphaMemory.getItems().iterator(); wmeIterator.hasNext(); ){
				
						WorkingMemoryElement wme = wmeIterator.next();
						
						if (wme.getStreamedTriple().getTimestamp() < gcThresholdTimestamp){
							
							/**
							 * Here we need to delete all references to the WME:
							 *  1. AlphaMemory which points to this WME
							 *  2. Tokens which point to this WME 
							 *  3. WorkingMemory list of all WMEs
							 */
							wme.remove();
							wmeIterator.remove();
						}
					}
				}
			}
		}
	}
}
