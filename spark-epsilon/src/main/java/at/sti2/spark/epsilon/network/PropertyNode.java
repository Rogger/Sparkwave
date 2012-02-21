package at.sti2.spark.epsilon.network;

import java.util.List;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.epsilon.network.run.Token;
import at.sti2.spark.rete.WorkingMemoryElement;

public class PropertyNode extends Node {

	@Override
	public void activateEntry(Triple triple, List <Token> tokenNodes) {
		activate(triple, tokenNodes, LinkType.LINK_SO);
	}
	
	@Override
	public void activate(Triple triple, List <Token> tokenNodes, LinkType linkType) {
		
		//Check for previous activation
		if (hasToken(triple, linkType))
			return;
		
		RDFTriple activeTriple = null;
		
		if (linkType.equals(Node.LinkType.LINK_SO))
			activeTriple = new RDFTriple(
					triple.getRDFTriple().getSubject(),
					new RDFURIReference(getUri()),
					triple.getRDFTriple().getObject());
		else
			activeTriple = new RDFTriple(
					triple.getRDFTriple().getObject(),
					new RDFURIReference(getUri()),
					triple.getRDFTriple().getSubject());
		
		Triple activeStreamedTriple = new Triple(
				activeTriple,
				triple.getTimestamp(),
				triple.isPermanent(),
				triple.getContext());
		
		activateAlphaNodes(new WorkingMemoryElement(activeStreamedTriple));
		
		tokenNodes.add(putToken(triple, linkType));
		
		//Activate other links
		for (Node node : sLinks)
			if (linkType.equals(Node.LinkType.LINK_SO))			
				node.activate(triple, tokenNodes, Node.LinkType.LINK_S);
			else
				node.activate(triple, tokenNodes, Node.LinkType.LINK_O);
				
		for (Node node : oLinks)
			if (linkType.equals(Node.LinkType.LINK_SO))			
				node.activate(triple, tokenNodes, Node.LinkType.LINK_O);
			else
				node.activate(triple, tokenNodes, Node.LinkType.LINK_S);
				
		for (Node node : soLinks)
			if (linkType.equals(Node.LinkType.LINK_SO))			
				node.activate(triple, tokenNodes, Node.LinkType.LINK_SO);
			else
				node.activate(triple, tokenNodes, Node.LinkType.LINK_OS);
				
		for (Node node : osLinks)
			if (linkType.equals(Node.LinkType.LINK_SO))			
				node.activate(triple, tokenNodes, Node.LinkType.LINK_OS);
			else
				node.activate(triple, tokenNodes, Node.LinkType.LINK_SO);
	}
}
