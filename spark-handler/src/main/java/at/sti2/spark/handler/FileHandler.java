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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.core.triple.RDFVariable;
import at.sti2.spark.core.triple.TripleCondition;
import at.sti2.spark.grammar.pattern.Handler;

public class FileHandler implements SparkweaveHandler {

	protected static Logger logger = Logger.getLogger(FileHandler.class);
	private Handler handlerProperties = null;
	
	//file logger
	private File path = null;
	private FileWriter writer = null;
	
	private int count = 0;
	
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	@Override
	public void init(Handler handlerProperties) throws SparkweaveHandlerException {
		this.handlerProperties = handlerProperties;
		this.path = new File(handlerProperties.getValue("path"));
		try {
			if(!path.exists()){
				path.createNewFile();				
			}
		} catch (IOException e) {
			throw new SparkweaveHandlerException(e);
		}
	}
	
	@Override
	public void invoke(Match match) throws SparkweaveHandlerException{
		
		logger.debug("Invoking FileHandler");
		Date date = new Date();
		
		//store match to file
		StringBuffer sb = new StringBuffer();
		sb.append(dateFormat.format(date)).append(" match_number:").append(count++).append("\n");
		sb.append( formatMatchNTriples(match, handlerProperties) );
		try {
			writer = new FileWriter(path,true);
			writer.write(sb.toString());
			writer.close();
			logger.info("Match number "+count+" written to file "+path);
		} catch (IOException e) {
			throw new SparkweaveHandlerException(e);
		}
		
	}
		
	private StringBuffer formatMatchNTriples(Match match, Handler handlerProperties){
		
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
		return buffer;
	}
}
