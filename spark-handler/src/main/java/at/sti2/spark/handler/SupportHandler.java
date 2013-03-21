/*
 * Copyright (c) 2013, University of Innsbruck, Austria.
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
package at.sti2.spark.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.triple.TripleCondition;
import at.sti2.spark.grammar.pattern.Handler;
import at.sti2.spark.preprocess.RDFFormatTransformer;
import at.sti2.spark.preprocess.XSLTransformer;

public class SupportHandler implements SparkwaveHandler {
	
	private long twoMinutesPause = 0l;

	private static Logger logger = Logger.getLogger(SupportHandler.class);
	private Handler handlerProperties = null;
	
	@Override
	public void init(Handler handlerProperties) {
		this.handlerProperties = handlerProperties;
	}
	
	@Override
	public void invoke(Match match) throws SparkwaveHandlerException{
		
		/*
		 * TODO This is an ugly hack to stop Impactorium handler of sending thousands of matches regarding the same event. 
		 *
		 ******************************************************/
		String value = handlerProperties.getValue("twominfilter");
		if(value == null || (value != null && value.equals("true"))){
			long timestamp = (new Date()).getTime();
			if (timestamp-twoMinutesPause < 120000)
				return;
			
			twoMinutesPause = timestamp;			
		}
		/* *****************************************************/
		
		final String url = handlerProperties.getValue("url");
		logger.info("Invoking URL " + url);
	
		// formatting match to n-triple format
		final List<TripleCondition> conditions = handlerProperties.getTriplePatternGraph().getConstruct().getConditions();
		final String formatMatchNTriples = match.outputNTriples(conditions);
		
		// converting n-triple string to inputstream
		final InputStream strIn = IOUtils.toInputStream(formatMatchNTriples);
		
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		
		// convert n-triple to RDFXML
		RDFFormatTransformer ntToRDFXML = new RDFFormatTransformer();
		ntToRDFXML.init(strIn, out1);
		ntToRDFXML.setProperty("from", "N3");
		ntToRDFXML.setProperty("to", "RDF/XML-ABBREV");
		ntToRDFXML.process();
		
		logger.debug("N3 -> RDF/XML output:\n"+out1.toString());
		
		ByteArrayInputStream in1 = new ByteArrayInputStream(out1.toByteArray());
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		
		// convert RDFXML to XML
		XSLTransformer rdfxmlToXML = new XSLTransformer();
		rdfxmlToXML.init(in1 , out2);
		rdfxmlToXML.setProperty("xsltLocation", "target/classes/support/fromRDFToEvent.xslt");
		rdfxmlToXML.process();
		
		String strEvent = out2.toString();
		logger.debug("RDF/XML -> Event XML output:\n"+strEvent);
		
		sendToREST(url, strEvent);
	}
	
	private void sendToREST(String url, String content){
		
		//HTTP Post
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		
		try {
			StringEntity postStringEntity = new StringEntity(content);
			postStringEntity.setContentType("text/xml");
			
			httpPost.addHeader("Accept", "*/*");
			httpPost.setEntity(postStringEntity);
			HttpResponse response = httpclient.execute(httpPost);
			
			logger.info("[POSTING Event] Status code " + response.getStatusLine());
			
			//First invocation succeeded
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				
				HttpEntity entityResponse = response.getEntity();
				
				//Something has been returned, let's see what is in it
				if (entityResponse != null){
					String stringResponse = EntityUtils.toString(entityResponse);
					logger.debug("Response " + stringResponse);
				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		} catch (ClientProtocolException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} finally{
			httpclient.getConnectionManager().shutdown();
		}
	}
	
}
