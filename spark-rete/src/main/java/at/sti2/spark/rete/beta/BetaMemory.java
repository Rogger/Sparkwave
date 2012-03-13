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

package at.sti2.spark.rete.beta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.sti2.spark.core.stream.Triple;
import at.sti2.spark.rete.Token;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.node.RETENode;

public class BetaMemory extends RETENode {

	private List <Token> items = null;
	
	private boolean rootNode = false;
	
	public BetaMemory(){
		items = Collections.synchronizedList(new ArrayList <Token> ());
	}
	
	public void addItem(Token token){
		synchronized(items){
			items.add(token);
		}
	}
	
	public void removeItem(Token token){
		synchronized(items){
			items.remove(token);
		}
	}
	
	public List <Token> getItems(){
		return items;
	}
	
	@Override
	public void leftActivate(Token parentToken, WorkingMemoryElement wme){
		
		Token newToken = createToken(parentToken, wme);
		
		//TODO Insert token at the head of items
		addItem(newToken);
		
		for (RETENode reteNode : children)
			reteNode.leftActivate(newToken);
	}

	@Override
	public void leftActivate(Token token) {
		
	}
	
	@Override
	public void rightActivate(WorkingMemoryElement wme) {
		// TODO Auto-generated method stub
	}
	
	private Token createToken(Token parentToken, WorkingMemoryElement wme){
		
		Token newToken = new Token();
		newToken.setParent(parentToken);
		newToken.setWme(wme);
		
		//Added for retraction purposes
		newToken.setNode(this);
		
		//TODO Insert token at the head of WME tokens
		wme.addToken(newToken);
		
		//TODO Insert token at the head of parent's children
		if (parentToken!=null){
			parentToken.addChild(newToken);
		
			//Insert initial time interval for the new token
			newToken.setStartTime(parentToken.getStartTime());
			newToken.setEndTime(parentToken.getEndTime());
			
			Triple triple = wme.getTriple();
			long tripleTimestamp = triple.getTimestamp();
			long tokenStartTime = newToken.getStartTime();
			long tokenEndTime = newToken.getEndTime();
			
			if(!triple.isPermanent()){
				if (tripleTimestamp<tokenStartTime)
					newToken.setStartTime(tripleTimestamp);
				else if (tripleTimestamp>tokenEndTime)
					newToken.setEndTime(tripleTimestamp);
			}
		} else {
			//TODO What if the first token is the static one?
			//Token without parent is token at dummy (root) beta memory
			//It will have start and end time as streamed triple
			newToken.setStartTime(wme.getTriple().getTimestamp());
			newToken.setEndTime(wme.getTriple().getTimestamp());
		}
		
		return newToken;
	}
	
	public void setRootNode(){
		rootNode = true;
	}
	
	public boolean isRootNode(){
		return rootNode;
	}
}
