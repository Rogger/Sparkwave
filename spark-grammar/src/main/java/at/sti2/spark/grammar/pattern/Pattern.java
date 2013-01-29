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


/**
 * 
 * Triple pattern
 * @author srdkom
 * @author michaelrogger
 */
public class Pattern {

	private GraphPattern whereClause = null;
	private Construct construct = null;
	private List <Prefix> prefixes = null;
	private String epsilonOntology = "null";
	private String staticInstances = "null";
	private List <Handler> handlers = null;

	public Pattern() {
		super();
		whereClause = null;
		prefixes = new ArrayList <Prefix> ();
		construct = new Construct();
		handlers = new ArrayList <Handler>();
	}
	
	public GraphPattern getWhereClause(){
		return whereClause;
	}
	
	public void setWhereClause(GraphPattern whereClause){
		this.whereClause = whereClause;
	}

	public Construct getConstructConditions() {
		return construct;
	}
	
	public List<Prefix> getPrefixes() {
		return prefixes;
	}

	public void addPrefix(Prefix prefix){
		prefixes.add(prefix);
	}
	
	public Prefix getPrefixByIndex(int index){
		return prefixes.get(index);
	}
	
	public List<Handler> getHandlers() {
		return handlers;
	}
	
	public String getEpsilonOntology() {
		return epsilonOntology;
	}

	public void setEpsilonOntology(String epsilonOntology) {
		this.epsilonOntology = epsilonOntology;
	}

	public String getStaticInstances() {
		return staticInstances;
	}

	public void setStaticInstances(String staticInstances) {
		this.staticInstances = staticInstances;
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
	
	public Construct getConstruct() {
		return construct;
	}

	public void setConstruct(Construct construct) {
		this.construct = construct;
	}

	public String toString(){
		
		StringBuffer buffer = new StringBuffer();
		
		if (prefixes.size() > 0){
			for (Prefix prefix : prefixes){
				buffer.append("PREFIX ");
				buffer.append(prefix.getLabel()).append(": ");
				buffer.append("<").append(prefix.getNamespace()).append("> \n");
			}
		}
		buffer.append("\n");
		
		buffer.append("EPSILON_ONTOLOGY = \""+epsilonOntology+"\"\n");
		buffer.append("\n");
		
		buffer.append("STATI_INSTANCES = \""+staticInstances+"\"\n");
		buffer.append("\n");
		
		if (handlers != null){
			buffer.append("HANDLERS { \n");
			for(Handler handler : handlers){
				buffer.append(handler.toString());
			}
			buffer.append("}\n");
		}
		buffer.append("\n");

		buffer.append("CONSTRUCT {\n");
		buffer.append(construct.toString());
		buffer.append("}\n");

		buffer.append("\n");
		buffer.append("WHERE {\n");
		buffer.append(whereClause.toString());
		buffer.append("}\n");
		
		return buffer.toString();
	}

	public void setPrefixes(List<Prefix> prefixes) {
		this.prefixes = prefixes;
	}
}
