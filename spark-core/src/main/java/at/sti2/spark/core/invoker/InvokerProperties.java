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
package at.sti2.spark.core.invoker;

import at.sti2.spark.core.condition.TriplePatternGraph;


public class InvokerProperties {
	
	private String invokerClass = null;
	private String invokerBaseURL = null;
	private TriplePatternGraph triplePatternGraph = null;
	
	public InvokerProperties(TriplePatternGraph triplePatternGraph){
		this.triplePatternGraph = triplePatternGraph;
	}
	
	public String getInvokerClass() {
		return invokerClass;
	}
	
	public void setInvokerClass(String invokerClass) {
		this.invokerClass = invokerClass;
	}
	
	public String getInvokerBaseURL() {
		return invokerBaseURL;
	}
	
	public void setInvokerBaseURL(String invokerBaseURL) {
		this.invokerBaseURL = invokerBaseURL;
	}

	public TriplePatternGraph getTriplePatternGraph() {
		return triplePatternGraph;
	}

	public void setTriplePatternGraph(TriplePatternGraph triplePatternGraph) {
		this.triplePatternGraph = triplePatternGraph;
	}
}
