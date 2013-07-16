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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.preprocess.PreProcess;
import at.sti2.sparkwave.configuration.ConfigurationModel;
import at.sti2.sparkwave.configuration.PPPluginConfig;
import at.sti2.sparkwave.parser.StreamParserThread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Accepts new connections on socket. Forwards results to pre-processing and parser threads.
 * @author michaelrogger
 * @author srdkom
 *
 */
public class ServerSocketThread implements Runnable{
	
	static Logger logger = LoggerFactory.getLogger(ServerSocketThread.class);

	private ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("PreProcess-%d").build();
	private ExecutorService sparkwaveParserExecutor = Executors.newCachedThreadPool(tf); 
	private List<BlockingQueue<Triple>> queues = null;
	ConfigurationModel configuration = null;
	
	public ServerSocketThread(ConfigurationModel configuration, List<BlockingQueue<Triple>> queues){
		this.queues = queues;
		this.configuration = configuration;
	}
	
	
	/**
	 * UNIX Domain Socket Server
	 */
//	public void run(){
//		
//		//Open unix domain socket 
//		final File socketFile = new File(new File(System.getProperty("java.io.tmpdir")), "sparkweave.sock");
//		
//		AFUNIXServerSocket server;
//		
//		try {
//			server = AFUNIXServerSocket.newInstance();
//	        server.bind(new AFUNIXSocketAddress(socketFile));
//	        logger.info("Server: " + server);
//	        
//	        while (!Thread.interrupted()) {
//	            logger.info("Waiting for connection...");
//	            Socket sock = server.accept();
//	            logger.info("Connected: " + sock);
//	            (new SparkWeaveNetworkServerThread(sparkWeaveNetwork, sock)).start();
//	        }
//	        
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
	
	/**
	 * TCP/IP Sparkwave Network Server
	 */
	public void run(){

		try {
			
			//Open TCP/IP Server socket
			ServerSocket server = new ServerSocket(configuration.getPort());
	        logger.info("Server: " + server);
	        
	        while (!Thread.interrupted()) {
	        	logger.info("Waiting for connection...");
	            Socket sock = server.accept();
	            logger.info("Connected: " + sock);
	            
	            //TODO Not every connection should cause a rebuild of the plugin chain. Should work with arbitrary many connections and failure resistent. re-use plugin threads and parser threads.
	            
	            InputStream socketStreamIn = sock.getInputStream();
	            
	            // PreProcessing Plugins to be loaded
	            if(configuration.getPPPluginsConfig().size() == 2){
	            	
	            	//TODO support arbitrary many plugins
	            	
	            	// Wiring: socketStreamIn --> (Plugin1) --> PipeOut1 --> PipeIn1
	            	final PipedOutputStream pipeOut1 = new PipedOutputStream();
	            	final PipedInputStream pipeIn1 = new PipedInputStream(pipeOut1);
	            	
	            	// Wiring: PipeIn1 --> (Plugin2) --> PipeOut2 --> PipeIn2
	            	final PipedOutputStream pipeOut2 = new PipedOutputStream();
	            	final PipedInputStream pipeIn2 = new PipedInputStream(pipeOut2);
	            	
	            	final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            	
	            	// plugin configuration
	            	PPPluginConfig pluginConfig1 = configuration.getPPPluginsConfig().get(0);
	            	PreProcess plugin1 = instantiateAndConfigurePlugin(pluginConfig1, socketStreamIn, pipeOut1);
	            	
	            	PPPluginConfig pluginConfig2 = configuration.getPPPluginsConfig().get(1);
	            	PreProcess plugin2 = instantiateAndConfigurePlugin(pluginConfig2, pipeIn1, pipeOut2);

	            	// N3 Parser
	            	StreamParserThread sparkStreamParserThread = new StreamParserThread(pipeIn2, queues);
	            	
	            	// kick-off pre-process
	            	sparkwaveParserExecutor.execute(plugin1);
	            	sparkwaveParserExecutor.execute(plugin2);
	            	
	            	// kick-off parser
	            	sparkwaveParserExecutor.execute(sparkStreamParserThread);

	            }else{
	            	
	            	StreamParserThread sparkStreamParserThread = new StreamParserThread(socketStreamIn, queues);
	            	
	            	// kick-off parser
	            	sparkwaveParserExecutor.execute(sparkStreamParserThread);
	            
	            }
	            
	        }
	        
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			
		}
	}
	
	private PreProcess instantiateAndConfigurePlugin(PPPluginConfig pluginConfig, InputStream in, OutputStream out) throws ClassNotFoundException, InstantiationException, IllegalAccessException{
    	
		PreProcess plugin = null;
		
		String className = pluginConfig.getClassName();
    			
    	Class<?> clazz = Class.forName(className);
    	// implements the interface
    	if(PreProcess.class.isAssignableFrom(clazz)){

    		// instantiate class
    		plugin = (PreProcess)clazz.newInstance();	  
    		
    		// initialize plugin
    		plugin.init(in, out);
    		
    		// Set all properties from xml config
    		Map<String, String> properties = pluginConfig.getProperties();
    		for(String key : properties.keySet()){
    			plugin.setProperty(key, properties.get(key));
    		}
    	}
    	
    	return plugin;
	}
}
