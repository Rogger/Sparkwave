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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.core.triple.RDFVariable;
import at.sti2.spark.core.triple.TripleCondition;
import at.sti2.spark.grammar.pattern.Handler;
import at.sti2.spark.preprocess.RDFFormatTransformer;
import at.sti2.spark.preprocess.XSLTransformer;

public class SupportHandler implements SparkwaveHandler {
	
	private long twoMinutesPause = 0l;

	private static Logger logger = Logger.getLogger(SupportHandler.class);
	private Handler handlerProperties = null;
	
//	private ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("SupportHandler-%d").build();
//	private ExecutorService executor = Executors.newCachedThreadPool(tf); 
	
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
		long timestamp = (new Date()).getTime();
		if (timestamp-twoMinutesPause < 120000)
			return;
		
		twoMinutesPause = timestamp;
		/* *****************************************************/
		
		final String url = handlerProperties.getValue("url");
		logger.info("Invoking URL " + url);
	
		// formatting match to n-triple format
		final String formatMatchNTriples = formatMatchNTriples(match, handlerProperties);
		
		// converting n-triple string to inputstream
		final InputStream strIn = IOUtils.toInputStream(formatMatchNTriples);
		
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		
		// convert n-triple to RDFXML
		RDFFormatTransformer ntToRDFXML = new RDFFormatTransformer();
		ntToRDFXML.init(strIn, out1);
		ntToRDFXML.setProperty("from", "N3");
		ntToRDFXML.setProperty("to", "RDF/XML-ABBREV");
		ntToRDFXML.process();
		
		ByteArrayInputStream in1 = new ByteArrayInputStream(out1.toByteArray());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		// convert RDFXML to XML
		XSLTransformer rdfxmlToXML = new XSLTransformer();
		rdfxmlToXML.init(in1 , baos);
		rdfxmlToXML.setProperty("xsltLocation", "target/classes/support/fromRDFToEvent.xslt");
		rdfxmlToXML.process();
		
	}
	
	private void sendToREST(String url, String content){
		//Define report id value 
//		String reportId = "" + (new Date()).getTime();
		
		//HTTP Post
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		
		try {
			StringEntity postStringEntity = new StringEntity(content);
			
			httpPost.setEntity(postStringEntity);
			HttpResponse response = httpclient.execute(httpPost);
			
			logger.info("[CREATING REPORT] Status code " + response.getStatusLine());
			
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
	
	private String formatMatchNTriples(Match match, Handler handlerProperties){
		
		StringBuffer buffer = new StringBuffer();
		for (TripleCondition condition : handlerProperties.getTriplePatternGraph().getConstruct().getConditions()){
			
			//Resolve subject
			buffer.append('<');
			if(condition.getConditionTriple().getSubject() instanceof RDFURIReference)
				
				buffer.append(((RDFURIReference)condition.getConditionTriple().getSubject()).toString());
			
			else if (condition.getConditionTriple().getSubject() instanceof RDFVariable){
				
				String variableId = ((RDFVariable)condition.getConditionTriple().getSubject()).getVariableId();
				buffer.append(match.getVariableBindings().get(variableId).toString());
				
			}
			buffer.append("> ");
			
			//Resolve predicate
			buffer.append('<');
			if(condition.getConditionTriple().getPredicate() instanceof RDFURIReference)
				
				buffer.append(((RDFURIReference)condition.getConditionTriple().getPredicate()).toString());
			
			else if (condition.getConditionTriple().getPredicate() instanceof RDFVariable){
				
				String variableId = ((RDFVariable)condition.getConditionTriple().getPredicate()).getVariableId();
				buffer.append(match.getVariableBindings().get(variableId).toString());
				
			}
			buffer.append("> ");
			
			//Resolve object
			if(condition.getConditionTriple().getObject() instanceof RDFURIReference){
				
				buffer.append('<');
				buffer.append(((RDFURIReference)condition.getConditionTriple().getObject()).toString());
				buffer.append("> .\n");
				
			} else if (condition.getConditionTriple().getObject() instanceof RDFVariable){
				
				String variableId = ((RDFVariable)condition.getConditionTriple().getObject()).getVariableId();
				RDFValue value = match.getVariableBindings().get(variableId);
				
				if (value instanceof RDFURIReference){
					buffer.append('<');
					buffer.append(value.toString());
					buffer.append("> .\n");
				} else if (value instanceof RDFLiteral){
					buffer.append('\"');
					buffer.append(((RDFLiteral)value).getValue());
					buffer.append('\"');
					buffer.append("^^<");
					buffer.append(((RDFLiteral)value).getDatatypeURI());
					buffer.append("> .\n");
				}
			} else if (condition.getConditionTriple().getObject() instanceof RDFLiteral){
				buffer.append('\"');
				buffer.append(((RDFLiteral)condition.getConditionTriple().getObject()).getValue());
				buffer.append('\"');
				buffer.append("^^<");
				buffer.append(((RDFLiteral)condition.getConditionTriple().getObject()).getDatatypeURI());
				buffer.append("> .\n");
			}
		}
		return buffer.toString();
	}
}
