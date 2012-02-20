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
				new RDFURIReference(rdfType),
				new RDFURIReference(getUri()));
		else
			activeTriple = new RDFTriple(
				(RDFURIReference)triple.getRDFTriple().getObject(),
				new RDFURIReference(rdfType),
				new RDFURIReference(getUri()));			
		
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
}
