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

package at.sti2.spark.rete;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import at.sti2.spark.core.collect.Removable;
import at.sti2.spark.rete.beta.BetaMemory;
import at.sti2.spark.rete.beta.ProductionNode;
import at.sti2.spark.rete.node.RETENode;

public class Token implements Removable{

	private Token parent = null;
	private WorkingMemoryElement wme = null;

	// Added for retracting purposes
	private RETENode node = null; // Points to the memory where this token is in

	// Synchronized list of children (both main RETE thread and GC thread are
	// accessing it)
	private List<Token> children = null;

	// Added for time window purposes
	private long startTime = 0l;
	private long endTime = 0l;
	
	public Token getParentTokenAtBetaMemory(BetaMemory betaMemory){
		if(node == betaMemory){
			return this;
		}else{
			return parent.getParentTokenAtBetaMemory(betaMemory);
		}
	}
	
	public Set<Token> getChildTokensAtBetaMemory(BetaMemory betaMemory){
		Set<Token> tokens = new LinkedHashSet<Token>(); 
		
		//check trivial case: this.node = betaMemory
		if(this.node == betaMemory){
			tokens.add(this);
			return tokens;
		}
		
		List<List<Token>> bfs = new ArrayList<List<Token>>();
		if(children.size()>0)
			bfs.add(children);
		
		while(bfs.size()>0){
			
			for(Token childToken : bfs.remove(0)){
				
				//childtoken at betamemory
				if(childToken.node == betaMemory){
					tokens.add(childToken);
				}else{
					List<Token> list = childToken.children;
					if(list.size()>0)
						bfs.add(list);
				}
				
			}
		}
		
		return tokens;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getTimeInterval() {
		return endTime - startTime;
	}

	public Token() {
		children = new ArrayList<Token>();
	}

	public Token(Token parent, WorkingMemoryElement wme, RETENode node) {

		this.parent = parent;
		this.wme = wme;
		this.node = node;

		children = new ArrayList<Token>();
	}

	public Token getParent() {
		return parent;
	}

	public void setParent(Token parent) {
		this.parent = parent;
	}

	public WorkingMemoryElement getWme() {
		return wme;
	}

	public void setWme(WorkingMemoryElement wme) {
		this.wme = wme;
	}

	public RETENode getNode() {
		return node;
	}

	public void setNode(RETENode node) {
		this.node = node;
	}

	// public List<Token> getChildren() {
	// return children;
	// }

	public void addChild(Token child) {
		children.add(child);
	}

	public void removeChild(Token child) {
		children.remove(child);
	}
	
	@Override
	public void remove() {
		// Remove token from parent
		if(parent!=null)
			parent.removeChild(this);
				
		deleteTokenAndDescendents();
	}

	private void deleteTokenAndDescendents() {

		// Not needed? GC deltes them anyway if there are no more references
		for (Iterator<Token> childrenIterator = children.iterator(); childrenIterator
				.hasNext();) {

			Token childToken = childrenIterator.next();
			childToken.deleteTokenAndDescendents();
			childrenIterator.remove();
		}

		// Remove token from the list of node items
		// TODO Beta and production node are basically the same so it should
		// inherit the same parent
//		if (node instanceof BetaMemory) {
//			((BetaMemory) node).removeItem(this);
//		} else if (node instanceof ProductionNode) {
//			((ProductionNode) node).removeItem(this);
//		}

		// Remove token from the list of tokens in WME
		// THIS REMOVAL IS DONE AT THE LEVEL OF WORKING MEMORY ELEMENT
		// wme.removeToken(this);
		// System.out.println("Removed token from WME. " + wme.toString());

//		 System.out.println("Removed token from parent. " + wme.toString());

		// Remove pointer to parent
//		parent = null;
	}

	/**
	 * Deletes token and all its parents
	 */
//	public void deleteTokenAndParents(){
//		
//		if (parent != null)
//			parent.deleteTokenAndParents();
//
//		//Remove token from the list of node items
//		//TODO Beta and production node are basically the same so it should inherit the same parent
//		if (node instanceof BetaMemory)
//			((BetaMemory)node).removeItem(this);
//		else if (node instanceof ProductionNode)
//			((ProductionNode)node).removeItem(this);
//		
//		//Remove token from the list of tokens in WME
//		wme.removeToken(this);
//		
//		//Remove token from the list of parent children
//		if (parent != null)
//			parent.removeChild(this);
//	}
	
	public String toString(){
		return wme.toString();
	}

}
