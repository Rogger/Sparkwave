/*
 * Copyright (c) 2010, University of Innsbruck, Austria.
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
package at.sti2.spark.core.solution;

import java.util.Hashtable;
import java.util.List;

import at.sti2.spark.core.triple.RDFLiteral;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.core.triple.RDFVariable;
import at.sti2.spark.core.triple.TripleCondition;

public class Match {

	private Hashtable <String, RDFValue> variableBindings = new Hashtable <String, RDFValue>();

	public Hashtable<String, RDFValue> getVariableBindings() {
		return variableBindings;
	}

	public void setVariableBindings(Hashtable<String, RDFValue> variableBindings) {
		this.variableBindings = variableBindings;
	}
	
	public String outputNTriples(List<TripleCondition> conditions){
		
		StringBuffer buffer = new StringBuffer();
		for (TripleCondition condition : conditions){
			
			//Resolve subject
			buffer.append('<');
			if(condition.getConditionTriple().getSubject() instanceof RDFURIReference)
				
				buffer.append(((RDFURIReference)condition.getConditionTriple().getSubject()).toString());
			
			else if (condition.getConditionTriple().getSubject() instanceof RDFVariable){
				
				String variableId = ((RDFVariable)condition.getConditionTriple().getSubject()).getVariableId();
				buffer.append(variableBindings.get(variableId).toString());
				
			}
			buffer.append("> ");
			
			//Resolve predicate
			buffer.append('<');
			if(condition.getConditionTriple().getPredicate() instanceof RDFURIReference)
				
				buffer.append(((RDFURIReference)condition.getConditionTriple().getPredicate()).toString());
			
			else if (condition.getConditionTriple().getPredicate() instanceof RDFVariable){
				
				String variableId = ((RDFVariable)condition.getConditionTriple().getPredicate()).getVariableId();
				buffer.append(variableBindings.get(variableId).toString());
				
			}
			buffer.append("> ");
			
			//Resolve object
			if(condition.getConditionTriple().getObject() instanceof RDFURIReference){
				
				buffer.append('<');
				buffer.append(((RDFURIReference)condition.getConditionTriple().getObject()).toString());
				buffer.append("> .\n");
				
			} else if (condition.getConditionTriple().getObject() instanceof RDFVariable){
				
				String variableId = ((RDFVariable)condition.getConditionTriple().getObject()).getVariableId();
				RDFValue value = variableBindings.get(variableId);
				
				if (value instanceof RDFURIReference){
					buffer.append('<');
					buffer.append(value.toString());
					buffer.append("> .\n");
				} else if (value instanceof RDFLiteral){
					buffer.append(value.toString());
					buffer.append(".\n");
				}
			} else if (condition.getConditionTriple().getObject() instanceof RDFLiteral){
				buffer.append(((RDFLiteral)condition.getConditionTriple().getObject()).toString());
				buffer.append(" .\n");
			}
		}
		return buffer.toString();
	}
}
