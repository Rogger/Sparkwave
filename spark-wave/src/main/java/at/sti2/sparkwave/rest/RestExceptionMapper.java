package at.sti2.sparkwave.rest;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * An exception mapper for jersey, maps
 * {@link at.sti2.sparkwave.rest.RestException} to
 * {@link javax.ws.rs.core.Response}
 * 
 * @author michaelrogger
 * 
 */
@Provider
public class RestExceptionMapper implements ExceptionMapper<RestException> {

	@Override
	public Response toResponse(RestException e) {
		JsonObject errResult = Json.createObjectBuilder()
				.add("error message", e.getMessage())
				.add("error code", e.getStatus().getStatusCode()).build();
		return Response.status(e.getStatus()).entity(errResult)
				.type(MediaType.APPLICATION_JSON).build();
	}

}
