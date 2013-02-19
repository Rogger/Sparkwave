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
	
	public static void main(String args[]){
		
		if (args.length < 2){
			logger.info("Sparkwave expects the following parameters:");
			logger.info(" <tcp/ip port>              - port to listen for incoming data streams, typically 8080");
			logger.info(" (<pattern file>)?          - path to *.tpg pattern file(s)");
			System.exit(0);
		}
		
		//Test to see if the port number is correct
		int portNumber = 0;
		try{
			portNumber = Integer.parseInt(args[0]);
			if ((portNumber < 0) || (portNumber > 65535)){
				logger.error("The port number value should be between 0 and 65535!");
				System.exit(0);
			}
		}catch(NumberFormatException nfex){
			logger.error("The port value should be a number!");
			System.exit(0);
		}
		
		ArrayList<File> patternFiles = new ArrayList<File>();
		for(int i = 1; i < args.length; i++){
			File patternFile = new File(args[i]);
			if (!patternFile.exists()){
				logger.error("Cannot load pattern file "+patternFile+" !");
				System.exit(0);
			}else{
				patternFiles.add(patternFile);				
			}
		}
		
		new SparkwaveKernel().bootstrap(portNumber, patternFiles);
	}
	
	/**
	 * Kick off bootstrap
	 */
	private void bootstrap(int portNumber, List<File> patternFiles){
		
		logger.info("Staring Sparkwave...");
		
		//Instantiate Queues
		List<BlockingQueue<Triple>> queues = new ArrayList<BlockingQueue<Triple>>();
		//Instantiate ExecutorService for SparkwaveProcessor
		ExecutorService sparkwaveProcessorExecutor = Executors.newFixedThreadPool(4);
		
		int patternFilesSize = patternFiles.size();
		for(int i = 0; i < patternFilesSize; i++){
			
			File patternFile = patternFiles.get(i);
			
			//Build triple pattern representation
			logger.info("Parsing pattern file ("+(i+1)+" of "+patternFilesSize+"): "+patternFile+"...");
			SparkPatternParser patternParser = new SparkPatternParser(patternFile);
			Pattern pattern = null;
			try {
				pattern = patternParser.parse();
			} catch (IOException e) {
				logger.error("Could not open pattern file "+patternFile);
			}
			logger.info("Parsed pattern:\n"+pattern);
			
			if(pattern!=null){
				
				//Create SparkwaveNetwork
				SparkwaveNetwork sparkwaveNetwork = new SparkwaveNetwork(pattern);
				sparkwaveNetwork.init();
				
				//Every pattern gets its own queue
				BlockingQueue<Triple> queue = new ArrayBlockingQueue<Triple>(10);
				queues.add(queue);
				
				//Create SparkwaveProcessorThread
				SparkwaveProcessorThread sparkwaveProcessor = new SparkwaveProcessorThread(sparkwaveNetwork, queue);
				sparkwaveProcessorExecutor.execute(sparkwaveProcessor);
				
			}
			
		}
		
		//One Server for all SparkwaveNetworks
		new SparkwaveNetworkServer(portNumber, queues).start();
	}
}
