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
package at.sti2.spark.streamer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import org.apache.log4j.Logger;

import at.sti2.spark.streamer.file.FileStreamReader;

public class SparkStreamer {
	
	static Logger logger = Logger.getLogger(SparkStreamer.class);

	private String triplesFileName = null;
	
	private int port = 0;
	private Socket sock = null;
	
//	private AFUNIXSocket sock = null;
//	private String unixDomainSocketPath = null;
	
	
	/**
	 * UNIX Domain Socket constructor
	 * @param unixDomainSocketPath
	 * @param triplesFileName
	 */
//	public SparkStreamer(String unixDomainSocketPath, String triplesFileName) {
//		this.unixDomainSocketPath = unixDomainSocketPath;
//		this.triplesFileName = triplesFileName;
//		
//		//Connect to the socket
//		connect();
//		
//		//Stream file
//		stream();
//		
//	}
	
	/**
	 * TCP/IP constructor
	 * @param unixDomainSocketPath
	 * @param triplesFileName
	 */
	public SparkStreamer(String port, String triplesFileName) {
		this.port = Integer.parseInt(port);
		this.triplesFileName = triplesFileName;
		
		//Connect to the socket
		connect();
		
		//Stream file
		stream();
		
		System.exit(0);
	}
	
	private void connect(){
		
		try {
			sock = new Socket ("localhost", port);
		} catch (IOException e) {
			logger.debug("Cannot connect to server.");
            System.exit(1);
		}
		logger.info("Connected.");
	}
	
	private void stream(){
		
		long tripleCounter = 0;
		long timepoint = (new Date()).getTime();
		
		try {
			PrintWriter streamWriter = new PrintWriter(sock.getOutputStream());
		
			//Read file, create objects and stream them
			FileStreamReader streamReader = new FileStreamReader(triplesFileName);
			
			streamReader.openFile();
			
			String tripleLine = null;
			
			logger.info("Beginning of streaming.");
			
			Date startStreaming = new Date();
			
			while ((tripleLine = streamReader.nextLine()) != null){
				
				streamWriter.println(tripleLine);
				tripleCounter++;
				
//				if (tripleCounter%1000 == 0){
//					long currentTimepoint = (new Date()).getTime();
//					System.out.println("Processing " + (1000/(currentTimepoint - timepoint)) + " triples/sec.");
//					timepoint = currentTimepoint;
//					streamWriter.flush();
//				}
			}
			
			streamWriter.flush();
			
			Date endStreaming = new Date();
			
			logger.info("End of streaming.");
			logger.info("Streamed " + tripleCounter + " triples.");
			logger.info("Total streaming time " + (endStreaming.getTime() - startStreaming.getTime()) + " ms.");
			
			
			streamReader.closeFile();
			
			streamWriter.close();
	        sock.close();
	        
	        logger.info("Disconnected.");
	        
		} catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
		}
	}
	
	public static void main(String args[]){
		if (args.length != 2){
			System.out.println("SparkStreamer sends a stream of triples to a localhost at the designated port. The streamer expects to receive following arguments:");
			System.out.println(" <port> - the local port at which Sparkwave instance listens for upcoming triples.");
			System.out.println(" <triple_file_name> - name of the file in N-TRIPLES format holding triples to be streamed.");
			System.exit(0);
		}
		
		new SparkStreamer(args[0], args[1]);
	}
	
}
