/*
 * Copyright (c) 2010, University of Innsbruck, Austria.
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

package at.sti2.spark.rete.node;

import java.util.ArrayList;
import java.util.List;

import at.sti2.spark.rete.Token;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.beta.BetaMemory;
import at.sti2.spark.rete.beta.JoinNode;

public abstract class RETENode {

	protected List<RETENode> children = null;
	protected RETENode parent = null;

	public RETENode() {
		children = new ArrayList<RETENode>();
	}

	public RETENode(List<RETENode> children, RETENode parent) {
		super();
		this.children = children;
		this.parent = parent;
	}

	public RETENode getParent() {
		return parent;
	}

	public void setParent(RETENode parent) {
		this.parent = parent;
	}

	public void setChildren(List<RETENode> children) {
		this.children = children;
	}

	public List<RETENode> getChildren() {
		return children;
	}

	public void addChild(RETENode child) {
		children.add(child);
	}
	
	//TODO This should be an abstract method and the code should go to the nodes
	public void update(){
		
		if (parent instanceof BetaMemory){
			
//			synchronized(((BetaMemory) parent).getItems()){
//				for (Iterator <Token> tokenIter = ((BetaMemory) parent).getItems().iterator(); tokenIter.hasNext();)
//					leftActivate(tokenIter.next());
//			}
			
		} else if (parent instanceof JoinNode){
		
			List <RETENode> parentChildren = parent.getChildren();
			parent.setChildren(new ArrayList <RETENode> ());
			parent.addChild(this);

//			// dynamic
//			for (WorkingMemoryElement wme : ((JoinNode) parent)
//					.getAlphaMemory().getItems())
//				parent.rightActivate(wme);
//
//			// permanent
//			for (WorkingMemoryElement wme : ((JoinNode) parent)
//					.getAlphaMemory().getPermanentItems())
//				parent.rightActivate(wme);
//
//			parent.setChildren(parentChildren);
		}

	}

	public abstract void rightActivate(WorkingMemoryElement wme);

	public abstract void leftActivate(Token token);

	public abstract void leftActivate(Token token, WorkingMemoryElement wme);
}
