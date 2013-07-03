package at.sti2.sparkwave.rest;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import at.sti2.sparkwave.SparkwaveKernel;
import at.sti2.sparkwave.rest.resources.SparkwaveKernelBinder;

public class RestServer {
	
	private static final Logger logger = Logger.getLogger(RestServer.class);
	
	private SparkwaveKernel sparkwaveKernel;
	
	public RestServer(SparkwaveKernel sparkwaveKernel) {
		this.sparkwaveKernel = sparkwaveKernel;
	}
	
	public void startServer(){
		logger.info("Starting REST management interface...");
		ResourceConfig rc = new ResourceConfig().
				register(new SparkwaveKernelBinder(sparkwaveKernel)).
				register(new RestExceptionMapper()).
				packages("at.sti2.sparkwave.rest.resources");
		
		HttpServer httpServer;
		try {
			httpServer = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
			logger.info(String.format("WADL available at %sapplication.wadl", BASE_URI));
			logger.info(String.format("Try out %spatterns", BASE_URI));
			System.in.read();
			httpServer.stop();
			
		} catch (RuntimeException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	public static final URI BASE_URI = getBaseURI();
	
	public static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/").port(9998).build();
	}

}