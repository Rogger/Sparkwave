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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import at.sti2.spark.core.condition.TripleCondition;
import at.sti2.spark.rete.Token;
import at.sti2.spark.rete.WorkingMemoryElement;
import at.sti2.spark.rete.alpha.AlphaMemory;
import at.sti2.spark.rete.node.RETENode;

public class JoinNode extends RETENode {

	private AlphaMemory alphaMemory = null;
	private List <JoinNodeTest> tests = null;
	private long timeWindowLength = 0l;
	private TripleCondition tripleCondition = null;
	
	public JoinNode(TripleCondition tripleCondition, long timeWindowLength){
		tests = new ArrayList <JoinNodeTest> ();
		this.timeWindowLength = timeWindowLength;
		this.tripleCondition = tripleCondition;
	}
	
	public AlphaMemory getAlphaMemory() {
		return alphaMemory;
	}

	public void setAlphaMemory(AlphaMemory alphaMemory) {
		this.alphaMemory = alphaMemory;
	}

	public List<JoinNodeTest> getTests() {
		return tests;
	}
	
	public void addJoinNodeTest(JoinNodeTest test){
		tests.add(test);
	}

	public TripleCondition getTripleCondition() {
		return tripleCondition;
	}

	/**
	 * Activation coming from an alpha memory.
	 */
	@Override
	public void rightActivate(WorkingMemoryElement wme) {
		
		//Look into the beta memory to find any token for which tests succeed.
		synchronized(((BetaMemory)parent).getItems()){
			Iterator <Token> betaMemoryTokenIter = ((BetaMemory)parent).getItems().iterator();
			
			while (betaMemoryTokenIter.hasNext()){
				
				Token betaMemoryToken = betaMemoryTokenIter.next();

				//Check if variables have the same value
				if (performTests(betaMemoryToken, wme)){
					
					//Check if the token and wme are falling into a window
					if (!(wme.getTriple().isPermanent()) && (!performTimeWindowTest(betaMemoryToken, wme)))
						continue;
						
					for (RETENode reteNode : children)
						if (reteNode instanceof BetaMemory)
							((BetaMemory)reteNode).leftActivate(betaMemoryToken, wme);
						else
							((ProductionNode)reteNode).leftActivate(betaMemoryToken, wme);
							
				}				
			}
		}
		
		//If the join node is under dummy root beta node left activation should fire
		if (((BetaMemory)parent).isRootNode())
			for (RETENode reteNode : children)
				if (reteNode instanceof BetaMemory)
					((BetaMemory)reteNode).leftActivate(null, wme);
				else 
					((ProductionNode)reteNode).leftActivate(null, wme);
	}

	/**
	 * Activation coming from a beta/root node.
	 */
	@Override
	public void leftActivate(Token token) {
		
		synchronized(alphaMemory.getItems()){
			for (Iterator <WorkingMemoryElement> wmeIterator = alphaMemory.getItems().iterator(); wmeIterator.hasNext(); ) {
				
				WorkingMemoryElement alphaWME = wmeIterator.next();
				
				//Check if two WME and token can be joined
				if (performTests(token, alphaWME)) {
					
					//Check if the token and wme are falling into a window
					if (!(alphaWME.getTriple().isPermanent()) && (!performTimeWindowTest(token, alphaWME)))
						continue;
						
					for (RETENode reteNode : children)
						if (reteNode instanceof BetaMemory)
							((BetaMemory)reteNode).leftActivate(token, alphaWME);
						else
							((ProductionNode)reteNode).leftActivate(token, alphaWME);
					
				}
			}
		}
	}
	
	public void setTests(List<JoinNodeTest> tests) {
		this.tests = tests;
	}

	private boolean performTimeWindowTest(Token token, WorkingMemoryElement wme){
		if (wme.getTriple().getTimestamp()<token.getStartTime())
			return (token.getEndTime() - wme.getTriple().getTimestamp()) < timeWindowLength;
		else if (wme.getTriple().getTimestamp()>token.getEndTime())
			return (wme.getTriple().getTimestamp() - token.getStartTime()) < timeWindowLength; 
		else return token.getEndTime() - token.getStartTime() < timeWindowLength;
	}
	
	public boolean performTests (Token token, WorkingMemoryElement wme){
		
		String lexicalValueArg1 = null;
		String lexicalvalueArg2 = null;
		
		for (JoinNodeTest test : tests){
			lexicalValueArg1 = wme.getTriple().getRDFTriple().getLexicalValueOfField(test.getArg1Field());
			
			//Select WME
			int index = test.getArg2ConditionNumber();
			
			//TODO Fix this for faster processing; instead of using indices maybe we can use pointers?!
			//The value I have is the one of index in the tree, while the only thing I have is the token from which to start.
			
			Vector<Token> wmeTokenVect = getTokenVect(token);
			Token wmeToken = wmeTokenVect.get(index);
			
			lexicalvalueArg2 = wmeToken.getWme().getTriple().getRDFTriple().getLexicalValueOfField(test.getArg2Field());
			
			if (!lexicalValueArg1.equals(lexicalvalueArg2))
				return false;
		}
		
		return true;
	}
	
	private Vector<Token> getTokenVect(Token token){
		
		Vector <Token> tokenVect = new Vector <Token>();
		Token tempToken = token;
		while (tempToken != null){
			tokenVect.add(0, tempToken);
			tempToken = tempToken.getParent();
		}
			
		return tokenVect;
	}

	@Override
	public void leftActivate(Token token, WorkingMemoryElement wme) {
		// TODO Auto-generated method stub
	}
}
