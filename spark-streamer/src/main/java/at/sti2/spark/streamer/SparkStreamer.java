package at.sti2.spark.streamer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import at.sti2.spark.streamer.file.NTripleStreamReader;

public class SparkStreamer {

	private String unixDomainSocketPath = null;
	private String triplesFileName = null;
	private AFUNIXSocket sock = null;
	
	public SparkStreamer(String unixDomainSocketPath, String triplesFileName) {
		this.unixDomainSocketPath = unixDomainSocketPath;
		this.triplesFileName = triplesFileName;
		
		//Connect to the socket
		connect();
		
		//Stream file
		stream();
		
	}
	
	private void connect(){
		File socketFile = new File(unixDomainSocketPath);
		try {
			sock = AFUNIXSocket.newInstance();
			sock.connect(new AFUNIXSocketAddress(socketFile));
		} catch (IOException e) {
			System.out.println("Cannot connect to server.");
            System.out.flush();
            System.exit(1);
		}
		System.out.println("Connected.");
	}
	
	private void stream(){
		
		long tripleCounter = 0;
		long timepoint = (new Date()).getTime();
		
		try {
			PrintWriter streamWriter = new PrintWriter(sock.getOutputStream());
		
			//Read file, create objects and stream them
			NTripleStreamReader streamReader = new NTripleStreamReader(triplesFileName);
			
			streamReader.openFile();
			
			String tripleLine = null;
			
			System.out.println("Beginning of streaming.");
			
			Date startStreaming = new Date();
			
			while ((tripleLine = streamReader.nextTripleLine()) != null){
				
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
			
			System.out.println("End of streaming.");
			System.out.println("Streamed " + tripleCounter + " triples.");
			System.out.println("Total streaming time " + (endStreaming.getTime() - startStreaming.getTime()) + " ms.");
			
			
			streamReader.closeFile();
			
			streamWriter.close();
	        sock.close();
	        
	        System.out.println("Disconnected.");
	        
		} catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
		}
	}
	
	public static void main(String args[]){
		if (args.length != 2){
			System.out.println("SparkStreamer enables streaming of triples. The streamer expects to receive two arguments:");
			System.out.println(" <unix_domain_socket_path> - name of the socket at which SparkWeave network expects triples.");
			System.out.println(" <triples_file_name> - name of the file holding triples which are going to be streamed.");
			System.exit(0);
		}
		
		new SparkStreamer(args[0], args[1]);
	}
	
}
