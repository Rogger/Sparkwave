package at.sti2.sparkwave.rest.resources;

import java.net.URI;
import java.util.Collection;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.antlr.runtime.ANTLRStringStream;
import org.apache.log4j.Logger;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpContainer;

import at.sti2.spark.grammar.SparkParserResult;
import at.sti2.spark.grammar.SparkPatternParser;
import at.sti2.spark.grammar.pattern.Pattern;
import at.sti2.sparkwave.SparkwaveKernel;
import at.sti2.sparkwave.rest.RestException;
import at.sti2.sparkwave.rest.RestServer;

import com.hp.hpl.jena.sparql.lib.org.json.JSONException;

@Path("/patterns")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PatternsResource {
	
	private static final Logger logger = Logger.getLogger(PatternsResource.class);
	
	@Context
	private SparkwaveKernel sparkwaveKernel;
	
	@Context
	GrizzlyHttpContainer container;
	
	/**
	 * Return all patterns available in directory
	 * @return
	 */
	@GET
	public JsonArray getPatterns(){
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

		Collection<Pattern> patterns = sparkwaveKernel.getLoadedPatterns();
		for(Pattern pattern : patterns){
			JsonObject jsonObject = null;
			try {
				jsonObject = toJSONObject(pattern);
			} catch (JSONException e) {
				throw new RestException("Could not parse pattern: "+e.getMessage(), Response.Status.BAD_REQUEST);
			}
			if(jsonObject!=null)
				arrayBuilder.add(jsonObject);
		}
		
		JsonArray jsonArray = arrayBuilder.build();
		
		return jsonArray;
	}
	
	/**
	 * Add new pattern
	 * @param jsonPattern
	 * @return
	 */
	@POST
	public Response postPattern(JsonObject jsonPattern){
		logger.info("Processing POST request");
		if(!jsonPattern.containsKey("content")){
			throw new RestException("Missing content", Response.Status.BAD_REQUEST);
		}
		String patternContent = jsonPattern.getString("content");
		
		logger.debug(jsonPattern);
		
		//parse pattern
		SparkPatternParser parser = new SparkPatternParser();
		ANTLRStringStream reader = new ANTLRStringStream(patternContent);
		SparkParserResult parserResult = null;
		try {
			parserResult = parser.parse(reader);
		} catch (Exception e) {
			logger.error("Parser Exception: "+e.getMessage());
			throw new RestException("Could not parse pattern: "+e.getMessage(), Response.Status.BAD_REQUEST);
		}
		Pattern pattern = parserResult.getPattern();
		
		//load pattern in sparkwave
		if(pattern!=null){
			sparkwaveKernel.addProcessorThread(pattern);
		}
		
		JsonObject parsedJsonPattern;
		try {
			parsedJsonPattern = toJSONObject(pattern);
		} catch (JSONException e) {
			logger.error("Could not serialize Pattern to Json: "+e.getMessage());
			throw new RestException("Could not parse pattern, "+e.getMessage(), Response.Status.BAD_REQUEST);
		}
		
		long patternId = pattern.getId();
		URI baseURI = RestServer.getBaseURI();
		URI patternURI = UriBuilder.fromUri(baseURI).path("patterns/"+patternId).build();
		
		Response response = Response.created(patternURI).entity(parsedJsonPattern).build();
		return response;
	}

	/**
	 * Get pattern
	 * @param patternId id of the pattern
	 * @return
	 * @throws JSONException
	 */
	@GET @Path("/{patternid}")
	public JsonObject getPattern(@PathParam("patternid") long patternId) throws JSONException{
		logger.info("Processing GET request for patternId " +patternId);
		Pattern pattern = sparkwaveKernel.getLoadedPattern(patternId);
		if(pattern==null){
			logger.error("Pattern "+patternId+" not found");
			throw new RestException("Could not find pattern "+patternId,Response.Status.NOT_FOUND);
		}
		
		JsonObject jsonPattern = toJSONObject(pattern);
		return jsonPattern;
	}
	
	/**
	 * Delete pattern
	 * @return
	 */
	@DELETE @Path("/{patternid}")
	public Response deletePattern(@PathParam("patternid") long patternId){
		logger.info("Processing DELETE request for patternId " +patternId);
		boolean isRemoved = sparkwaveKernel.removeProcessorThread(patternId);
		if(isRemoved){
			return Response.ok().build();
		}else{
			throw new RestException("Could not find pattern "+patternId,Response.Status.NOT_FOUND);
		}
	}
	
	private JsonObject toJSONObject(Pattern pattern) throws JSONException{
		JsonObject jsonObject = Json.createObjectBuilder()
			.add("id", pattern.getId())
			.add("content", pattern.formatString().toString())
			.build();
		
		return jsonObject;
	}
}
