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
