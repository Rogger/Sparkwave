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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.grammar.SparkPatternParser;
import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.sparkwave.configuration.ConfigurationModel;
import at.sti2.sparkwave.configuration.SparkwaveConfigLoader;

public class SparkwaveKernel{
	
	private static Logger logger = Logger.getLogger(SparkwaveKernel.class);
	private static final String configFileName = "config.xml";
	
	public static void main(String args[]){
		
		if (args.length < 1){
			logger.info("Sparkwave expects the following parameters:");
			logger.info(" (<pattern file>)?	- path to one or more *.tpg pattern file(s)");
			System.exit(0);
		}
		
		logger.info("Initializing Sparkwave...");
		
		SparkwaveConfigLoader sparkConfigLoader = new SparkwaveConfigLoader();
		ConfigurationModel sparkwaveConfig = null;
		try {
			logger.info("Reading Sparkwave configuration from "+configFileName+"...");
			sparkwaveConfig = sparkConfigLoader.load(configFileName);
			logger.info("Loaded configuration: port="+sparkwaveConfig.getPort()+", Number of PreProcessing Plugins="+ sparkwaveConfig.getPPPluginsConfig().size());
			//TODO check schema
		} catch (ConfigurationException e) {
			logger.error("Could not load config file, please check existance of "+configFileName);
			System.exit(0);
		}
		
		//Test to see if the port number is correct
//		int portNumber = configLoader.getPort();
//		if ((portNumber < 0) || (portNumber > 65535)){
//			logger.error("The port number value should be between 0 and 65535!");
//			System.exit(0);
//		}
		
		ArrayList<File> patternFiles = new ArrayList<File>();
		for(int i = 0; i < args.length; i++){
			File patternFile = new File(args[i]);
			if (!patternFile.exists()){
				logger.error("Cannot load pattern file "+patternFile+" !");
				System.exit(0);
			}else{
				patternFiles.add(patternFile);				
			}
		}
		
		new SparkwaveKernel().bootstrap(sparkwaveConfig, patternFiles);
	}
	
	/**
	 * Kick off bootstrap
	 */
	private void bootstrap(ConfigurationModel sparkwaveConfig, List<File> patternFiles){
		
		//Instantiate Queues
		List<BlockingQueue<Triple>> queues = new ArrayList<BlockingQueue<Triple>>();
		
		//Instantiate ExecutorService for SparkwaveProcessor
		ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("Processor-%d").build();
		ExecutorService processorExecutor = Executors.newCachedThreadPool(tf);
		
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
				ProcessorThread sparkwaveProcessor = new ProcessorThread(sparkwaveNetwork, queue);
				processorExecutor.execute(sparkwaveProcessor);
				
			}
			
		}
		
		ThreadFactory threadFactoyServerSocket = new ThreadFactoryBuilder().setNameFormat("ServerSocket-%d").build();
		ExecutorService serverSocketExecutor = Executors.newSingleThreadExecutor(threadFactoyServerSocket);
		
		//One Server for all SparkwaveNetworks, acts as multiplexer
		serverSocketExecutor.execute(new ServerSocketThread(sparkwaveConfig, queues));
	}
}
