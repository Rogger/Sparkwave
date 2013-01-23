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
package at.sti2.spark.launcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.grammar.SparkPatternParser;
import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.spark.network.SparkwaveNetwork;
import at.sti2.spark.network.SparkwaveNetworkServer;
import at.sti2.spark.network.SparkwaveProcessorThread;

public class SparkwaveKernel{
	
	private static Logger logger = Logger.getLogger(SparkwaveKernel.class);
	private Pattern pattern = null;
	
	public static void main(String args[]){
		
		if (args.length < 2){
			System.err.println("Sparkwave expects the following parameters:");
			System.err.println(" <tcp/ip port>              - port on which network accepts incoming streams.");
			System.err.println(" (<pattern file>)?          - name of the file holding triple pattern definition.");
			System.exit(0);
		}
		
		//Test to see if the port number is correct
		int portNumber = 0;
		try{
			portNumber = Integer.parseInt(args[0]);
			if ((portNumber < 0) || (portNumber > 65535)){
				System.err.println("The port number value should be between 0 and 65535!");
				System.exit(0);
			}
		}catch(NumberFormatException nfex){
			System.err.println("The port value should be a number!");
			System.exit(0);
		}
		
		//Test to see if pattern file exists
		File patternFile = new File(args[1]);
		if (!patternFile.exists()){
			System.err.println("Triple pattern file doesn't exist!");
			System.exit(0);
		}
		
		new SparkwaveKernel().bootstrap(portNumber, new File[]{patternFile});
		
	}
	
	/**
	 * Kick off bootstrap
	 */
	private void bootstrap(int portNumber, File[] patternFiles){
		
		//Instantiate Queues
		List<BlockingQueue<Triple>> queues = new ArrayList<BlockingQueue<Triple>>();
		//Instantiate ExecutorService for SparkwaveProcessor
		ExecutorService sparkwaveProcessorExecutor = Executors.newFixedThreadPool(4);
		
		for(File patternFile : patternFiles){
			
			//Build triple pattern representation
			SparkPatternParser patternParser = new SparkPatternParser(patternFile);
			try {
				pattern = patternParser.parse();
			} catch (IOException e) {
				logger.error("Could not open pattern file "+patternFile);
			}
			
			//Every pattern gets its own queue
			BlockingQueue<Triple> queue = new ArrayBlockingQueue<Triple>(10);
			queues.add(queue);
			
			//Create SparkwaveNetwork
			SparkwaveNetwork sparkwaveNetwork = new SparkwaveNetwork(pattern);
			sparkwaveNetwork.init();
			
			//Create SparkwaveProcessorThread
			SparkwaveProcessorThread sparkwaveProcessor = new SparkwaveProcessorThread(sparkwaveNetwork, queue);
			sparkwaveProcessorExecutor.execute(sparkwaveProcessor);
			
		}
		
		//One Server for all SparkwaveNetworks
		new SparkwaveNetworkServer(portNumber, queues).start();
	}
}
