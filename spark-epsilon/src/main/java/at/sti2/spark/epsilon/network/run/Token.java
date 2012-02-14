package at.sti2.spark.epsilon.network.run;

import at.sti2.spark.core.stream.StreamedTriple;
import at.sti2.spark.epsilon.network.Node;
import at.sti2.spark.epsilon.network.Node.LinkType;

public class Token {

	private StreamedTriple streamedTriple = null;
	private LinkType linkType = null;
	private Node tokenNode = null;
	
	public Token(StreamedTriple streamedTriple, Node tokenNode, LinkType linkType) {
		this.streamedTriple = streamedTriple;
		this.linkType = linkType;
		this.tokenNode = tokenNode;
	}
	public StreamedTriple getStreamedTriple() {
		return streamedTriple;
	}
	public LinkType getLinkType() {
		return linkType;
	}
	
	public Node getTokenNode(){
		return tokenNode;
	}
	
	public void removeTokenFromNode(){
		tokenNode.removeToken(this);
	}
	
	@Override
	public boolean equals(Object token){
		//If tokens point to the same StreamedTriple instance and same LinkType they are the same
		if ((streamedTriple == ((Token)token).getStreamedTriple()) && 
			(linkType == ((Token)token).getLinkType()) &&
			(tokenNode == ((Token)token).getTokenNode()))
			return true;
		else
			return false;
	}
}
