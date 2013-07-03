package at.sti2.sparkwave.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class RestException extends WebApplicationException {
	
	private static final long serialVersionUID = 1L;
	
	private final Response.Status status;
	
	public RestException(final String message, final Response.Status status) {
		super(message,status);
		this.status = status;
	}

	public Response.Status getStatus() {
		return status;
	}
	
}
