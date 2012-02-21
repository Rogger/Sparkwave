package at.sti2.spark.epsilon.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.epsilon.network.run.Token;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.alpha.AlphaNode;

public abstract class Node {
	
	private String uri = null;
	
	public enum LinkType {LINK_S, LINK_O, LINK_SO, LINK_OS};

	protected List<Node> sLinks = null;
	protected List<Node> oLinks = null;
	protected List<Node> soLinks = null;
	protected List<Node> osLinks = null;
	
	protected List<Token> tokens;
	
	protected Vector<AlphaNode> alphaNodes;
	
	public Node() {
		
		sLinks = new ArrayList <Node> ();
		oLinks = new ArrayList <Node> ();
		soLinks = new ArrayList <Node> ();
		osLinks = new ArrayList <Node> ();
		
		tokens = Collections.synchronizedList(new ArrayList<Token>());
		alphaNodes = new Vector<AlphaNode>();
	}
	
	public void addSLinkNode(Node node){
		sLinks.add(node);
	}
	
	public void addOLinkNode(Node node){
		oLinks.add(node);
	}
	
	public void addSOLinkNode(Node node){
		soLinks.add(node);
	}
	
	public void addOSLinkNode(Node node){
		osLinks.add(node);
	}
	
	public void addAlphaNode(AlphaNode alphaNode){
		alphaNodes.add(alphaNode);
	}
	
	public void activateAlphaNodes(WorkingMemoryElement wme){
		for (AlphaNode alphaNode : alphaNodes)
			alphaNode.activate(wme);
	}
	
	public List <Token> getTokens(){
		return tokens;
	}
	
	public void removeToken(Token token){
		synchronized(tokens){
			tokens.remove(token);
		}
	}
	
	public boolean hasToken(Triple streamedTriple, LinkType linkType){
		synchronized(tokens){
			if (tokens.contains(new Token(streamedTriple, this, linkType)))
				return true;
			return false;
		}		
	}
	
	public Token putToken(Triple streamedTriple, LinkType linkType){
		Token token = new Token(streamedTriple, this, linkType);
		synchronized(tokens){
			tokens.add(token);
		}
		return token;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public abstract void activate(Triple triple,  List <Token> tokenNodes, LinkType linkType);
	
	public abstract void activateEntry(Triple triple, List <Token> tokenNodes);
	
}
