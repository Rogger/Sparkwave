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
package at.sti2.spark.grammar.pattern;

import java.util.HashMap;
import java.util.Map;


public class Handler {
	
	private String handlerClass = null;
	private Map<String,String> handlerKeyValue = null;
	private Pattern triplePatternGraph = null;
	
	public Handler(Pattern triplePatternGraph){
		this.triplePatternGraph = triplePatternGraph;
		this.handlerKeyValue = new HashMap<String, String>();
	}
	
	public String getHandlerClass() {
		return handlerClass;
	}
	
	public void setHandlerClass(String invokerClass) {
		this.handlerClass = invokerClass;
	}
	
	public Pattern getTriplePatternGraph() {
		return triplePatternGraph;
	}

	public void setTriplePatternGraph(Pattern triplePatternGraph) {
		this.triplePatternGraph = triplePatternGraph;
	}
	
	public void addKeyValue(String key,String value){
		handlerKeyValue.put(key, value);
	}
	
	public String getValue(String key){
		return handlerKeyValue.get(key);
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Class:");
		buffer.append(getHandlerClass());
		buffer.append("\n");
		buffer.append("KeyValue Pairs:");
		buffer.append(handlerKeyValue);
		buffer.append("\n");
		return super.toString();
	}
}
