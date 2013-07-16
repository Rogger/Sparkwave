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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.grammar.SparkParserException;
import at.sti2.spark.grammar.SparkParserResult;
import at.sti2.spark.grammar.SparkPatternParser;
import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.sparkwave.cla.CommandLineArguments;
import at.sti2.sparkwave.configuration.ConfigurationModel;
import at.sti2.sparkwave.configuration.SparkwaveConfigLoader;
import at.sti2.sparkwave.rest.RestServer;

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
	
	private static final Logger logger = LoggerFactory.getLogger(SparkwaveKernel.class);
	private String configFileName = "config.xml";
	
	// Keeping track of queues, networks, processor threads
	private List<BlockingQueue<Triple>> queues = new CopyOnWriteArrayList<BlockingQueue<Triple>>();
	
	// no direct instantiation
	private SparkwaveKernel(){}
	
	private static SparkwaveKernel sparkwaveKernel;
	
	/**
	 * Get instance
	 */
	public static SparkwaveKernel getInstance(){
		if(sparkwaveKernel == null){
			sparkwaveKernel = new SparkwaveKernel();
		}
		return sparkwaveKernel;
	}
	
	public static void main(String args[]){
		getInstance().init(args);
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
			logger.info("Reading Sparkwave configuration from {} ...", configFileName);
			sparkwaveConfig = sparkConfigLoader.load(configFileName);
			logger.info("Loaded configuration: port={}, Number of pre-processing plugins={}", sparkwaveConfig.getPort(), sparkwaveConfig.getPPPluginsConfig().size());
		} catch (ConfigurationException e) {
			logger.error("Could not load config file, please check existance of {}",configFileName);
			System.exit(1);
		}
		
		ArrayList<File> patternFiles = new ArrayList<File>();
		for(String patternPath : commandLineArguments.getPatterns()){
			File patternFile = new File(patternPath);
			if (!patternFile.exists()){
				logger.error("Cannot load pattern file {} !", patternFile);
				System.exit(1);
			}else{
				patternFiles.add(patternFile);				
			}
		}
		
		// kick off bootstrap
		bootstrap(sparkwaveConfig, patternFiles);
		
		new RestServer(this).startServer();
	}
	
	/**
	 * Kick off bootstrap
	 */
	private void bootstrap(ConfigurationModel sparkwaveConfig, List<File> patternFiles){
		
		//Instantiate ExecutorService for SparkwaveProcessor
//		ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("Processor-%d").build();
//		ExecutorService processorExecutor = Executors.newCachedThreadPool(tf);

		
		int patternFilesSize = patternFiles.size();
		for(int i = 0; i < patternFilesSize; i++){
			
			File patternFile = patternFiles.get(i);
			
			//Build triple pattern representation
			logger.info("Parsing pattern file ("+(i+1)+" of "+patternFilesSize+"): "+patternFile+"...");
			SparkPatternParser patternParser = new SparkPatternParser();
			SparkParserResult parserResult = null;
			try {
				parserResult = patternParser.parse(patternFile);
			} catch (IOException e) {
				logger.error("Could not open pattern file "+patternFile+" "+e.getMessage());
			} catch (SparkParserException e) {
				logger.error("Could not parse the file "+patternFile+" "+e.getMessage());
			}
			Pattern pattern = parserResult.getPattern();
			
			logger.info("Parsed pattern:\n"+pattern);
			
			if(pattern!=null){
				
				addProcessorThread(pattern);
				
//				processorExecutor.
//				processorExecutor.shutdownNow();
				
				//TODO √generate pattern id (random? hash? int++?) for pattern, use name
				//TODO √Associate: processor and thread, method to find corresponding sparkwave processor for name. HashMap<Name,<List<Processor,Thread>?
				//TODO √1) remove queue for pattern 2) shutdown processorThread
				
				//TODO √Add GrizzlyServer and access SparkwaveKernel.
				//TODO getPatterns method
				//TODO Test to remove,add patterns!!
				
				/* pattern class
				 * 
				 * variables:
				 * filename
				 * 
				 * method:
				 * getContent()
				 * 	
				*/
				
				/* this class
					
					variables:
					map<pattern,thread>
					list of patterns
					
					methods:
				 * 	addProcessorThread(Pattern)
				 * 	removeProcessorThread(Pattern)
				 * 	getLoadedPatterns()
				 * 	getLoadedPattern(name)
				 * 
				 */
			}
		}
		
//		for(Thread t : threads){
//			try {
//				Thread.sleep(3000);
//				logger.info("interrupting "+t);
//				t.interrupt();
//				t.join();
//				logger.info("joined "+t);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		ThreadFactory threadFactoyServerSocket = new ThreadFactoryBuilder().setNameFormat("ServerSocket-%d").build();
		ExecutorService serverSocketExecutor = Executors.newSingleThreadExecutor(threadFactoyServerSocket);
		
		//One Server for all SparkwaveNetworks, acts as multiplexer
		serverSocketExecutor.execute(new ServerSocketThread(sparkwaveConfig, queues));
	}
	
	
	Map<Pattern,Thread> patternThreadMap = new HashMap<Pattern,Thread>();
	HashMap<Pattern,Queue<Triple>> patternQueueMap = new HashMap<Pattern,Queue<Triple>>();
	Map<Long, Pattern> idPatternMap = new HashMap<Long, Pattern>();
	
	
	/**
	 * Starts all relevant threads for a pattern and keeps track of them
	 * @param pattern
	 */
	public void addProcessorThread(Pattern pattern){
		
		//Create SparkwaveNetwork
		SparkwaveNetwork sparkwaveNetwork = new SparkwaveNetwork(pattern);
		sparkwaveNetwork.init();
		
		//Every pattern gets its own queue
		BlockingQueue<Triple> queue = new ArrayBlockingQueue<Triple>(10);
		queues.add(queue);
		
		//Create SparkwaveProcessorThread
		ProcessorThread sparkwaveProcessor = new ProcessorThread(sparkwaveNetwork, queue);
		Thread thread = new Thread(sparkwaveProcessor);
		thread.setName("Processor-"+thread.getName());
		thread.start();
		
		patternThreadMap.put(pattern, thread);
		patternQueueMap.put(pattern, queue);
		idPatternMap.put(pattern.getId(), pattern);
		
	}
	
	/**
	 * Terminates all threads corresponding to a pattern
	 * @param patternId of the pattern
	 */
	public boolean removeProcessorThread(long patternId){
		
		Pattern pattern = idPatternMap.remove(patternId);
		if(pattern == null) return false;
		
		Thread thread = patternThreadMap.remove(pattern);
		if(thread == null) return false;
		
		logger.debug("Interrupting {}", thread);
		thread.interrupt();
		
		Queue<Triple> queue = patternQueueMap.remove(pattern);
		if(queue != null){
			logger.debug("Removing queue {} from queues", queue);
			//synchronized because it might happen that StreamParserThread is iterating over queues
			synchronized(queues){
				queues.remove(queue);
			}
		}
		
		return true;
	}
	
	/**
	 * Returns a collection of all loaded patterns
	 * @return collection of patterns
	 */
	public @Nonnull Collection<Pattern> getLoadedPatterns(){
		return idPatternMap.values();
	}
	
	/**
	 * Get loaded pattern
	 * @param id of the pattern
	 * @return the pattern or NULL
	 */
	public @Nullable Pattern getLoadedPattern(long id){
		return idPatternMap.get(id);
	}
	
}