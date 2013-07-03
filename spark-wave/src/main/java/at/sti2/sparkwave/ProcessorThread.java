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
package at.sti2.sparkwave;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.epsilon.network.run.Token;

/**
 * 
 * @author michaelrogger
 * 
 */
public class ProcessorThread implements Runnable {

	static Logger logger = Logger.getLogger(ProcessorThread.class);

	private SparkwaveNetwork sparkwaveNetwork = null;
	private BlockingQueue<Triple> queue = null;

	private boolean isTerminated = false;

	public ProcessorThread(SparkwaveNetwork sparkwaveNetwork,
			BlockingQueue<Triple> queue) {
		this.sparkwaveNetwork = sparkwaveNetwork;
		this.queue = queue;
	}

	/**
	 * The thread takes triples from the queue and runs them through the
	 * Sparkwave network instance
	 */
	public void run() {

		long tripleCounter = 0;
		Triple triple = null;

		while (!isTerminated) {
			try {
				// get triple from queue
				triple = queue.take();
				if (triple != null) {
					// logger.info(triple);
					sparkwaveNetwork.activateNetwork(triple);

					tripleCounter++;
					if (tripleCounter % 2 == 0)
						runGC();
				}
			} catch (InterruptedException iex) {
				logger.debug("Thread " + Thread.currentThread().toString()
						+ " interrupted!");
				setTerminated(true);
				logger.debug("Clearing queue before stopping thread!");
				queue.clear();
			}

			// if(queue.isEmpty()){
			// logger.info("Pattern has been matched "+
			// sparkwaveNetwork.getReteNetwork().getNumMatches()+ " times.");
			// }

		}
	}

	public void runGC() {

		/************************************************
		 * CLEANING EPSILON NETWORK
		 ************************************************/

		for (Iterator<Triple> ptIter = sparkwaveNetwork.getEpsilonNetwork()
				.getProcessedTriples().iterator(); ptIter.hasNext();) {

			Triple processedTriple = ptIter.next();

			for (Token token : sparkwaveNetwork.getEpsilonNetwork()
					.getTokenNodesByStreamedTriple(processedTriple))
				token.removeTokenFromNode();

			// Remove the list of tokens for given streamed triple
			sparkwaveNetwork.getEpsilonNetwork().removeListByStreamedTriple(
					processedTriple);

			// Remove the streamed triple from the list
			ptIter.remove();
		}

	}

	public void handleTerminationEvent() {

	}

	public void setTerminated(boolean isTerminated) {
		this.isTerminated = isTerminated;
	}

	public boolean isTerminated() {
		return isTerminated;
	}

	public BlockingQueue<Triple> getQueue() {
		return queue;
	}

	public SparkwaveNetwork getSparkwaveNetwork() {
		return sparkwaveNetwork;
	}
}