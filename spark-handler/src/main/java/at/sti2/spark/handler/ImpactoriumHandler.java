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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.triple.TripleCondition;
import at.sti2.spark.grammar.pattern.Handler;

public class ImpactoriumHandler implements SparkwaveHandler {
	
	/*
	 * TODO Remove this. This is an ugly hack to stop Impactorium handler of sending thousands of matches regarding the same event. 
	 */
	private long twoMinutesPause = 0l;

	static Logger logger = LoggerFactory.getLogger(ImpactoriumHandler.class);
	Handler handlerProperties = null;
	
	@Override
	public void init(Handler handlerProperties) {
		this.handlerProperties = handlerProperties;
		
	}
	
	@Override
	public void invoke(Match match) throws SparkwaveHandlerException{
		
		/*
		 * TODO Remove this. This is an ugly hack to stop Impactorium handler of sending thousands of matches regarding the same event. 
		 *
		 ******************************************************/
		long timestamp = (new Date()).getTime();
		if (timestamp-twoMinutesPause < 120000)
			return;
		
		twoMinutesPause = timestamp;
		/* *****************************************************/
		
		String baseurl = handlerProperties.getValue("baseurl");
		logger.info("Invoking impactorium at base URL " + baseurl);
		
		//Define report id value 
		String reportId = "" + (new Date()).getTime();
		
		//HTTP PUT the info-object id
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut httpPut = new HttpPut(baseurl + "/info-object");
		
		try {
			StringEntity infoObjectEntityRequest = new StringEntity("<info-object name=\"Report " + reportId + " \"/>", "UTF-8");
			httpPut.setEntity(infoObjectEntityRequest);
			HttpResponse response = httpclient.execute(httpPut);
			
			logger.info("[CREATING REPORT] Status code " + response.getStatusLine());
			
			//First invocation succeeded
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				
				HttpEntity infoObjectEntityResponse = response.getEntity();
				
				//Something has been returned, let's see what is in it
				if (infoObjectEntityResponse != null){
					String infoObjectResponse = EntityUtils.toString(infoObjectEntityResponse);
					logger.debug("InfoObject response " + infoObjectResponse);
					
				//Extract info-object identifier
				String infoObjectReportId = extractInfoObjectIdentifier(infoObjectResponse);
				if(infoObjectReportId==null){
					logger.error("Info object report id " + infoObjectReportId);
				}else{
					logger.info("Info object report id " + infoObjectReportId);			

					//Format the output for the match
					final List<TripleCondition> conditions = handlerProperties.getTriplePatternGraph().getConstruct().getConditions();
					final String ntriplesOutput = match.outputNTriples(conditions);
					
					//HTTP PUT the data 
					httpPut = new HttpPut(baseurl + "/info-object/" + infoObjectReportId + "/data/data.nt");
					StringEntity dataEntityRequest = new StringEntity(ntriplesOutput, "UTF-8");
					httpPut.setEntity(dataEntityRequest);
					response = httpclient.execute(httpPut);
					
					logger.info("[STORING DATA] Status code " + response.getStatusLine());
					
					//First invocation succeeded
					if (!(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK))
						throw new SparkwaveHandlerException("Could not write data.");
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		} catch (ClientProtocolException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	public String extractInfoObjectIdentifier(String infoObjectResponse){
		
		String reportId = null;
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		//dbf.setNamespaceAware(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new ByteArrayInputStream(infoObjectResponse.getBytes("UTF-8")));
			
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile("//info-object");
			Object result = expr.evaluate(doc, XPathConstants.NODESET);
			
			NodeList nodes = (NodeList) result;
			Node item = nodes.item(0);
			if(item!=null){
				NamedNodeMap attributesMap = item.getAttributes();
				Node idAttribute = attributesMap.getNamedItem("id");
				reportId = idAttribute.getNodeValue();
			}
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return reportId;
	}
	
}
