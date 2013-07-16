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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkStreamer {
	
	static Logger logger = LoggerFactory.getLogger(SparkStreamer.class);

	private String triplesFileName = null;
	
	private String host = "localhost";
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
		
		//Stream file
		File fileToStream = new File(triplesFileName);
		
		// Load all files from directory
		if(fileToStream.isDirectory()){
			Collection<File> listFiles = FileUtils.listFiles(
					fileToStream,
					new RegexFileFilter("^(.*?)"), 
					DirectoryFileFilter.DIRECTORY
			);
			for(File file : listFiles){
				stream(file);
			}
		}else{
			stream(fileToStream);			
		}
	}
	
	private void stream(File fileToStream){
		
		PrintWriter streamWriter = null;
		LineIterator lineIterator = null;

		long Counter = 0;
		
		try {
			sock = new Socket ("localhost", port);			
		} catch (IOException e) {
			logger.debug("Cannot connect to server.");
            System.exit(1);
		}
		logger.info("Connected.");
		
		
		try {
			streamWriter = new PrintWriter(sock.getOutputStream());
			lineIterator = FileUtils.lineIterator(fileToStream, "UTF-8");
			
			logger.info("Beginning to stream.");
			Date startStreaming = new Date();
			String line = null;
			while (lineIterator.hasNext()){
				
				line = lineIterator.nextLine();
				streamWriter.println(line);
				Counter++;
				
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
			logger.info("Streamed " + Counter + " triples/lines.");
			logger.info("Total streaming time " + (endStreaming.getTime() - startStreaming.getTime()) + " ms.");
	        
	        
		} catch (IOException e) {
            logger.error(e.getMessage());
		} finally {
			IOUtils.closeQuietly(streamWriter);
			lineIterator.close();
			logger.info("Disconnected.");
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
