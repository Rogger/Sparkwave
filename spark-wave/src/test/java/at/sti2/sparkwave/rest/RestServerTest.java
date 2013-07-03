package at.sti2.sparkwave.rest;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import at.sti2.sparkwave.SparkwaveKernel;
import at.sti2.sparkwave.rest.resources.PatternsResource;
import at.sti2.sparkwave.rest.resources.SparkwaveKernelBinder;

public class RestServerTest extends JerseyTest {

	@Override
	protected Application configure() {
		SparkwaveKernel sparkwaveKernel = SparkwaveKernel.getInstance();
		return new ResourceConfig(PatternsResource.class)
				.register(new SparkwaveKernelBinder(sparkwaveKernel));
	}

	@Test
	public void testPostGetDelete() throws IOException {

		// POST new pattern
		String patternContent = FileUtils.readFileToString(new File(
				"target/test-classes/support/support_pattern.tpg"));
		JsonObject pattern = Json.createObjectBuilder()
				.add("content", patternContent).build();

		Response postResponse = target("patterns").request(
				MediaType.APPLICATION_JSON).post(Entity.json(pattern));
		assertNotNull(postResponse);

		StatusType statusType = postResponse.getStatusInfo();
		assertEquals(statusType.getStatusCode(),
				Response.Status.CREATED.getStatusCode());

		URI patternURI = postResponse.getLocation();
		assertNotNull(patternURI);

		// GET pattern
		Response getResponse = target(patternURI.getPath()).request(
				MediaType.APPLICATION_JSON).get();
		assertEquals(Response.Status.OK.getStatusCode(), getResponse
				.getStatusInfo().getStatusCode());

		JsonObject jsonObject = getResponse.readEntity(JsonObject.class);
		JsonNumber jsonNumber = jsonObject.getJsonNumber("id");
		long patternId = jsonNumber.longValue();
		assertTrue(patternId > 0);

		// DELETE pattern
		Response deleteResponse = target("patterns/"+patternId).request(
				MediaType.APPLICATION_JSON).delete();
		assertEquals(Response.Status.OK.getStatusCode(), deleteResponse
				.getStatusInfo().getStatusCode());

		// GET pattern (Not Found)
		getResponse = target(RestServer.getBaseURI()+"/patterns/"+patternId).request(
				MediaType.APPLICATION_JSON).get();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), getResponse
				.getStatusInfo().getStatusCode());

	}

	@Test
	public void testExpectedFail(){

		// POST fail
		String patternContent = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \nPREFIX support: <http://www.foi.se/support/wp4demo#> \n\nEPSILON_ONTOLOGY = \"target/test-classes/support/support_schema.owl\"\n\nSTATIC_INSTANCES = \"target/test-classes/support/instances.nt\"\n\nHANDLERS { \nHANDLER {\n\"class\" = \"at.sti2.spark.handler.SupportHandler\"\n\"twominfilter\" = \"true\"\n\"url\" = \"http://www.support-project.eu/SupportPlatform/SupportPlatformEventService.svc/event\"\n}\nHANDLER {\n\"class\" = \"at.sti2.spark.handler.ConsoleHandler\"\n\"verbose\" = \"true\"\n}\n}\n\nCONSTRUCT {\n<http://www.foi.se/support/wp4demo#EventX> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.foi.se/support/wp4demo#Event> .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.foi.se/support/wp4demo#PETPATDetection> .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#name> \"PETPATDetection\" .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#generated-by> <http://www.foi.se/support/wp4demo#StreamProcessor> .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#location> ?location1 .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#location> ?location2 .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#date> \"NOW()\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#report_based_on_service> ?sensor1 .\n<http://www.foi.se/support/wp4demo#EventX> <http://www.foi.se/support/wp4demo#report_based_on_service> ?sensor2 .\n}\n\nWHERE {\n?detection1 <http://www.foi.se/support/wp4demo#has_status> \"true\" .\n?detection1 <http://www.foi.se/support/wp4demo#has_sensor> ?sensor1 .\n?sensor1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.foi.se/support/wp4demo#PETSensor> .\n?sensor1 <http://www.foi.se/support/wp4demo#has_longitude> ?s1long .\n?sensor1 <http://www.foi.se/support/wp4demo#sensor_has_location> ?location1 .\n?location1 <http://www.foi.se/support/wp4demo#location_is_part_of_location> <http://www.foi.se/support/wp4demo#DockX> .\n?detection2 <http://www.foi.se/support/wp4demo#has_status> \"true\" .\n?detection2 <http://www.foi.se/support/wp4demo#has_sensor> ?sensor2 .\n?sensor2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.foi.se/support/wp4demo#PATSensor> .\n?sensor2 <http://www.foi.se/support/wp4demo#sensor_has_location> ?location2 .\n?location2 <http://www.foi.se/support/wp4demo#location_is_part_of_location> <http://www.foi.se/support/wp4demo#DockX> .\nIMEWINDOW (100) \n}\n";
		JsonObject pattern = Json.createObjectBuilder()
				.add("content", patternContent).build();

		Response postResponse = target("patterns").request(
				MediaType.APPLICATION_JSON).post(Entity.json(pattern));
		assertNotNull(postResponse);

		StatusType statusType = postResponse.getStatusInfo();
		assertEquals(statusType.getStatusCode(),
				Response.Status.BAD_REQUEST.getStatusCode());

		// GET fail
		Response getResponse = target("patterns/123").request(
				MediaType.APPLICATION_JSON).get();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), getResponse
				.getStatusInfo().getStatusCode());

		// DELETE fail

	}

}
