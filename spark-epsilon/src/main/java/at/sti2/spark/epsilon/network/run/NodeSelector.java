/*
 * Copyright (c) 2011, University of Innsbruck, Austria.
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
package at.sti2.spark.epsilon.network.run;

import java.util.HashMap;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.epsilon.network.Node;

/**
 * An entering triple can trigger only one node in the network.
 * The class is providing lookup functionality based on class/property values and pointers to the corresponding epsilon nodes.
 * 
 * @author skomazec
 *
 */
public class NodeSelector {
	
	//TODO Investigate if two hashtables make sense
	private static String rdfTypeURIReference = new String("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	private HashMap<String, Node> nodeLookupMap = new HashMap <String, Node> ();
	
	public void put(String uri, Node node){
		nodeLookupMap.put(uri, node);
	}
	
	/*
	 * The method returns a Node instance as the entrance to the network or null
	 * 
	 * The method makes an examination of the triple whether it is rdf:type or a pure property triple.
	 */
	public Node lookup(Triple streamedTriple){
		
		RDFTriple rdfTriple = streamedTriple.getRDFTriple();
		
		String key = ((RDFURIReference)rdfTriple.getPredicate()).toString();
		
		if (key.equals(rdfTypeURIReference))
			key = ((RDFURIReference)rdfTriple.getObject()).toString();
		
		return nodeLookupMap.get(key);
	}
}
