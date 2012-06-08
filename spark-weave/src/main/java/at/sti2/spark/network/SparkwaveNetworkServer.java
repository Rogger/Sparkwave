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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import at.sti2.spark.core.stream.Triple;

public class SparkwaveNetworkServer extends Thread{
	
	static Logger logger = Logger.getLogger(SparkwaveNetworkServer.class);

	private SparkwaveNetwork sparkwaveNetwork = null;
	private int serverPort = 0;
	
	private BlockingQueue<Triple> queue = new ArrayBlockingQueue<Triple>(10);
	
	public SparkwaveNetworkServer(SparkwaveNetwork sparkwaveNetwork, int serverPort){
		this.sparkwaveNetwork = sparkwaveNetwork;
		this.serverPort = serverPort;
	}
	
	ExecutorService sparkWeaveExecutor = Executors.newSingleThreadExecutor(); 
	ExecutorService sparkwaveProcessorExecutor = Executors.newSingleThreadExecutor(); 
	
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
	 * TCP/IP Sparkweave Network Server
	 */
	public void run(){

		try {
			//Instantiate Sparkwave processor over the queue
			SparkwaveProcessorThread sparkwaveProcessor = new SparkwaveProcessorThread(sparkwaveNetwork, queue);
			sparkwaveProcessorExecutor.execute(sparkwaveProcessor);
			logger.info("Sparkwave processor waiting for incoming triples...");
			
			//Open TCP/IP Server socket
			ServerSocket server = new ServerSocket(serverPort);
	        logger.info("Server: " + server);
	        
	        while (!Thread.interrupted()) {
	        	logger.info("Waiting for connection...");
	            Socket sock = server.accept();
	            logger.info("Connected: " + sock);
	            
	            BufferedReader streamReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	            
	            SparkwaveStreamThread sparkThread = new SparkwaveStreamThread(streamReader, queue, sparkwaveNetwork);
	            
	            sparkWeaveExecutor.execute(sparkThread);	            
	        }
	        
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}
	}
}
