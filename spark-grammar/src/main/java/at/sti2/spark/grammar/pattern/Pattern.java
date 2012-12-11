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

package at.sti2.spark.grammar.pattern;

import java.util.ArrayList;
import java.util.List;

import at.sti2.spark.core.triple.TripleCondition;
import at.sti2.spark.core.triple.TripleConstantTest;


/**
 * 
 * Triple pattern represents an ordered list of triple conditions
 * 
 * @author srdkom
 */
public class Pattern {

	private GraphPattern whereClause = null;
	private List <TripleCondition> constructConditions = null;
	private List <Prefix> prefixes = null;
	private List <Handler> handlers = null;

	public Pattern() {
		super();
		whereClause = null;
		constructConditions = new ArrayList <TripleCondition> ();
		prefixes = new ArrayList <Prefix> ();
		handlers = new ArrayList <Handler>();
	}
	
	public GraphPattern getWhereClause(){
		return whereClause;
	}
	
	public void setWhereClause(GraphPattern whereClause){
		this.whereClause = whereClause;
	}

	public List<TripleCondition> getConstructConditions() {
		return constructConditions;
	}
	
	public List<Prefix> getPrefixes() {
		return prefixes;
	}

	public void addConstructTripleCondition(TripleCondition condition) {
		constructConditions.add(condition);
	}
	
	public void addPrefix(Prefix prefix){
		prefixes.add(prefix);
	}
	
	public TripleCondition getConstructConditionByIndex(int index){
		return constructConditions.get(index);
	}
	
	public Prefix getPrefixByIndex(int index){
		return prefixes.get(index);
	}
	
	public List<Handler> getHandlers() {
		return handlers;
	}

	public void addHandlerProperties(Handler handlerProperties) {
		this.handlers.add(handlerProperties);
	}

	public String getNamespaceByLabel(String label){
		String namespace = null;
		
		for (Prefix prefix : prefixes)
			if (prefix.getLabel().equals(label)){
				namespace = prefix.getNamespace();
				break;
			}
		
		return namespace;
	}
	
	public String toString(){
		
		StringBuffer buffer = new StringBuffer();
		
		if (prefixes.size() > 0){
			buffer.append("PREFIXES\n");
			for (Prefix prefix : prefixes){
				buffer.append(prefix.getLabel());
				buffer.append(" : ");
				buffer.append(prefix.getNamespace());
				buffer.append('\n');
			}
		}
		
		if (handlers != null){
			buffer.append("HANDLERS\n");
			for(Handler handler : handlers){
				buffer.append(handler.toString()).append("\n");
			}
		}
		
		buffer.append("CONSTRUCT\n");
		
		for (TripleCondition condition : constructConditions)
			for (TripleConstantTest constantTest : condition.getConstantTests()){
				buffer.append(constantTest.getTestField());
				buffer.append(' ');
				buffer.append(constantTest.getLexicalTestSymbol());
				buffer.append('\n');
			}
		
		buffer.append("WHERE\n");
		buffer.append(whereClause.toString());
		
		return buffer.toString();
	}

	public void setPrefixes(List<Prefix> prefixes) {
		this.prefixes = prefixes;
	}
}
