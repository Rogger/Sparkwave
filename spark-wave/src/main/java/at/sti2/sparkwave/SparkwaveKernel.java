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

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.grammar.SparkPatternParser;
import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.sparkwave.cla.CommandLineArguments;
import at.sti2.sparkwave.configuration.ConfigurationModel;
import at.sti2.sparkwave.configuration.SparkwaveConfigLoader;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Serves as entrypoint for Sparkwave.
 * @author michaelrogger
 * @author srdkom
 *
 */
public class SparkwaveKernel{
	
	private static Logger logger = Logger.getLogger(SparkwaveKernel.class);
	private String configFileName = "config.xml";
	
	public static void main(String args[]){
		new SparkwaveKernel().init(args);
	}
	
	private void init(String args[]){
		
		
		CommandLineArguments commandLineArguments = new CommandLineArguments();
		JCommander jc = null;
		try{
			jc = new JCommander(commandLineArguments, args);			
		}catch(ParameterException pe){
			System.err.println(pe.getMessage());
			System.err.println("Use -h for help");
			System.exit(0);
		}
		
		// help
		if(commandLineArguments == null || commandLineArguments.isHelp()){
			jc.usage();
			System.exit(0);
		}
		
		// version
		if(commandLineArguments.isVersion()){
			Package classPackage = this.getClass().getPackage();
			String implementationVersion = classPackage.getImplementationVersion();
			System.out.println("version: "+implementationVersion);
			System.exit(0);
		}
		
		// config
		configFileName = commandLineArguments.getConfig();
		
		if(commandLineArguments.getPatterns().size() ==0){
			System.err.println("Use -p to specifiy at least one pattern!");
			jc.usage();
			System.exit(0);
		}
		
		
		logger.info("Initializing Sparkwave...");
		
		SparkwaveConfigLoader sparkConfigLoader = new SparkwaveConfigLoader();
		ConfigurationModel sparkwaveConfig = null;
		try {
			logger.info("Reading Sparkwave configuration from "+configFileName+"...");
			sparkwaveConfig = sparkConfigLoader.load(configFileName);
			logger.info("Loaded configuration: port="+sparkwaveConfig.getPort()+", Number of pre-processing plugins="+ sparkwaveConfig.getPPPluginsConfig().size());
		} catch (ConfigurationException e) {
			logger.error("Could not load config file, please check existance of "+configFileName);
			System.exit(1);
		}
		
		ArrayList<File> patternFiles = new ArrayList<File>();
		for(String patternPath : commandLineArguments.getPatterns()){
			File patternFile = new File(patternPath);
			if (!patternFile.exists()){
				logger.error("Cannot load pattern file "+patternFile+" !");
				System.exit(1);
			}else{
				patternFiles.add(patternFile);				
			}
		}
		
		// kick off bootstrap
		bootstrap(sparkwaveConfig, patternFiles);

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
