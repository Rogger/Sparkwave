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
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import at.sti2.spark.core.condition.TripleCondition;
import at.sti2.spark.core.solution.Match;
import at.sti2.spark.core.solution.OutputBuffer;
import at.sti2.spark.core.triple.RDFValue;
import at.sti2.spark.core.triple.variable.RDFVariable;
import at.sti2.spark.rete.Token;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.node.RETENode;

public class ProductionNode extends RETENode {
	
	static Logger logger = Logger.getLogger(ProductionNode.class);

	private List <Token> items = null;
	
	long matchCounter = 0;
	
	private OutputBuffer outputBuffer = null;
	
	public ProductionNode(){
//		items = Collections.synchronizedList(new ArrayList <Token> ());
		items = new ArrayList <Token> ();
		outputBuffer = new OutputBuffer();
	}
	
	public void addItem(Token token){
//		synchronized(items){
			items.add(token);
//		}
	}
	
	public void removeItem(Token token){
//		synchronized(items){
			items.remove(token);
//		}
	}
	
	@Override
	public void rightActivate(WorkingMemoryElement wme) {

	}
	
	@Override
	public void leftActivate(Token token) {
		
	}

	public long getMatchCounterValue() {
		return matchCounter;
	}

	public void leftActivate(Token token, WorkingMemoryElement wme) {
		
		Token newToken = createToken(token, wme);
		
//		addItem(newToken);
		matchCounter++;
		
		Match match = new Match();
		match.setVariableBindings(getVariableBindings(newToken));
		outputBuffer.put(match);
		
//		StringBuffer buffer = new StringBuffer();
//		buffer.append("We have " + (matchCounter) + " match:\n");
//		Token printToken = newToken;
//		
//		while (printToken != null){
//			buffer.append(printToken.getWme().getTriple().toString());
//			buffer.append('\n');
//			printToken = printToken.getParent();
//		}
//		
//		buffer.append("Time interval [" + newToken.getTimeInterval() + "] ms.");
//		
//		System.out.println(buffer.toString());
		
	}
	
	private void removeMatchToken(Token token){
		
		Token currentToken = token.getParent();
		
		while(currentToken != null){
			
//			if(currentToken.getChildrenSize() >
		}
	}
	
	public OutputBuffer getOutputBuffer() {
		return outputBuffer;
	}

	private Token createToken(Token parentToken, WorkingMemoryElement wme){
		
		Token newToken = new Token();
		newToken.setParent(parentToken);
		newToken.setWme(wme);
		//Added for retraction purposes
		newToken.setNode(this);
		
		if (parentToken!=null){
			parentToken.addChild(newToken);
			
			//Insert initial time interval for the new token
			newToken.setStartTime(parentToken.getStartTime());
			newToken.setEndTime(parentToken.getEndTime());
			
			if (wme.getTriple().getTimestamp()<newToken.getStartTime())
				newToken.setStartTime(wme.getTriple().getTimestamp());
			else if (wme.getTriple().getTimestamp()>newToken.getEndTime())
				newToken.setEndTime(wme.getTriple().getTimestamp());
			
		} else {
			//Token without parent is token at dummy (root) beta memory
			//It will have start and end time as streamed triple
			newToken.setStartTime(wme.getTriple().getTimestamp());
			newToken.setEndTime(wme.getTriple().getTimestamp());
		}
		
//		wme.addToken(newToken);
		
		return newToken;
	}

	public List<Token> getItems() {
		return items;
	}
	
	private Hashtable <String, RDFValue> getVariableBindings(Token productionToken){
		
		Hashtable <String, RDFValue> variableBindings = new Hashtable<String, RDFValue>();
		
		Token tempToken = productionToken;
		String variableId = null;
		
		while (tempToken != null){
			
			TripleCondition condition = ((JoinNode)tempToken.getNode().getParent()).getTripleCondition();
			
			if (condition.isVariableAtSubject()){
				variableId = ((RDFVariable)condition.getConditionTriple().getSubject()).getVariableId();
				if (!variableBindings.containsKey(variableId))
					variableBindings.put(variableId, tempToken.getWme().getTriple().getRDFTriple().getSubject());
			}
			
			if (condition.isVariableAtPredicate()){
				variableId = ((RDFVariable)condition.getConditionTriple().getPredicate()).getVariableId();
				if (!variableBindings.containsKey(variableId))
					variableBindings.put(variableId, tempToken.getWme().getTriple().getRDFTriple().getPredicate());
			}
			
			if (condition.isVariableAtObject()){
				variableId = ((RDFVariable)condition.getConditionTriple().getObject()).getVariableId();
				if (!variableBindings.containsKey(variableId))
					variableBindings.put(variableId, tempToken.getWme().getTriple().getRDFTriple().getObject());
			}
			
			tempToken = tempToken.getParent();
		}
		
		return variableBindings;
	}
	
	//The strategy for removing is to climb until the top most token while deleting WME'S from alpha mems
//	private void removeResult(Token resultToken){
//		
//		Token removeToken = resultToken;
//		
//		while(removeToken!=null){
//			
//			//Delete associated WME from alphamem lists
//			//Remove occurrence from each alpha memory
//			for (AlphaMemory alphamem : removeToken.getWme().getAlphaMems())
//				alphamem.removeItem(removeToken.getWme());
//			
//			//Delete the token WME from alpha memories
//			removeToken.getWme().getTokens().remove(removeToken);
//			
//			//Delete the token from the node list
//			if (removeToken.getNode() instanceof BetaMemory)
//				((BetaMemory)removeToken.getNode()).getItems().remove(this);
//			else if (removeToken.getNode() instanceof ProductionNode)
//				((ProductionNode)removeToken.getNode()).getItems().remove(this);
//			
//			//Delete token from the parent list
//			if (removeToken.getParent()!=null)
//				removeToken.getParent().getChildren().remove(this);
//			
//			removeToken = removeToken.getParent();
//		}
//		
//	}
	
	

}
