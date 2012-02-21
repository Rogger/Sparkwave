package at.sti2.spark.epsilon.network.run;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.epsilon.network.Node;
import at.sti2.spark.epsilon.network.Node.LinkType;

/**
 * Epsilon network token enables tracking of network history execution
 * 
 * @author skomazec
 *
 */
public class Token {

	private Triple streamedTriple = null;
	private LinkType linkType = null;
	private Node tokenNode = null;
	
	public Token(Triple streamedTriple, Node tokenNode, LinkType linkType) {
		this.streamedTriple = streamedTriple;
		this.linkType = linkType;
		this.tokenNode = tokenNode;
	}
	public Triple getStreamedTriple() {
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
