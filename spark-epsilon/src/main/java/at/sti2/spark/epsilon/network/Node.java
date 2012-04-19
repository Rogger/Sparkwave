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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.epsilon.network.run.Token;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.alpha.AlphaNode;

public abstract class Node {

	private String uri = null;

	public enum LinkType {
		LINK_S, LINK_O, LINK_SO, LINK_OS
	};

	protected List<Node> sLinks = null;
	protected List<Node> oLinks = null;
	protected List<Node> soLinks = null;
	protected List<Node> osLinks = null;

	protected List<Token> tokens;

	protected Vector<AlphaNode> alphaNodes;

	public Node() {

		sLinks = new ArrayList<Node>();
		oLinks = new ArrayList<Node>();
		soLinks = new ArrayList<Node>();
		osLinks = new ArrayList<Node>();

//		tokens = Collections.synchronizedList(new ArrayList<Token>());
		tokens = new ArrayList<Token>();
		alphaNodes = new Vector<AlphaNode>();
	}

	public void addSLinkNode(Node node) {
		sLinks.add(node);
	}

	public void addOLinkNode(Node node) {
		oLinks.add(node);
	}

	public void addSOLinkNode(Node node) {
		soLinks.add(node);
	}

	public void addOSLinkNode(Node node) {
		osLinks.add(node);
	}

	public void addAlphaNode(AlphaNode alphaNode) {
		alphaNodes.add(alphaNode);
	}

	public void activateAlphaNodes(WorkingMemoryElement wme) {
		for (AlphaNode alphaNode : alphaNodes)
			alphaNode.activate(wme);
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public void removeToken(Token token) {
		tokens.remove(token);
	}

	public boolean hasToken(Triple streamedTriple, LinkType linkType) {
		if (tokens.contains(new Token(streamedTriple, this, linkType)))
			return true;
		return false;
	}

	public Token putToken(Triple streamedTriple, LinkType linkType) {
		Token token = new Token(streamedTriple, this, linkType);
		tokens.add(token);
		return token;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public abstract void activate(Triple triple, List<Token> tokenNodes,
			LinkType linkType);

	public abstract void activateEntry(Triple triple, List<Token> tokenNodes);

}
