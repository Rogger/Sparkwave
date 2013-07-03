package at.sti2.sparkwave.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/ping")
public class PingResource {

	@GET
	@Produces("text/plain")
	public String getPingMessage(){
		return "pong";
	}
}
