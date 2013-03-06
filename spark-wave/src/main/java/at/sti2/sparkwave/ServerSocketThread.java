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
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.preprocess.RDFFormatTransformer;
import at.sti2.spark.preprocess.XSLTransformer;
import at.sti2.sparkwave.parser.StreamParserThread;

public class ServerSocketThread implements Runnable{
	
	static Logger logger = Logger.getLogger(ServerSocketThread.class);

	private ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("PreProcess-%d").build();
	private ExecutorService sparkwaveParserExecutor = Executors.newCachedThreadPool(tf); 
	private List<BlockingQueue<Triple>> queues = null;
	private int serverPort = 0;
	
	public ServerSocketThread(int serverPort, List<BlockingQueue<Triple>> queues){
		this.queues = queues;
		this.serverPort = serverPort;
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
			ServerSocket server = new ServerSocket(serverPort);
	        logger.info("Server: " + server);
	        
	        while (!Thread.interrupted()) {
	        	logger.info("Waiting for connection...");
	            Socket sock = server.accept();
	            logger.info("Connected: " + sock);
	            
	            InputStream socketStreamIn = sock.getInputStream();
	            
	            // Wiring: socketStreamIn --> (Plugin1) --> PipeOut1 --> PipeIn1
	            final PipedOutputStream pipeOut1 = new PipedOutputStream();
	            final PipedInputStream pipeIn1 = new PipedInputStream(pipeOut1);
	           
	            // Wiring: PipeIn1 --> (Plugin2) --> PipeOut2 --> PipeIn2
	            final PipedOutputStream pipeOut2 = new PipedOutputStream();
	            final PipedInputStream pipeIn2 = new PipedInputStream(pipeOut2);
	            
	            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            
	            // XML -> XMLRDF
	            Source xslt = new StreamSource(new File("target/test-classes/support/fromEventToRDF.xslt"));
	            XSLTransformer xsltTransformer = new XSLTransformer(xslt, socketStreamIn, pipeOut1);
	            
	            // XMLRDF -> N3
	            RDFFormatTransformer xmlrdfToNT = new RDFFormatTransformer(pipeIn1, baos,"RDF/XML-ABBREV","N-TRIPLE");

//	            StreamParserThread sparkStreamParserThread = new StreamParserThread(pipeIn2, queues);
//	            StreamParserThread sparkStreamParserThread = new StreamParserThread(socketStreamIn, queues);

	            // kick-off pre-process
	            sparkwaveParserExecutor.execute(xsltTransformer);
	            sparkwaveParserExecutor.execute(xmlrdfToNT);
	            // kick-off processor
//	            sparkwaveParserExecutor.execute(sparkStreamParserThread);
	        }
	        
		} catch (IOException e) {
			logger.error(e);
		} finally {
			
		}
	}
}
