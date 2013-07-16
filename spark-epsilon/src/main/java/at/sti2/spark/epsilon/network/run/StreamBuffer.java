/*
 * Copyright (c) 2011, University of Innsbruck, Austria.
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

package at.sti2.spark.epsilon.network.run;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.spark.core.stream.Triple;

/**
 * StreamBuffer holds the received triples to be picked up by the WorkerThreads and processed through the network
 * 
 * @author skomazec
 *
 */
public class StreamBuffer {
	
	static Logger logger = LoggerFactory.getLogger(StreamBuffer.class);

	private LinkedBlockingQueue<Triple> streamTripleQueue = null;
	
	public StreamBuffer(){
		streamTripleQueue = new LinkedBlockingQueue<Triple> ();
		logger.info("StreamBuffer instance created.");
	}
	
	public Triple take(){
		
		Triple triple = null;
		try {
			triple = streamTripleQueue.take();
		} catch (InterruptedException e) {
			logger.error("Interrupted while taking a stream triple from the queue.", e);
		}
		return triple;
	}
	
	public void put(Triple streamedTriple){
		try {
			streamTripleQueue.put(streamedTriple);
		} catch (InterruptedException e) {
			logger.error("Interrupted while putting a stream triple to the queue.", e);
		}
	}
}
