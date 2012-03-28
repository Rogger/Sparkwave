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
package at.sti2.spark.output;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import at.sti2.spark.core.condition.TripleCondition;
import at.sti2.spark.core.condition.TriplePatternGraph;
import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.solution.OutputBuffer;
import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.core.triple.variable.RDFVariable;

public class SparkweaveNetworkOutputThread extends Thread {

	static Logger logger = Logger.getLogger(SparkweaveNetworkOutputThread.class);
	
	private OutputBuffer outputBuffer = null;
	private TriplePatternGraph triplePatternGraph = null;
	
	public SparkweaveNetworkOutputThread(TriplePatternGraph patternGraph, OutputBuffer outputBuffer){
		this.outputBuffer = outputBuffer;
		this.triplePatternGraph = patternGraph;
	}
	
	public void run(){
		Match match = null;
		while(true){
			try {
				match = outputBuffer.get();
				System.out.println(formatMatch(match));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String formatMatch(Match match){
		StringBuffer buffer = new StringBuffer();
		for (TripleCondition condition : triplePatternGraph.getConstructConditions()){
			
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
				buffer.append(">\n");
				
			} else if (condition.getConditionTriple().getObject() instanceof RDFVariable){
				
				String variableId = ((RDFVariable)condition.getConditionTriple().getObject()).getVariableId();
				RDFValue value = match.getVariableBindings().get(variableId);
				
				if (value instanceof RDFURIReference){
					buffer.append('<');
					buffer.append(value.toString());
					buffer.append(">\n");
				} else if (value instanceof RDFLiteral){
					buffer.append('\"');
					buffer.append(((RDFLiteral)value).getValue());
					buffer.append('\"');
					buffer.append("^^<");
					buffer.append(((RDFLiteral)value).getDatatypeURI());
					buffer.append(">\n");
				}
			}
		}
		return buffer.toString();
	}
	
	private String outputMatch(Match match){
		StringBuffer buffer = new StringBuffer();
		Enumeration <String> keyEnum = match.getVariableBindings().keys();
		while(keyEnum.hasMoreElements()){
			String variableId = (String)keyEnum.nextElement();
			RDFValue value = (RDFValue)match.getVariableBindings().get(variableId);
			buffer.append(variableId);
			buffer.append(" : ");
			buffer.append(value.toString());
			buffer.append('\n');
		}		
		return buffer.toString();		
	}
}
