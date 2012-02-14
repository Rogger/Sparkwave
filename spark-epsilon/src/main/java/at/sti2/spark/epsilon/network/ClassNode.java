package at.sti2.spark.epsilon.network;

import java.net.URI;
import java.util.List;

import at.sti2.spark.core.stream.StreamedTriple;
import at.sti2.spark.core.triple.RDFTriple;
import at.sti2.spark.core.triple.RDFURIReference;
import at.sti2.spark.epsilon.network.run.Token;
import at.sti2.spark.rete.WorkingMemoryElement;

public class ClassNode extends Node {

	private static String rdfType = "www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	@Override
	public void activateEntry(StreamedTriple triple, List <Token> tokenNodes) {
		activate(triple, tokenNodes, LinkType.LINK_S);
	}
	
	@Override
	public void activate(StreamedTriple triple, List <Token> tokenNodes, LinkType linkType) {
		
		URI subjectValue = null;
		
		//Check for previous activation
		if (hasToken(triple, linkType))
			return;
		
		//Activate
		RDFTriple activeTriple = null;
				
		if (linkType.equals(Node.LinkType.LINK_S))
			activeTriple = new RDFTriple(
				(RDFURIReference)triple.getTriple().getSubject(),
				new RDFURIReference(rdfType),
				new RDFURIReference(getUri()));
		else
			activeTriple = new RDFTriple(
				(RDFURIReference)triple.getTriple().getObject(),
				new RDFURIReference(rdfType),
				new RDFURIReference(getUri()));			
		
		StreamedTriple activeStreamedTriple = new StreamedTriple(
				activeTriple,
				triple.getTimestamp(),
				triple.getContext());
		
		activateAlphaNodes(new WorkingMemoryElement(activeStreamedTriple));

		tokenNodes.add(putToken(triple, linkType));
		
		//Activate over links
		for (Node node : sLinks)
			node.activate(triple, tokenNodes, Node.LinkType.LINK_S);
	}
}
