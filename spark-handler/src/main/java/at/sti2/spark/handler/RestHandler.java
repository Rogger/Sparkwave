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
package at.sti2.spark.handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

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

/**
 * This handler sends the matched pattern to a REST service using HTTP-POST.
 * url: mandatory parameter that specifies the URL of the REST service
 * @author michaelrogger
 *
 */
public class RestHandler implements SparkwaveHandler {
	
	private static Logger logger = Logger.getLogger(RestHandler.class);
	private Handler handlerProperties = null;
	
	@Override
	public void init(Handler handlerProperties) {
		this.handlerProperties = handlerProperties;
	}
	
	@Override
	public void invoke(Match match) throws SparkwaveHandlerException{
	
		String url = handlerProperties.getValue("url");
		logger.info("Invoking impactorium at URL " + url);
		
		final List<TripleCondition> conditions = handlerProperties.getTriplePatternGraph().getConstruct().getConditions();
		final String ntriplesOutput = match.outputNTriples(conditions);
		
		//HTTP PUT the info-object id
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		
		try {
			StringEntity infoObjectEntityRequest = new StringEntity(ntriplesOutput);
			
			httpPost.setEntity(infoObjectEntityRequest);
			HttpResponse response = httpclient.execute(httpPost);
			
			logger.info("[CREATING REPORT] Status code " + response.getStatusLine());
			
			//First invocation succeeded
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				
				HttpEntity infoObjectEntityResponse = response.getEntity();
				
				//Something has been returned, let's see what is in it
				if (infoObjectEntityResponse != null){
					String infoObjectResponse = EntityUtils.toString(infoObjectEntityResponse);
					logger.debug("InfoObject response " + infoObjectResponse);
				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		} catch (ClientProtocolException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
}
