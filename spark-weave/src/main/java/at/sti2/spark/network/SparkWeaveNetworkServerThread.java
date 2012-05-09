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
package at.sti2.spark.network;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.epsilon.network.run.Token;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.alpha.AlphaMemory;

public class SparkWeaveNetworkServerThread implements Runnable {

	static Logger logger = Logger
			.getLogger(SparkWeaveNetworkServerThread.class);

	private SparkWeaveNetwork sparkWeaveNetwork = null;
	private BlockingQueue<Triple> blockingQueue;
	private boolean run = true;

	public SparkWeaveNetworkServerThread(SparkWeaveNetwork sparkWeaveNetwork,
			BlockingQueue<Triple> queue) {
		this.sparkWeaveNetwork = sparkWeaveNetwork;
		this.blockingQueue = queue;
	}

	public void run() {

		long tripleCounter = 0;
		long startProcessingTime;
		long endProcessingTime;

		try {
			startProcessingTime = System.currentTimeMillis();

			while (run) {

				// get triple from queue
				Triple triple = blockingQueue.take();

				if (!triple.isPoisonTriple()) {

					// activate network
					long currentTimeMillis = System.currentTimeMillis();
					triple.setTimestamp(currentTimeMillis);
					sparkWeaveNetwork.activateNetwork(triple);

					// GC
					tripleCounter++;
					if (tripleCounter % 2 == 0)
						runGC();
				} else {
					run = false;
				}

				// if (tripleCounter%1000 == 0){
				// logger.info(sparkWeaveNetwork.getEpsilonNetwork().getNetwork().getEpsilonMemoryLevels());
				// logger.info(sparkWeaveNetwork.getReteNetwork().getWorkingMemory().getAlphaMemoryLevels());
				// logger.info(sparkWeaveNetwork.getReteNetwork().getBetaMemoryLevels());

				// logger.info("Processing " + (1000/(sTriple.getTimestamp() -
				// timepoint)) + " triples/sec.");
				// timepoint = sTriple.getTimestamp();
				// }

			}

			endProcessingTime = System.currentTimeMillis();

			StringBuffer timeBuffer = new StringBuffer();
			timeBuffer.append("Processing took ["
					+ (endProcessingTime - startProcessingTime) + "ms] ");
			timeBuffer
					.append((endProcessingTime - startProcessingTime) / 60000);
			timeBuffer.append(" min ");
			timeBuffer
					.append(((endProcessingTime - startProcessingTime) % 60000) / 1000);
			timeBuffer.append(" s ");
			timeBuffer.append((endProcessingTime - startProcessingTime) % 1000);
			timeBuffer.append(" ms.");

			logger.info(timeBuffer.toString());
			logger.info("Pattern has been matched "
					+ sparkWeaveNetwork.getReteNetwork().getNumMatches()
					+ " times.");

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void runGC() {

		/************************************************
		 * CLEANING EPSILON NETWORK
		 ************************************************/

		for (Iterator<Triple> ptIter = sparkWeaveNetwork.getEpsilonNetwork()
				.getProcessedTriples().iterator(); ptIter.hasNext();) {

			Triple processedTriple = ptIter.next();

			for (Token token : sparkWeaveNetwork.getEpsilonNetwork()
					.getTokenNodesByStreamedTriple(processedTriple))
				token.removeTokenFromNode();

			// Remove the list of tokens for given streamed triple
			sparkWeaveNetwork.getEpsilonNetwork().removeListByStreamedTriple(
					processedTriple);

			// Remove the streamed triple from the list
			ptIter.remove();
		}

		/************************************************
		 * CLEANING RETE NETWORK
		 ************************************************/

		// GC wakes up and goes through the list WorkingMemoryElements to clean
		// them up
		// long gcThresholdTimestamp = sparkWeaveNetwork.getLastTimestamp() -
		// sparkWeaveNetwork.getTimeWindowLength();
//		long gcThresholdTimestamp = System.currentTimeMillis()
//				- sparkWeaveNetwork.getTimeWindowLength();
//
//		// StringBuffer buffer = new StringBuffer("AM MEM ALLOC ");
//
//		// Loop over all alpha memories in RETE and check WMEs which they hold
//		for (AlphaMemory alphaMemory : sparkWeaveNetwork.getReteNetwork()
//				.getWorkingMemory().getAlphaMemories()) {
//
//			// buffer.append('[');
//			// buffer.append(alphaMemory.getItems().size());
//			// buffer.append(',');
//
//			for (Iterator<WorkingMemoryElement> wmeIterator = alphaMemory.getSubjects().iterator(); wmeIterator.hasNext();) {
//
//				WorkingMemoryElement wme = wmeIterator.next();
//
//				if (wme.getTriple().getTimestamp() < gcThresholdTimestamp) {
//
//					/**
//					 * Here we need to delete all references to the WME: 1.
//					 * AlphaMemory which points to this WME 2. Tokens which
//					 * point to this WME 3. WorkingMemory list of all WMEs
//					 */
//					wme.remove();
//
//					// Removing the
//					wmeIterator.remove();
//				} else {
//					break;
//				}
//			}
//
//			// buffer.append(alphaMemory.getItems().size());
//			// buffer.append(']');
//			// buffer.append(' ');
//		}
		// buffer.append('\n');
		// System.out.println(buffer.toString());
		// System.out.println(sparkWeaveNetwork.getReteNetwork().getBetaMemoryLevels());
	}
}
