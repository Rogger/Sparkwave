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
package at.sti2.spark.epsilon.network;

import java.util.List;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.epsilon.network.run.Token;
import at.sti2.spark.rete.WorkingMemoryElement;

public class ClassNode extends Node {

	private static String rdfType = "www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	@Override
	public void activateEntry(Triple triple, List <Token> tokenNodes) {
		activate(triple, tokenNodes, LinkType.LINK_S);
	}
	
	@Override
	public void activate(Triple triple, List <Token> tokenNodes, LinkType linkType) {
		
		//Check for previous activation
		if (hasToken(triple, linkType))
			return;
		
		//Activate
		RDFTriple activeTriple = null;
				
		if (linkType.equals(Node.LinkType.LINK_S))
			activeTriple = new RDFTriple(
				(RDFURIReference)triple.getRDFTriple().getSubject(),
				RDFURIReference.Factory.createURIReference(rdfType),
				RDFURIReference.Factory.createURIReference(getUri()));
		else
			activeTriple = new RDFTriple(
				(RDFURIReference)triple.getRDFTriple().getObject(),
				RDFURIReference.Factory.createURIReference(rdfType),
				RDFURIReference.Factory.createURIReference(getUri()));			
		
		Triple activeStreamedTriple = new Triple(
				activeTriple,
				triple.getTimestamp(),
				triple.isPermanent(),
				triple.getContext());
		
		activateAlphaNodes(new WorkingMemoryElement(activeStreamedTriple));

		tokenNodes.add(putToken(triple, linkType));
		
		//Activate over links
		for (Node node : sLinks)
			node.activate(triple, tokenNodes, Node.LinkType.LINK_S);
	}
	
	@Override
	public String toString() {
		return uri;
	}
}
