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
