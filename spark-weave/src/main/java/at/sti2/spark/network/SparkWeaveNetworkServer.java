package at.sti2.spark.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SparkWeaveNetworkServer extends Thread{

	private SparkWeaveNetwork sparkWeaveNetwork = null;
	
	public SparkWeaveNetworkServer(SparkWeaveNetwork sparkWeaveNetwork){
		this.sparkWeaveNetwork = sparkWeaveNetwork;
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
//	        System.out.println("Server: " + server);
//	        
//	        while (!Thread.interrupted()) {
//	            System.out.println("Waiting for connection...");
//	            Socket sock = server.accept();
//	            System.out.println("Connected: " + sock);
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
			//Open TCP/IP Server socket
			ServerSocket server = new ServerSocket(8080);
	        System.out.println("Server: " + server);
	        
	        while (!Thread.interrupted()) {
	            System.out.println("Waiting for connection...");
	            Socket sock = server.accept();
	            System.out.println("Connected: " + sock);
	            (new SparkWeaveNetworkServerThread(sparkWeaveNetwork, sock)).start();
	        }
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
